# clj-mecab

Clojure wrapper for the Japanese Morphological Analyzer MeCab.

## About

A minimal wrapper around the SWIG-generated Java bindings for MeCab.
Currently tested with [UniDic](http://en.sourceforge.jp/projects/unidic/) and IPAdic, although other dictionaries are planned.

## Prerequisites

clj-mecab requires you to have [MeCab](https://code.google.com/p/mecab/) (0.996) installed (the `mecab-config` binary is used to find your MeCab configuration) and on your path.
You also need to have the Java JNI (SWIG) bindings for MeCab installed in Maven.
This can be accomplished by:

```bash
mvn install:install-file -DgroupId=org.chasen -DartifactId=mecab -Dpackaging=jar -Dversion=0.996 -Dfile=/usr/share/java/mecab/MeCab.jar -DgeneratePom=true
```

Where `/usr/share/java/mecab/MeCab.jar` should point to the generated jar on your system.

## Leiningen Dependency

Include in :dependencies in your `project.clj`:

```clojure
[clj-mecab "0.1.0"]
```

## Usage

```clojure
(use 'clj-mecab.parse)
(parse-sentence "こんにちは、世界！")

[{:orth "こんにちは", :fType "*", :iType "*", ...} {:orth "、", :fType "*", :iType "*", ...} {:orth "世界", :fType "*", :iType "*", ...} ...]
```

## BUGS

-   For some yet unknown reason, calling .getSurface on a Node object will not work (empty string) the first time, but will the second time.
    Currently this means that :orth is not generated when using IPAdic.

## License

Copyright © 2013 Bor Hodošček

Distributed under the Eclipse Public License, the same as Clojure, as well as the 3-clause BSD license.
