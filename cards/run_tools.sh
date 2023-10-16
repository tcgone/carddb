#!/usr/bin/env bash
mvn -Dexec.mainClass=tcgone.carddb.tools.Application -Dexec.args="--yaml=src/main/resources/cards --export-e3" -Dexec.classpathScope=test test-compile exec:java