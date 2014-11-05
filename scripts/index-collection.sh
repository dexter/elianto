#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` collection-file.json"
  exit $E_BADARGS
fi

echo "converting $1"
mvn exec:java -Dexec.mainClass="$CLI.IndexCollectionCLI" -Dexec.classpathScope=runtime -Dexec.args="-input $1"
echo "indexed"
