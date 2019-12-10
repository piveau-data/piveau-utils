# ChangeLog

## [2.2.0](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/2.2.0) (2019-12-10)

**Added:**
* RDF Helper for renaming resources containing self references
* First draft implementation for a triple store connector `TripleStore` with appropriate dcat-ap managers

**Fixed:**
* Pre-processor handling unsupported parser mime types types

## [2.1.0](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/2.1.0) (2019-11-17)

**Added:**
* Util function for parsing language tags (formerly part of indexing)
* PiveauContext
* rdf4j based pre-processing to fix not allowed characters in URIRefs
 
## [2.0.0](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/2.0.0) (2019-11-08)

**Added:**
* DCAT-AP context for metrics in `DCATAPUriSchema`

**Removed:**
* Vocabularies code (separated to piveau-vocabularies)
* Indexing code (separated to piveau-indexing)

## [1.1.2](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/1.1.2) (2019-10-18)

**Fixed:**
* Typo in DQV property `computedOn`

## [1.1.1](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/1.1.1) (2019-10-02)

**Added:**
* reading and writing rdf datasets
* piveau dqv vocabulary
* prov vocabulary
* `RDFMimeTypes`
* piveau ConceptScheme scoring
* DQV metric for scoring
* Array parser for `ConfigHelper`
 
**Removed:**
* URL decoding when normalizing id

**Changed:**
* Hash now implemented in class `TripleHash` and in Kotlin

**Fixed:**
* typo in RDF mime type

## [1.1.0](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/1.1.0) (2019-08-23)

**Added:**
* `DQV` and `SHACL` namespaces
* Code coverage (surefire, jacoco)

**Changed:**
* Requires now latest LTS Java 11

## [1.0.0](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/1.0.0) (2019-08-08)

**Added:**
* ConfigHelper helps reading json config values either as string or as json object
* Canonical hash function for jena models

**Changed:**
* Morph to kotlin

## [0.0.3](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/0.0.3) (2019-05-17)

**Added:**
* Model read method with baseUri parameter

## [0.0.2](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/0.0.2) (2019-05-11)

**Added:**
* findIdentifier with flexible configuration

**Changed:**
* findIdentifier algorithm refined

## [0.0.1](https://gitlab.fokus.fraunhofer.de/viaduct/piveau-utils/tags/0.0.1) (2019-04-16)
Initial release