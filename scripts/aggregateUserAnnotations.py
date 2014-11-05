#!/usr/bin/env python
import json
import logging
import sys
import numpy as np
import scipy as sp
import math
import os
import scipy.spatial.distance as scipyDistance
import scipy.stats as scipyStats
from optparse import OptionParser
from collections import defaultdict
from itertools import izip

def main():
    parser = OptionParser()
    parser.add_option("-i", "--input", dest="input",
                      help="json file with annotations", metavar="FILE")
    parser.add_option("-o", "--outputDir", dest="outputDir",
                      help="directory where will be saved the files", metavar="FILE")
    parser.add_option("-m", "--min", dest="minAgreement",
                      help="minimum number of annotations to use for a document", default=3)
    
    (options, args) = parser.parse_args()
    if not options.input or not options.outputDir:
        print parser.print_help()
        sys.exit(-1)
        
    minAgreement = int(options.minAgreement)

    k = 4
    n = defaultdict(int, default=0)
    annotationMatrix = defaultdict(dict)
    userEntity = defaultdict(int)
    with open(options.input) as data_file:
        for line in data_file:
            annotation = json.loads(line)
            entityId = annotation['entityId']
            userScore = annotation['userScore']
            docId = annotation['doc']['docId']
            userId = int(annotation['user']['uid'])

            if userId not in annotationMatrix[docId]:
                n[docId] += 1
                annotationMatrix[docId][userId] = {}
                for i in range(0, 4):
                    annotationMatrix[docId][userId][i] = []
                    
            keyUserEntity = (userId, docId, entityId)
            if keyUserEntity not in userEntity:
                annotationMatrix[docId][userId][userScore].append(entityId)
                userEntity[keyUserEntity] = 1
                
    # Delete the documents with less than minAgreement annotation
    for docId in annotationMatrix.keys():
        if n[docId] < minAgreement:
            del annotationMatrix[docId]
        
    scoreMatrix = {}
    mappingEntityIdToIndex  = {}
    mappingUserIdToIndex  = {}
    for docId in annotationMatrix:
        globalAnnotatedEntities = set([ entityId 
        for userId in annotationMatrix[docId] 
            for score in annotationMatrix[docId][userId] 
                for entityId in annotationMatrix[docId][userId][score]] )
        scoreMatrix[docId] = np.zeros( (n[docId], len(globalAnnotatedEntities)), dtype=int)
        mappingEntityIdToIndex[docId] = dict(izip(globalAnnotatedEntities, range(len(globalAnnotatedEntities))))
        mappingUserIdToIndex[docId] = dict(izip(annotationMatrix[docId].keys(), range(len(annotationMatrix[docId].keys()))))
        scoreMatrix[docId] = np.zeros( (n[docId], len(globalAnnotatedEntities)), dtype=int)
        
        for userId in annotationMatrix[docId]:
            for score in annotationMatrix[docId][userId]:
                for entityId in annotationMatrix[docId][userId][score]:
                    scoreMatrix[docId][ mappingUserIdToIndex[docId][userId], mappingEntityIdToIndex[docId][entityId] ] = score

    userAgreement = defaultdict(list)
    for docId in scoreMatrix:
        
        #print 
        #print scoreMatrix[docId]
        kendallTauMatrixCondensed = computeKendallTau(scoreMatrix, docId)
        kendallTauMatrix = scipyDistance.squareform(kendallTauMatrixCondensed)
        kendallUser = [np.mean(np.delete(array, 0), dtype=float) for array in kendallTauMatrix]
        
        for (userId, idx) in mappingUserIdToIndex[docId].iteritems():
             userAgreement[userId].append(kendallUser[idx])

    print 'User Agreement:'
    for userId in userAgreement:
        userAgreement[userId] = np.mean(userAgreement[userId], dtype=float)
        print 'userId %d: %.3f' % (userId, userAgreement[userId])
    
    
    averageScore = defaultdict(float)
    weightedScore = defaultdict(float)
    for docId in scoreMatrix:
        scores = np.transpose(scoreMatrix[docId])
        averageScore[docId] = [np.mean(entityScores, dtype=float) for entityScores in scores]
        
        weights = np.zeros( len(scoreMatrix[docId]), dtype=float)
        for (userId, idx) in mappingUserIdToIndex[docId].iteritems():
            weights[idx] = userAgreement[userId]
        weightedScore[docId] = [np.average(entityScores, weights=weights) for entityScores in scores]
        
    with open(os.path.join(options.outputDir, 'dataset_math_average.tsv'), 'w') as math_average_file:
        with open(os.path.join(options.outputDir, 'dataset_weighted_average.tsv'), 'w') as weighted_average_file:
            for docId in scoreMatrix:
                for (entityId, idx) in mappingEntityIdToIndex[docId].iteritems():
                    math_average_file.write("%d\t%d\t%.3f\n" % (docId, entityId, averageScore[docId][idx]))
                    weighted_average_file.write("%d\t%d\t%.3f\n" % (docId, entityId, weightedScore[docId][idx]))
        

def computeKendallTau(scoreMatrix, docId):
    
    distanceMatrixCondensed = scipyDistance.pdist(scoreMatrix[docId], lambda u, v: scipyStats.kendalltau(u,v)[0] )
    # delete the nan entries
    distanceMatrixCondensed = distanceMatrixCondensed[np.logical_not(np.isnan(distanceMatrixCondensed))]
    #return np.mean(distanceMatrixCondensed, dtype=float)
    return distanceMatrixCondensed

if __name__ == "__main__":
    logging.basicConfig(format='%(asctime)s %(levelname)s %(message)s', level=logging.INFO)
    sys.exit(main())
