(ns clj-mecab.parse
  (:require [clojure.spec :as s]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.data.csv :as csv])
  (:import [net.moraleboost.mecab Lattice Node]
           [net.moraleboost.mecab.impl StandardTagger]))

;; ## Dictionary Auto-detection

(def dictionary-info
  (let [system-dic-dir (-> (shell/sh "mecab-config" "--dicdir")
                           :out
                           string/trim-newline)
        user-config (str (System/getProperty "user.home") "/.mecabrc")
        user-dic-dir (if (.exists (io/file user-config))
                       (->> user-config slurp (re-seq #"dicdir = (.*)/[^/]+dic/") first second))
        dic-dir (if (and user-dic-dir (.exists (io/file user-dic-dir)))
                  user-dic-dir
                  system-dic-dir)
        dics (seq (.list (io/file dic-dir)))]
    {:dic-dir dic-dir
     :dics (zipmap (map keyword dics) (map #(str dic-dir "/" %) dics))}))

(def dictionary-features
  {:ipadic [:pos-1 :pos-2 :pos-3 :pos-4 :c-type :c-form :orth-base :kana :pron]
   :unidic [:pos-1 :pos-2 :pos-3 :pos-4 :c-type :c-form :l-form :lemma :orth :pron :orth-base :pron-base :goshu :i-type :i-form :f-type :f-form]
   :unidic-MLJ [:pos-1 :pos-2 :pos-3 :pos-4 :c-type :c-form :l-form :lemma :orth :pron :kana :goshu :orth-base :pron-base :kana-base :form-base :i-type :i-form :i-con-type :f-type :f-form :f-con-type :a-type :a-con-type :a-mod-type]
   :unidic-EMJ [:pos-1 :pos-2 :pos-3 :pos-4 :c-type :c-form :l-form :lemma :orth :pron :kana :goshu :orth-base :pron-base :kana-base :form-base :i-type :i-form :i-con-type :f-type :f-form :f-con-type :a-type :a-con-type :a-mod-type]})

;; Bind initial values for tagger and features to a random found dictionary type.
(def ^:dynamic *tagger* (StandardTagger. (if (-> dictionary-info :dics seq) (str "-d " (-> dictionary-info :dics first second)) "")))
(def ^:dynamic *features* (get dictionary-features (-> dictionary-info :dics first first)
                               ;; If a dictionary format is not in the map, use a generic one:
                               (conj (lazy-seq (for [field (partition 2 (interleave (repeat "feature-") (range)))] (keyword (string/join field)))) :orth)))

(defmacro with-dictionary
  "Evaluates body with MeCab's dictionary set to dic-type keyword.
  Parses all features as keywords into a map."
  [dic-type & body]
  `(binding [*tagger* (StandardTagger. (str "-d " (get-in dictionary-info [:dics ~dic-type])))
             *features* (get dictionary-features ~dic-type *features*)]
     ~@body))

(defmacro with-dictionary-string
  "Evaluates body with MeCab's dictionary set to dic-dir.
  Tries to guess the dictionary type and parse all features as keywords into a map."
  [dic-dir & body]
  `(binding [*tagger* (StandardTagger. (str "-d " ~dic-dir))
             *features* (get dictionary-features
                             (condp re-seq ~dic-dir
                               #"(?i)ipadic" :ipadic
                               #"(?i)MLJ" :unidic-MLJ
                               #"(?i)(EMJ|CWJ)" :unidic-EMJ
                               #"(?i)unidic" :unidic)
                             *features*)]
     ~@body))

(defmacro with-dictionary-raw
  "Evaluates body with MeCab's dictionary set to dic-type keyword.
  Parses all features as keywords into a map."
  [dic-type dic-path & body]
  `(binding [*tagger* (StandardTagger. (str "-d " ~dic-path))
             *features* (get dictionary-features ~dic-type *features*)]
     ~@body))

;; ## Parse Functions

(defn parse-sentence
  "Returns parsed sentence as a vector of maps, each map representing the features of one morpheme."
  [s]
  (let [^Lattice lattice (.createLattice ^StandardTagger *tagger*)
        _ (.setSentence lattice s)
        _ (.parse ^StandardTagger *tagger* lattice)]
    (pop ; Discards EOS
     (loop [node (-> lattice (.bosNode) (.next))
            results []]
       (if-not node
         (do (.destroy lattice) ;; Prevent memory from leaking.
             results)
         (recur
          (.next node)
          (conj results
                (let [orth (.surface node)
                      morpheme (->> (.feature node)
                                    csv/read-csv
                                    first
                                    (zipmap *features*))]
                  (-> morpheme
                      (assoc :orth orth)
                      (update-in [:lemma] #(or % orth))
                      (update-in [:orth-base] #(or % orth)))))))))))

(s/fdef parse-sentence
  :args (s/cat :s string?)
  :ret (s/coll-of (s/map-of keyword? string?)))
