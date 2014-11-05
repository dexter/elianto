## How to generate a Wikinews dataset

### Get the original dataset

  1. Download the xml dump of wikinews
  2. Convert it to json using [json-wikipedia](https://github.com/diegoceccarelli/json-wikipedia)
  3. You can download the english dump converted [here](https://dl.dropboxusercontent.com/u/4663256/datasets/en-wikinews.json.gz)
  
### Generate the documents / spots / entities for Elianto

In order to manually annotate the entities with Elianto you need to produce three files 
that you'll have to injest in Elianto. 
  1. The documents 
  2. The mentions for each document and the entity assigned by the annotators
  3. The description of the entities
  
In order to generate the documents and the mention run: 
 
    scripts/create-collection-from-wikinews.sh wikinews.json documents.json annotatedspots.json

Given the json wikinews, `wikinews.json` will generate the documents and the annotated spots.
If you want to filter out some document (too long documents, or just keep the documents that contain at
list a certain number of annotations), modify the method `isFilter` in `WikiNews.WikiNewsFilter`

    ./src/main/java/it/cnr/isti/hpc/dexter/annotate/wikinews/WikiNews.java
	public boolean isFilter(WikiNews news) {
    ...
	

to return true if the WikiNews is not good. 

Then recompile the project with:

    mvn assembly:assembly -DskipTests

and regenerate the documents:

    scripts/create-collection-from-wikinews.sh wikinews.json documents.json annotated-spots.json


### Index the model

Once you have the annotated spots, you can generate the entity description with: 

	scripts/retrieve-entities.sh annotated-spots.json entity-descriptions.json

then index: 

    scripts/index-collection.sh documents.json
    scripts/index-spots.sh annotated-spots.json
	scripts/index-entities.sh entity-descriptions.json
