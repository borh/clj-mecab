(defproject clj-mecab "0.1.0"
  :description "Clojure wrapper for the Japanese Morphological Analyzer MeCab"
  :url "https://github.com/borh/clj-mecab"
  :licenses [{:name "Eclipse Public License"
              :url "http://www.eclipse.org/legal/epl-v10.html"}
             {:name "BSD"
              :url "BSD"}]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.chasen/mecab "0.996"]]
  :main clj-mecab.parse)
