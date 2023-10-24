#!/usr/bin/env bash
mvn -Dexec.mainClass=tcgone.carddb.tools.Application -Dexec.args="--yaml=src/main/resources/cards --run-interactive-merger --export-yaml" -Dexec.classpathScope=test test-compile exec:java
