(defproject clj-mecab "0.2.0-SNAPSHOT"
  :description "Clojure wrapper for the Japanese Morphological Analyzer MeCab"
  :url "https://github.com/borh/clj-mecab"
  :licence {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :licenses [{:name "Eclipse Public License"
              :url "http://www.eclipse.org/legal/epl-v10.html"}
             {:name "BSD"
              :url "BSD"}]
  :dependencies [[org.chasen/mecab "0.996"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]]}}
  :main clj-mecab.parse)
