#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` annotated-spots.json[.gz] entities.json"
  exit $E_BADARGS
fi

echo "retrieving entity descriptions for spots in $1"
$JAVA $CLI.RetrieveEntityDescriptionCLI -input $1 -output $2
echo "entity descriptions in $2"
