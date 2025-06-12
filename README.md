# Envelop for Chromatik

**Envelop for Chromatik** is an open-source content package for [Chromatik](https://chromatik.co/). It provides lighting models of the Envelop venues, animation content, and legacy software support.

## Installation

_NOTE: Envelop for Chromatik is currently under development. It relies upon some features that have not yet been released in Chromatik 1.1.0._

- Install the latest [Chromatik pre-release &rarr;](https://github.com/heronarts/Chromatik/releases/tag/1.1.1-SNAPSHOT-2025-06-11)
- Download the [Envelop for Chromatik content package &rarr;](https://github.com/EnvelopSound/EnvelopForChromatik/releases/download/0.0.1-SNAPSHOT-2025-06-12/envelop-0.0.1-SNAPSHOT.jar)

Once Chromatik is up and running, drag and drop the downloaded file `envelop-0.0.1-SNAPSHOT.jar` onto the Chromatik window, follow the prompt to install the package.

Follow the instructions in [Migration Guide &rarr;](MIGRATION.md)

### Developer Install

The development package can be built and installed manually with Maven.

```
$ mvn install
```

## Licensing

Envelop for Chromatik is generally made available under the [GPLv2 License](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html). These means that you are free to modify and use Envelop for Live in your own projects, so long as they are compatible with the GPL.
