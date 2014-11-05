#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0`  log.txt"
  exit $E_BADARGS
fi

echo "deleting spots"

mvn exec:java -Dexec.mainClass="$CLI.DeleteOverlappingSpotsCLI" -Dexec.classpathScope=runtime -Dexec.args="-output $1"
echo "indexed"
