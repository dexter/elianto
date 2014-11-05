#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` exportDir"
  exit $E_BADARGS
fi

echo "querying user-annotations inside the database"
mvn exec:java -Dexec.mainClass="$CLI.ExportCLI" -Dexec.classpathScope=runtime -Dexec.args="-output $1"

echo "documents in $1"
