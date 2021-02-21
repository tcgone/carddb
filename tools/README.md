# carddb/tools

This module has some toolset to add/update cards to TCG ONE card database and card implementations.

## Features

- Reading from [pokemontcg.io](https://github.com/PokemonTCG/pokemon-tcg-data/tree/master/json/cards) or [Kirby's format (preferred)]((https://github.com/kirbyUK/ptcgo-data/tree/master/en_US))
- Downloading scans
- Generating [TCG ONE Card Database YAML files](https://github.com/tcgone/carddb/tree/master/data/src/main/resources/cards)
- Generating [TCG ONE Engine Implementation Groovy Files](https://github.com/axpendix/tcgone-engine-contrib/tree/master/src/tcgwars/logic/impl)
- Reading from [TCG ONE Card Database YAML files](https://github.com/tcgone/carddb/tree/master/data/src/main/resources/cards)
- Processing card scans in batch before uploading to [Scans Server](https://forum.tcgone.net/t/6697) via [scans.pl](https://github.com/tcgone/carddb/blob/master/tools/scripts/scans.pl)

## Instructions

1. Clone the repo: `git clone https://github.com/tcgone/carddb.git`
1. Build the whole `carddb` module: `mvn install`
1. Switch to tools: `cd tools`
1. View options: `java -jar target/carddb-tools-*.jar`
1. Read from [kirby's repo](https://github.com/kirbyUK/ptcgo-data/tree/master/en_US), convert to yaml and implementation templates: `java -jar target/carddb-tools-*.jar "--pio=../ptcgo-data/en_US/sm10.json" "--pio=../ptcgo-data/en_US/det1.json" --export-yaml --export-impl-tmpl`
1. Download scans: `java -jar target/carddb-tools-*.jar "--pio=../ptcgo-data/en_US/sm10.json" "--pio=../ptcgo-data/en_US/det1.json" --download-scans`
