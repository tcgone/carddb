# carddb

The mighty new card database of TCG ONE.

Models can be inspected [here](https://github.com/tcgone/carddb/tree/master/src/main/java/net/tcgone/carddb/model)

Card data is defined in YAML files and placed in [`src/main/resources/cards`](https://github.com/tcgone/carddb/tree/master/src/main/resources).

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
