# Armedia Commons Utilities

The Commons Utilities package is a set of utility classes used throughout many projects within Armedia. They provide tools for many different tasks ranging from concurrency management, to reliable hash code calculation, functional programming, XML processing, encoding/decoding data, among many others.

If you want to take it out for a whirl, feel free!!

## Build

1. Build the Caliente [Commons-Utilities](https://github.com/Armedia/Commons-Utilities) package. This package is not publicly available in binary form (yet!), but you should be able to pull the sources and build it locally without issue.

```
$ git clone https://github.com/Armedia/Commons-Utilities
$ cd Commons-Utilities
$ mvn install
```

The build process supports three build profiles:

* "No" profile (i.e. SNAPSHOT)
  * Default when no profile is specified
  * Will generate artifacts with SNAPSHOT in their name
* Beta profile (-Pbeta)
  * Requires defining a **betaNumber** variable which will supplement built artifacts' tags (i.e. -DbetaNumber=03)
* Release profile (-Prelease)
  * Used for release builds, builds the "pure" artifact (i.e. no "SNAPSHOT" or beta tag)

### That's it!!

Enjoy!