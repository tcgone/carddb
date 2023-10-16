# carddb

The mighty new card database of TCG ONE.

Models can be inspected [here](https://github.com/tcgone/carddb/tree/master/model/src/main/java/tcgone/carddb/model)

Card data is defined in YAML files and placed in [`data/src/main/resources/cards`](https://github.com/tcgone/carddb/tree/master/data/src/main/resources).

Data in this repository powers the card, set and format data on https://tcgone.net/cards.

The definitions are also served through an API under `https://tcgone.net/api/`. Examples:
- `https://tcgone.net/api/v1/cards/search?query=bulbasaur`
- `https://tcgone.net/api/v1/cards/search?set=bs`
- `https://tcgone.net/api/v1/sets`
- `https://tcgone.net/api/v1/formats`

You are free to use this API for your projects, please contact us in [#dev](https://discord.gg/JZP2qzU) for more information. 

### Automatic Build and Deployment via Github Actions

When a new commit is pushed to a pull request, `build` action builds and runs some validations on the changes. These checks must pass in order to allow the code to be merged to `master`. 

When a new commit is pushed to `master` (i.e. after merging a PR), `deploy` action builds and deploys the project artifact to our maven repository. 

In order to have the card changes show up, you may ask [a project contributor](https://github.com/orgs/tcgone/people) to trigger the `deploy` action of `tcgone/front` module.

# carddb/tools

This module has some toolset to add/update cards to TCG ONE card database and card implementations.

## Features

- Reading card data from [pokemontcg.io](https://github.com/PokemonTCG/pokemon-tcg-data/tree/master/cards/en) or [Kirby's format (preferred)]((https://github.com/kirbyUK/ptcgo-data/tree/master/en_US))
- Reading expansion data from [pokemontcg.io](https://github.com/PokemonTCG/pokemon-tcg-data/tree/master/sets)
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
    - Read expansion data from [pokemontcg.io](https://github.com/PokemonTCG/pokemon-tcg-data/tree/master/sets) to fill in several details you otherwise have to remember to fill in yourself. `java -jar target/carddb-tools-*.jar`**`"--pio-expansion=../pokemon-tcg-data/sets/en.json"`**`"--pio=../ptcgo-data/en_US/sm10.json" "--pio=../ptcgo-data/en_US/det1.json" --export-yaml --export-impl-tmpl`
1. Download scans: `java -jar target/carddb-tools-*.jar "--pio=../ptcgo-data/en_US/sm10.json" "--pio=../ptcgo-data/en_US/det1.json" --download-scans`
    - Expansion data can be read here automatically name the folder created to hold the scans. **Note**: Folder names for existing Promo sets should be checked for accuracy before adding to the scans server.
