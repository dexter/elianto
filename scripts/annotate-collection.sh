#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=3

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` collection-name annotated-spots.json[.gz] spotter"
  exit $E_BADARGS
fi

echo "annotating collection $1 using spotter $3"
$JAVA $CLI.AnnotateCollectionCLI -input $1 -spotter $3 -output $2
echo "annotated spot in $2"
