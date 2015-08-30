(defproject clj-mecab "0.4.1.4"
  :description "Clojure wrapper for the Japanese Morphological Analyzer MeCab"
  :url "https://github.com/borh/clj-mecab"
  :licence {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :licenses [{:name "Eclipse Public License"
              :url "http://www.eclipse.org/legal/epl-v10.html"}
             {:name "BSD"
              :url "BSD"}]
  :scm {:url "https://github.com/borh/clj-mecab.git"
        :name "git"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.nativelibs4java/bridj "0.6.2"]
                 [cmecab/cmecab "2.0.1"]
                 [org.clojure/data.csv "0.1.3"]
                 [prismatic/schema "1.0.0-alpha1"]]
  :jvm-opts ["-server"]
  :profiles {:dev {:dependencies [[criterium "0.4.3"]]}}
  :main clj-mecab.parse)
