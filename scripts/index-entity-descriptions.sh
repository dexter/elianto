#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` entity-description.json[.gz]"
  exit $E_BADARGS
fi

echo "indexing $1"
mvn exec:java -Dexec.mainClass="$CLI.IndexEntityDescriptionsCLI" -Dexec.classpathScope=runtime -Dexec.args="-input $1"
echo "indexed"
