# clj-mecab

Clojure wrapper for the Japanese Morphological Analyzer MeCab.

## About

A minimal wrapper around the SWIG-generated Java bindings for MeCab.
Currently tested with [UniDic](http://en.sourceforge.jp/projects/unidic/) and IPAdic, although other dictionaries are planned.

## Prerequisites

clj-mecab requires you to have [MeCab](https://code.google.com/p/mecab/) (0.996) installed (the `mecab-config` binary is used to find your MeCab configuration) and on your path.
You also need to have the Java JNI (SWIG) bindings for the version of MeCab you have installed on your system installed in your local Maven repository (`~/.m2`).
This can be accomplished by:

```bash
mvn install:install-file -DgroupId=org.chasen -DartifactId=mecab -Dpackaging=jar -Dversion=0.996 -Dfile=/usr/share/java/mecab/MeCab.jar -DgeneratePom=true
```

Where `/usr/share/java/mecab/MeCab.jar` should point to the generated jar on your system.

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
    wget http://en.sourceforge.jp/frs/redir.php\\?m\\=jaist\\&f\\=%2Funidic%2F58338%2Funidic-mecab-2.1.2_src.zip
    unzip -x unidic-mecab-2.1.2_src.zip
    cd unidic-mecab-2.1.2_src && ./configure --prefix=/usr && make -j4 && make install && cd ..
    ```

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
