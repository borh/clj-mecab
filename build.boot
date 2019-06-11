(require '[clojure.java.shell :as sh])

(defn next-version [version]
  (when version
    (let [[a b] (next (re-matches #"(.*?)([\d]+)" version))]
      (when (and a b)
        (str a (inc (Long/parseLong b)))))))

(defn deduce-version-from-git
  "Avoid another decade of pointless, unnecessary and error-prone
  fiddling with version labels in source code."
  []
  (let [[version commits hash dirty?]
        (next (re-matches #"(.*?)-(.*?)-(.*?)(-dirty)?\n"
                          (:out (sh/sh "git" "describe" "--dirty" "--long" "--tags" "--match" "[0-9].*"))))]
    (try
      (cond
        dirty? (str (next-version version) "-" hash "-dirty")
        (pos? (Long/parseLong commits)) (str (next-version version) "-" hash)
        :otherwise version)
      (catch Exception e (println "Not a git repository or empty repository. Please git init in this directory/make a commit.")))))

(def project "clj-mecab")
(def version (deduce-version-from-git))

(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.10.1" :scope "provided"]

                            [seancorfield/boot-tools-deps "0.4.7" :scope "test"]
                            [adzerk/bootlaces "0.2.0" :scope "test"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(task-options!
 pom {:project     (symbol project)
      :version     version
      :description "Clojure wrapper for the Japanese Morphological Analyzer MeCab"
      :url         "https://github.com/borh/clj-mecab"
      :scm         {:url "https://github.com/borh/clj-mecab"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"
                    "BSD"
                    "BSD"}}
 jar {:main 'clj-mecab.parse :file (str project "-" version ".jar")}
 target {:dir #{"target"}})

(require '[boot-tools-deps.core :refer [deps]])
(require '[adzerk.bootlaces :refer :all])

(bootlaces! version)

(deftask build []
  (comp (deps :quick-merge true) (pom) (jar) (target) (install)))

(deftask dev []
  (comp (watch) (deps :aliases [:test]) (repl :init-ns 'clj-mecab.parse :server true)))

(require '[adzerk.boot-test :as boot-test])
(deftask test []
  (comp (deps :aliases [:test] :quick-merge true)
        (boot-test/test)))
