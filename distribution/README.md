UniFIRE Distribution
====================

This module contains the UniFIRE software entry points. It uses Maven Assembly to build all the sources and external libraries into a lib/ folder, and copy all the scripts into a bin/ folder.

* `/bin/` : Distributed scripts. Updated from `/target/unifire-distribution/` via `./update.sh`.
* `/lib/` : Distributed libraries. Updated from `/target/unifire-distribution/` via `./update.sh`.
* `/src/` : Sources classes and scripts
    * `assembly/`: maven assembly configuration
    * `main/`: Java classes for the upper level applications
    * `scripts/`: Corresponding launcher bash scripts
* `/target/unifire-distribution/`: Built sources and copied scripts. Run Maven package/install to update.
* `./update.sh`: copies `/target/unifire-distribution/*` to `.`(this folder). To be executed once you have a stable software for the users who just want to run the software.

---------------


