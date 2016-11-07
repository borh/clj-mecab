(def project 'clj-mecab)
(def version "0.4.9")

(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.9.0-alpha14"]

                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [org.clojure/test.check "0.9.0" :scope "test"]
                            [adzerk/bootlaces "0.1.13" :scope "test"]

                            [org.clojure/data.csv "0.1.3"]
                            [com.nativelibs4java/bridj "0.7.0"]
                            [net.moraleboost.cmecab-java/cmecab-java "2.1.0"]])

(task-options!
 pom {:project     project
      :version     version
      :description "Clojure wrapper for the Japanese Morphological Analyzer MeCab"
      :url         "https://github.com/borh/clj-mecab"
      :scm         {:url "https://github.com/borh/clj-mecab"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"
                    "BSD"
                    "BSD"}})

(require '[adzerk.bootlaces :refer :all])

(bootlaces! version)

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(deftask dev
  []
  (comp (watch) (build) (repl :server true)))

(require '[adzerk.boot-test :refer [test]])
