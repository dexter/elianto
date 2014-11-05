# Elianto: a framework for the production of human annotated rank-enriched datasets

The Entity Linking (EL) problem consists in automatically linking short fragments of text within a document to entities in a given Knowledge Base like Wikipedia. Due to its impact in several text-understanding related tasks, EL is an hot research topic. The correlated problem of devising the most relevant entities mentioned in the document, a.k.a. salient entities (SE), is also attracting increasing interest. Unfortunately, publicly available evaluation datasets that contain accurate and supervised knowledge about mentioned entities and their relevance ranking are currently very poor both in number and quality. This lack makes very difficult to compare different EL and SE solutions on a fair basis, as well as to devise innovative techniques that relies on these datasets to train machine learning models, in turn used to automatically link and rank entities.

For these reasons we implemented Elianto, an open-source web framework for the production of human annotated rank-enriched datasets for EL and SE tasks. It supports human labelling of semi-structured documents through a guided two-step process. In the first step, entities mentioned in a given document are annotated by users. In the second step, such entities are ranked on the basis of the perceived relevance/saliency. The framework is fully configurable to perform also only a single task (e.g. only annotation or only entity ranking) and provides all the tools needed to start annotating in a very short time.

For more information about the team and the framework. please refer to the [website](http://elianto.isti.cnr.it).

A simple demo is also available on the website. It is worth mentioning that Elianto is back-ended by the spotting module of [DEXTER](https://github.com/dexter-entity-linking/dexter), a framework that provides all the tools needed to develop any EL technique. Elianto thus exploits Dexter to identify a set of spots in the text documents to annotate, along with a list of candidate entities for each spot. This makes easier for the users the heavy job of identifying spots and associated entities in text document. In addition, Elianto allows users to provide new spots (not suggested by the tool) as well as new entities to associate with them.

## Generate the input files for Elianto

In order to manually annotate the entities with Elianto you need to produce three files 
that you'll have to injest in Elianto. 
  1. The documents
  2. The mentions for each document and the entity assigned by the annotators
  3. The description of the entities

### Generate the documents

In order to generate the documents you have to produce a file with a json-serialized version of each Document (`it.cnr.isti.hpc.dexter.annotate.bean.Document`) on each line. An example of such a CLI can be found in `ConvertConllDocumentsToDocumentsCLI` that can be run by using a simple bash script: 
 
    scripts/conll-to-documents.sh conll-json-file  documents.json

Given the json version of the [CoNLL](http://www.cnts.ua.ac.be/conll2003/ner/) dataset (popular in the EL field, you will find a json modified version in `datasets/conll.json.gz`), the script will generate the documents in the Elianto format. If you need to import a different dataset, you need to write a new CLI with a similar behavior, recompile the project with:

    mvn assembly:assembly -DskipTests

and then run it.

Once you have the document in the Elianto format, you need to index it in the sqlite database. The command to do this operation is:

    scripts/index-collection.sh documents.json

### Generate the mentions

Given the documents produced in the previous step (imported in the sqlite db), we need to generate the candidate spots for each document as well as the the candidate entities of each spot. For doing that you need to run the bash script:

	scripts/annotate-collection.sh collection-name annotated-spots.json

The collection name is the name of the dataset you imported in the previous step (it is a json field of each document). The script will produce in output a file with the candidate spot and the candidate entities for each document. We need to import them inside the sqlite db with the command:

    scripts/index-spots.sh annotated-spots.json

### Generate the entities

The description of each entity is stored inside the database for a simpler and faster UI. To obtain the descriptions you need to run the command:

	scripts/retrieve-entities.sh annotated-spots.json entity-descriptions.json

and the index it inside the database as usual:

	scripts/index-entities.sh entity-descriptions.json

## Start the server 

start the server running from the root of the project

	mvn jetty:run

## Configuring Elianto

In the project root there is a `project.properties` file which contains all the major settings of Elianto. From there you can:
* change the database configuration (type and name)
* set the email of the administrators (csv format). The administrators have full control of the application as well as full control over the annotation performed by the users
* change the EL system used for the spotting (you need to adapt also the code that interact with the server if you change this parameter)
* the bucket size and the document agreement: the documents are partitioned in buckets to avoid that the annotation are spread among a lot of documents with a low overlap on each one (the same document usually need to be annotated by more than one persons). Before moving to a new bucket (the system automatically manage the operation) the framework checks that are at least X annotation per document (where X is the agreement).
* annotation tasks: csv with the steps to perform (step1, step2 or both)
