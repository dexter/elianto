#!/usr/bin/env bash
source ./scripts/config.sh

EXPECTED_ARGS=3

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` wikinews.json documents.json annotatedspots.json"
  exit $E_BADARGS
fi

echo "generating collection from wikinews $1"
$JAVA $CLI.AnnotateWikiNewsJsonCLI -input $1 -documents $2 -spots $3
echo "documents in $2, annotated spots in $3"
