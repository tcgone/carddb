# carddb-tools

This project has some toolset to add/update cards to TCG ONE card database and card implementations.

## Features

- Reading from [pokemontcg.io](https://github.com/PokemonTCG/pokemon-tcg-data/tree/master/json/cards) or [Kirby's format (preferred)]((https://github.com/kirbyUK/ptcgo-data/tree/master/en_US))
- Downloading scans
- Generating [TCG ONE Card Database YAML files](https://github.com/axpendix/carddb/tree/master/src/main/resources/cards)
- Generating [TCG ONE Engine Implementation Groovy Files](https://github.com/axpendix/tcgone-engine-contrib/tree/master/src/tcgwars/logic/impl)
- Reading from [TCG ONE Card Database YAML files](https://github.com/axpendix/carddb/tree/master/src/main/resources/cards)
- Processing card scans in batch before uploading to [Scans Server](https://forum.tcgone.net/t/6697) via [scans.pl](https://github.com/axpendix/carddb-tools/blob/master/scripts/scans.pl)

## Instructions

1. Clone this repo and run: `./mvnw package`. Note that the [`carddb`](https://github.com/axpendix/carddb) dependency is now handled via jitpack.io.
1. View options: `java -jar target/carddb-tools-*.jar`
1. Read from [kirby's repo](https://github.com/kirbyUK/ptcgo-data/tree/master/en_US), convert to yaml and implementation templates: `java -jar target/carddb-tools-*.jar "--pio=../ptcgo-data/en_US/sm10.json" "--pio=../ptcgo-data/en_US/det1.json" --export-yaml --export-impl-tmpl`
1. Download scans: `java -jar target/carddb-tools-*.jar "--pio=../ptcgo-data/en_US/sm10.json" "--pio=../ptcgo-data/en_US/det1.json" --download-scans`
