[![Clojars Project](https://img.shields.io/clojars/v/clj-mecab.svg)](https://clojars.org/clj-mecab)

# clj-mecab

Clojure wrapper for the Japanese Morphological Analyzer MeCab.

## About

A minimal wrapper around the SWIG-generated Java bindings for MeCab.
Currently tested with all varieties of  [UniDic](http://unidic.ninjal.ac.jp/) and IPAdic, although other dictionaries are planned.

## Prerequisites

clj-mecab requires you to have [MeCab](http://taku910.github.io/mecab/) (0.996) installed (the `mecab-config` binary is used to find your MeCab configuration) and on your path.

### Package manager

On Debian:

```bash
apt get install mecab mecab-utils libmecab-java libmecab-jni unidic-mecab
```

On MacOS:

```bash
brew install mecab mecab-unidic
```

Note that you will need to manually install Maven dependencies on MacOS (see next section).

### Maven dependencies

You also need to have the Java JNI (SWIG) bindings for the version of MeCab you have installed on your system installed in your local Maven repository (`~/.m2`).
This can be accomplished by:

```bash
mvn install:install-file -DgroupId=org.chasen -DartifactId=mecab -Dpackaging=jar -Dversion=0.996 -Dfile=/usr/share/java/mecab/MeCab.jar -DgeneratePom=true
```

Where `/usr/share/java/mecab/MeCab.jar` should point to the generated jar on your system.

You will also need to manually download [cmecab-java](https://github.com/takscape/cmecab-java) and install it into your local Maven repo:

```bash
wget https://github.com/takscape/cmecab-java/releases/download/2.1.0/cmecab-java-2.1.0.tar.gz
tar xzf cmecab-java-2.1.0.tar.gz
mvn install:install-file -DgroupId=net.moraleboost.cmecab-java -DartifactId=cmecab-java -Dpackaging=jar -Dversion=2.1.0 -Dfile=cmecab-java-2.1.0/cmecab-java-2.1.0.jar -DgeneratePom=true
```

### Manually building and installing MeCab

MeCab depends on [CRF++](http://crfpp.sourceforge.net/), so first install that.

```bash
wget http://crfpp.googlecode.com/files/CRF%2B%2B-0.58.tar.gz
tar xzf CRF++-0.58.tar.gz
cd CRF++-0.58 && ./configure && make -j4 && make install && cd ..
```

Next, install [MeCab](http://code.google.com/p/mecab/).

```bash
wget http://mecab.googlecode.com/files/mecab-0.996.tar.gz
tar xzf mecab-0.996.tar.gz
cd mecab-0.996 && ./configure --with-charset=utf8 --enable-utf8-only && make -j4 && make install && cd ..
```

And at least one dictionary:

-   IPAdic:

    ```bash
    wget http://mecab.googlecode.com/files/mecab-ipadic-2.7.0-20070801.tar.gz
    tar xzf mecab-ipadic-2.7.0-20070801.tar.gz
    cd mecab-ipadic-2.7.0-20070801 && ./configure --with-charset=utf8 && make -j4 && make install && cd ..
    ```

-   UniDic:

    ```bash
    curl -O http://unidic.ninjal.ac.jp/dictionaries/UniDic-gendai/stable/zip/unidic-cwj-2.2.0.zip
    unzip -x unidic-cwj-2.2.0.zip
    cd unidic-cwj-2.2.0 && install -d $(mecab-config --dicdir)/unidic-cwj && install -m 644 dicrc *.bin *.dic $(mecab-config --dicdir)/unidic-cwj && cd ..
    ```

## Leiningen Dependency

Include in :dependencies in your `project.clj`:

```clojure
[clj-mecab "0.4.12"]
```

## Usage

Interactive use:

`$ boot repl`

```clojure
(require '[clj-mecab.parse :as mecab])
(mecab/parse-sentence "こんにちは、世界！")

[{:orth "こんにちは", :f-type "*", :i-type "*", ...} {:orth "、", :f-type "*", :i-type "*", ...} {:orth "世界", :f-type "*", :i-type "*", ...} ...]
```

## Roadmap

Several features are planned for future versions:

-   Better examples

## BUGS

-   For some yet unknown reason, calling .getSurface on a Node object will not work (empty string) the first time, but will the second time.
    Currently this means that :orth is not generated when using IPAdic.
    UniDic provides the surface node in the features array and is unaffected.
    Probably same issue as taku910/mecab#26

## License

Copyright © 2013-2019 Bor Hodošček

Distributed under the Eclipse Public License, the same as Clojure, as well as the 3-clause BSD license.
