#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` annotated-spots.json"
  exit $E_BADARGS
fi

echo "converting $1"
mvn exec:java -Dexec.mainClass="$CLI.IndexAnnotatedSpotsCLI" -Dexec.classpathScope=runtime -Dexec.args="-input $1"
echo "indexed"
