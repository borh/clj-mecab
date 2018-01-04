(ns clj-mecab.parse
  (:require [clojure.spec.alpha :as s]
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
        user-config (str (io/file (System/getProperty "user.home") ".mecabrc"))
        user-dic-dir (if (.exists (io/file user-config))
                       (->> user-config slurp (re-seq #"dicdir = (.*)/[^/]+/") first second))
        dic-dir (if (and user-dic-dir (.exists (io/file user-dic-dir)))
                  user-dic-dir
                  system-dic-dir)
        dics (seq (.list (io/file dic-dir)))]
    {:dic-dir dic-dir
     :default-dic :unidic-cwj
     :dics (zipmap (map keyword dics) (map #(str (io/file dic-dir %)) dics))}))

(s/def ::pos-1 string?)
(s/def ::pos-2 string?)
(s/def ::pos-3 string?)
(s/def ::pos-4 string?)
(s/def ::c-type string?)
(s/def ::c-form string?)
(s/def ::l-form string?)
(s/def ::lemma string?)
(s/def ::orth string?)
(s/def ::pron string?)
(s/def ::orth-base string?)
(s/def ::pron-base string?)
(s/def ::goshu string?)
(s/def ::i-type string?)
(s/def ::i-form string?)
(s/def ::f-type string?)
(s/def ::f-form string?)
(s/def ::i-con-type string?)
(s/def ::f-con-type string?)
(s/def ::type string?)
(s/def ::kana string?)
(s/def ::kana-base string?)
(s/def ::form string?)
(s/def ::form-base string?)
(s/def ::a-type string?)
(s/def ::a-con-type string?)
(s/def ::a-mod-type string?)
(s/def ::lid string?)
(s/def ::lemma-id string?)

(s/def ::morpheme
  (s/keys
   :req [::pos-1 ::pos-2 ::pos-3 ::pos-4 ::c-type ::c-form ::orth ::orth-base ::pron]
   :opt [::l-form ::lemma ::kana ::goshu ::pron-base ::kana-base ::form ::form-base ::i-type ::i-form ::i-con-type ::f-type ::f-form ::f-con-type ::type ::a-type ::a-con-type ::a-mod-type ::lid ::lemma-id]))

(let [ipadic-features [:pos-1 :pos-2 :pos-3 :pos-4 :c-type :c-form :orth-base :kana :pron]
      unidic-21-features [:pos-1 :pos-2 :pos-3 :pos-4 :c-type :c-form :l-form :lemma :orth :pron :kana :goshu :orth-base :pron-base :kana-base :form-base :i-type :i-form :i-con-type :f-type :f-form :f-con-type :a-type :a-con-type :a-mod-type]
      unidic-22-features [:pos-1 :pos-2 :pos-3 :pos-4 :c-type :c-form :l-form :lemma :orth :pron :orth-base :pron-base :goshu :i-type :i-form :f-type :f-form :i-con-type :f-con-type :type :kana :kana-base :form :form-base :a-type :a-con-type :a-mod-type :lid :lemma-id]]
  (def dictionary-features
    {:ipadic ipadic-features
     :ipadic-utf8 ipadic-features
     :unidic-cwj unidic-22-features
     :unidic-csj unidic-22-features
     :unidic-kindai unidic-21-features
     :unidic-kyogen unidic-21-features
     :unidic-kinsei unidic-21-features
     :unidic-qkana unidic-21-features
     :unidic-wakan unidic-21-features
     :unidic-wabun unidic-21-features
     :unidic-manyo unidic-21-features}))

;; Bind initial values for tagger and features to a random found dictionary type.
(def ^:dynamic *tagger* (StandardTagger. (if (-> dictionary-info :dics seq) (str "-d " (-> dictionary-info :dics first second)) "")))
(def ^:dynamic *features* (get dictionary-features (-> dictionary-info :default-dic)
                               ;; If a dictionary format is not in the map, use a generic one:
                               (conj (lazy-seq (for [field (partition 2 (interleave (repeat "feature-") (range)))] (keyword (string/join field)))) :orth)))

(defmacro with-dictionary
  "Evaluates body with MeCab's dictionary set to dic-type keyword.
  Parses all features as keywords into a map."
  [dic-type & body]
  `(binding [*tagger* (StandardTagger. (str "-d " (get-in dictionary-info [:dics ~dic-type])))
             *features* (get dictionary-features ~dic-type *features*)]
     ~@body))

(defn guess-dictionary [s]
  (condp re-seq s
    #"(?i)ipadic" :ipadic
    #"(?i)juman" :juman
    #"(?i)(MLJ|kindai)" :unidic-kindai
    #"(?i)(EMJ|kinsei)" :unidic-kinsei
    #"(?i)unidic.*cwj" :unidic-cwj
    #"(?i)unidic.*csj" :unidic-csj
    #"(?i)unidic.*kyogen" :unidic-kyogen
    #"(?i)unidic.*qkana" :unidic-qkana
    #"(?i)unidic.*wakan" :unidic-wakan
    #"(?i)unidic.*wabun" :unidic-wabun
    #"(?i)unidic.*manyo" :unidic-manyo
    #"(?i)unidic" :unidic-cwj))

(defmacro with-dictionary-string
  "Evaluates body with MeCab's dictionary set to dic-dir.
  Tries to guess the dictionary type and parse all features as keywords into a map."
  [dic-dir & body]
  `(binding [*tagger* (StandardTagger. (str "-d " ~dic-dir))
             *features* (get dictionary-features
                             (guess-dictionary ~dic-dir)
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
  :ret (s/coll-of ::morpheme))
