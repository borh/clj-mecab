(ns clj-mecab.parse
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.data.csv :as csv])
  (:import [net.moraleboost.mecab Lattice Node]
           [net.moraleboost.mecab.impl StandardTagger]
           [java.nio.file NoSuchFileException LinkOption]
           [java.io File]))

;; ## Dictionary auto-detection

(s/def :mecab/dictionary
  #{:ipadic :juman :unidic :unidic-neologd :unidic-cwj :unidic-csj :unidic-kindai
    :unidic-kyogen :unidic-kinsei :unidic-qkana :unidic-wakan :unidic-wabun :unidic-manyo})
(s/def :dictionary/dir (s/and string? #(.exists (io/file %))))
(s/def :dictionary/default :mecab/dictionary)
(s/def :dictionaries/path :dictionary/dir)
(s/def :dictionaries/dirs (s/map-of :mecab/dictionary :dictionary/dir))
(s/def :dictionaries/info
  (s/keys :req [:dictionaries/path :dictionary/default :dictionaries/dirs]))

(defn guess-dictionary [s]
  (if s
    (condp re-seq s
      #"(?i)ipadic" :ipadic
      #"(?i)juman" :juman
      #"(?i)unidic.*neologd.*" :unidic-neologd
      #"(?i)(MLJ|kindai)" :unidic-kindai
      #"(?i)(EMJ|kinsei)" :unidic-kinsei
      #"(?i)unidic.*cwj" :unidic-cwj
      #"(?i)unidic.*csj" :unidic-csj
      #"(?i)unidic.*kyogen" :unidic-kyogen
      #"(?i)unidic.*qkana" :unidic-qkana
      #"(?i)unidic.*wakan" :unidic-wakan
      #"(?i)unidic.*wabun" :unidic-wabun
      #"(?i)unidic.*manyo" :unidic-manyo
      #"(?i)unidic" :unidic
      ;; Fallback to UniDic if not autodetected, though this might be wrong.
      ;; In particular, Debian uses the 'default' directory.
      #"(?i)debian" :unidic
      :unidic)))

(defn canonicalize-path
  "Returns string of path will all symbolic links resolved."
  [s]
  (try
    (-> s io/file .toPath (.toRealPath (make-array LinkOption 0)) .toString)
    (catch NoSuchFileException _ nil)))

(defn validate-dic [s]
  (if s
    (if (clojure.set/superset? (into #{} (.list (io/file s)))
                               #{"char.bin" "matrix.bin" "sys.dic" "unk.dic" "dicrc"})
      s
      (println (format "'%s' is not a valid MeCab dictionary of type '%s', ignoring."
                       s (guess-dictionary s))))))

(defn extract-dictionaries-dir [s]
  (if s
    (->> s (re-seq #"dicdir = (.*)/[^/]+/?") first second canonicalize-path)))

(defn extract-default-dic [s]
  (if s
    (->> s (re-seq #"dicdir = (.*)/?") first second canonicalize-path validate-dic)))

(defn slurp-if-exists [^File s]
  (if (and (.exists s) (not (.isDirectory s)))
    (slurp s)))

(def dictionaries-info
  (let [binary-dics-dir (-> (shell/sh "mecab-config" "--dicdir")
                            :out
                            string/trim-newline
                            canonicalize-path)
        system-config (slurp-if-exists (io/file "/etc/mecabrc"))
        system-dics-dir (extract-dictionaries-dir system-config)
        system-dic (extract-default-dic system-config)
        system-dic-type (guess-dictionary system-dic)
        user-config (-> (System/getProperty "user.home") (io/file ".mecabrc") slurp-if-exists)
        user-dics-dir (extract-dictionaries-dir user-config)
        user-dic (extract-default-dic user-config)
        user-dic-type (guess-dictionary user-dic)

        valid-dic-dirs (cond-> []
                               (and binary-dics-dir (.isDirectory (io/file binary-dics-dir))) (conj binary-dics-dir)
                               (and system-dics-dir (.isDirectory (io/file system-dics-dir))) (conj system-dics-dir)
                               (and user-dics-dir (.isDirectory (io/file user-dics-dir))) (conj user-dics-dir))

        valid-dics (reduce
                     (fn [a dir]
                       (if-let [c-dir (validate-dic (canonicalize-path dir))]
                         (if (.isDirectory (io/file c-dir))
                           (let [dic-name (guess-dictionary c-dir)]
                             (assoc a dic-name c-dir))
                           a)
                         a))
                     {}
                     (mapcat (fn [dir] (seq (.listFiles (io/file dir))))
                             valid-dic-dirs))

        preferred-dic (condp get valid-dics
                        user-dic-type :>> user-dic-type
                        system-dic-type :>> system-dic-type
                        (or (some #(if (% valid-dics) %) [:unidic-cwj :unidic :unidic-csj :ipadic-utf8 :ipadic])
                            (ffirst valid-dics)))]
    {:dictionary/default preferred-dic
     :dictionaries/paths valid-dic-dirs
     :dictionaries/dirs  valid-dics}))

(def valid-dictionaries
  (set (keys (:dictionaries/dirs dictionaries-info))))

(s/def :mecab.features/pos-1 string?)
(s/def :mecab.features/pos-2 string?)
(s/def :mecab.features/pos-3 string?)
(s/def :mecab.features/pos-4 string?)
(s/def :mecab.features/c-type string?)
(s/def :mecab.features/c-form string?)
(s/def :mecab.features/l-form string?)
(s/def :mecab.features/lemma string?)
(s/def :mecab.features/orth string?)
(s/def :mecab.features/pron string?)
(s/def :mecab.features/orth-base string?)
(s/def :mecab.features/pron-base string?)
(s/def :mecab.features/goshu string?)
(s/def :mecab.features/i-type string?)
(s/def :mecab.features/i-form string?)
(s/def :mecab.features/f-type string?)
(s/def :mecab.features/f-form string?)
(s/def :mecab.features/i-con-type string?)
(s/def :mecab.features/f-con-type string?)
(s/def :mecab.features/type string?)
(s/def :mecab.features/kana string?)
(s/def :mecab.features/kana-base string?)
(s/def :mecab.features/form string?)
(s/def :mecab.features/form-base string?)
(s/def :mecab.features/a-type string?)
(s/def :mecab.features/a-con-type string?)
(s/def :mecab.features/a-mod-type string?)
(s/def :mecab.features/lid string?)
(s/def :mecab.features/lemma-id string?)
(s/def :mecab.features/position int?)

(s/def :mecab/morpheme
  (s/keys
    :req [:mecab.features/pos-1 :mecab.features/pos-2 :mecab.features/pos-3 :mecab.features/pos-4 :mecab.features/c-type :mecab.features/c-form :mecab.features/orth :mecab.features/orth-base]
    :opt [:mecab.features/pron :mecab.features/l-form :mecab.features/lemma :mecab.features/kana :mecab.features/goshu :mecab.features/pron-base :mecab.features/kana-base :mecab.features/form :mecab.features/form-base :mecab.features/i-type :mecab.features/i-form :mecab.features/i-con-type :mecab.features/f-type :mecab.features/f-form :mecab.features/f-con-type :mecab.features/type :mecab.features/a-type :mecab.features/a-con-type :mecab.features/a-mod-type :mecab.features/lid :mecab.features/lemma-id :mecab.features/position]))

(let [ipadic-features [:mecab.features/pos-1 :mecab.features/pos-2 :mecab.features/pos-3 :mecab.features/pos-4 :mecab.features/c-type :mecab.features/c-form :mecab.features/orth-base :mecab.features/kana :mecab.features/pron]
      unidic-21-features [:mecab.features/pos-1 :mecab.features/pos-2 :mecab.features/pos-3 :mecab.features/pos-4 :mecab.features/c-type :mecab.features/c-form :mecab.features/l-form :mecab.features/lemma :mecab.features/orth :mecab.features/pron :mecab.features/kana :mecab.features/goshu :mecab.features/orth-base :mecab.features/pron-base :mecab.features/kana-base :mecab.features/form-base :mecab.features/i-type :mecab.features/i-form :mecab.features/i-con-type :mecab.features/f-type :mecab.features/f-form :mecab.features/f-con-type :mecab.features/a-type :mecab.features/a-con-type :mecab.features/a-mod-type]
      unidic-22-features [:mecab.features/pos-1 :mecab.features/pos-2 :mecab.features/pos-3 :mecab.features/pos-4 :mecab.features/c-type :mecab.features/c-form :mecab.features/l-form :mecab.features/lemma :mecab.features/orth :mecab.features/pron :mecab.features/orth-base :mecab.features/pron-base :mecab.features/goshu :mecab.features/i-type :mecab.features/i-form :mecab.features/f-type :mecab.features/f-form :mecab.features/i-con-type :mecab.features/f-con-type :mecab.features/type :mecab.features/kana :mecab.features/kana-base :mecab.features/form :mecab.features/form-base :mecab.features/a-type :mecab.features/a-con-type :mecab.features/a-mod-type :mecab.features/lid :mecab.features/lemma-id]]
  (def dictionary-features
    {:ipadic        ipadic-features
     :unidic-cwj    unidic-22-features
     :unidic-csj    unidic-22-features
     :unidic-kindai unidic-21-features
     :unidic-kyogen unidic-21-features
     :unidic-kinsei unidic-21-features
     :unidic-qkana  unidic-21-features
     :unidic-wakan  unidic-21-features
     :unidic-wabun  unidic-21-features
     :unidic-manyo  unidic-21-features}))

;; ## Tagger wrappers

;; Bind initial values for tagger and features to a found dictionary type.
(def ^:dynamic *tagger*
  (StandardTagger. (if (-> dictionaries-info :dictionaries/dirs seq)
                     (str "-d " (get-in dictionaries-info
                                        [:dictionaries/dirs
                                         (:dictionary/default dictionaries-info)]))
                     "")))
(def ^:dynamic *features*
  (get dictionary-features (:dictionary/default dictionaries-info)
       ;; If a dictionary format is not in the map, use a generic one:
       (conj (lazy-seq
               (for [field (partition 2 (interleave (repeat "feature-") (range)))]
                 (keyword (string/join field))))
             :orth)))

(defmacro with-dictionary
  "Evaluates body with MeCab's dictionary set to dic-type keyword.
  Parses all features as keywords into a map."
  [dic-type & body]
  `(binding [*tagger* (StandardTagger.
                        (str "-d "
                             (get-in dictionaries-info [:dictionaries/dirs ~dic-type])))
             *features* (get dictionary-features ~dic-type *features*)]
     ~@body))

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

;; ## Parse functions

(defn parse-sentence
  "Returns parsed sentence as a vector of maps, each map representing the features of one morpheme."
  [s]
  (let [^Lattice lattice (.createLattice ^StandardTagger *tagger*)
        _ (.setSentence lattice s)
        _ (.parse ^StandardTagger *tagger* lattice)]
    (pop                                                    ; Discards EOS
      (loop [node (-> lattice (.bosNode) (.next))
             results []]
        (if-not node
          (do (.destroy lattice)                            ; Prevent memory from leaking.
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
                        (assoc :mecab.features/orth orth)
                        (update-in [:mecab.features/lemma] #(or % orth))
                        (update-in [:mecab.features/orth-base] #(or % orth)))))))))))

(s/fdef parse-sentence
  :args (s/cat :s string?)
  :ret (s/coll-of :mecab/morpheme))
