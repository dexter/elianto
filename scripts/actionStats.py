#!/usr/bin/env python
import json
import logging
import sys
from optparse import OptionParser

def main():
    parser = OptionParser()
    parser.add_option("-i", "--input", dest="input",
                      help="json file with actions", metavar="FILE")
    parser.add_option("-o", "--output", dest="output",
                      help="json file with aggregated statistics", metavar="FILE")
    (options, args) = parser.parse_args()
    
    if not options.input or not options.output:    
        print parser.print_help()
        sys.exit(-1)
        
    actions = []
    with open(options.input) as data_file:
        for line in data_file:    
            action = json.loads(line)
            action['docId'] = action['doc']['docId']
            del action['doc']
            action['userId'] = action['user']['uid'] 
            del action['user']
            actions.insert(action['id'], action)
            print action
        
    start_actions = {}
    annotation_times = {}
    for action in actions:
        key = (action['userId'], action['docId'])
        if action['type'] == 'GET_DOCUMENT':
            start_actions[key] = action['timestamp']
        else:
            delta = round( ( action['timestamp'] - start_actions[key]) / 1000., 2)
            stat = {'userId': action['userId'], 'docId': action['docId'], 'deltaTime': delta, 'type': action['type'], 'start': start_actions[key]}
            stat_key = (action['userId'], action['docId'], action['type'])
            if stat_key in annotation_times:
                if annotation_times[stat_key]['start'] == start_actions[key]:
                    # replace the old record
                    annotation_times[stat_key] = stat
                else:
                    # sum the time with the one from the old record
                    print '----------------------'
                    print stat
                    print annotation_times[stat_key]
                    stat['deltaTime'] = round(stat['deltaTime'] + annotation_times[stat_key]['deltaTime'], 2)
                    annotation_times[stat_key] = stat
            else:
                annotation_times[stat_key] = stat
            
    # replace the dictionary with an ordered list (by docId) 
    stats = sorted(annotation_times.values(), key=lambda stat: stat['docId'])   

    with open(options.output, 'w') as output:
        for stat in stats:
            del stat['start']
            json.dump(stat, output)
            output.write('\n')
            print stat


if __name__ == "__main__":
    logging.basicConfig(format='%(asctime)s %(levelname)s %(message)s', level=logging.INFO)
    sys.exit(main())