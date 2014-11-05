#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` conll-json-file  documents-file.json"
  exit $E_BADARGS
fi

echo "converting $1"
$JAVA $CLI.ConvertConllDocumentsToDocumentsCLI -input $1 -output $2
echo "documents in $2"
