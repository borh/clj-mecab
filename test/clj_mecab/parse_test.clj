(ns clj-mecab.parse-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [orchestra.spec.test :as st]
            [clojure.spec.test.alpha :as stest]
            [expound.alpha :as expound]
            [clj-mecab.parse :refer :all]))

(st/instrument)
(stest/check (stest/enumerate-namespace 'clj-mecab.parse))
(alter-var-root #'s/*explain-out* (constantly (expound/custom-printer {:show-valid-values? true :print-specs? false :theme :figwheel-theme})))

(deftest parse-sentence-unidic-test
  (testing "Parsing with UniDic."
    (is (= (with-dictionary (if (-> dictionaries-info :dictionaries/dirs :unidic-cwj) :unidic-cwj :unidic)
                            (parse-sentence "解析する"))
           [#:mecab.features{:orth       "解析"
                             :f-type     "*"
                             :i-type     "*"
                             :lemma      "解析"
                             :c-form     "*"
                             :pos-1      "名詞"
                             :pos-2      "普通名詞"
                             :pos-3      "サ変可能"
                             :pron       "カイセキ"
                             :pos-4      "*"
                             :orth-base  "解析"
                             :c-type     "*"
                             :goshu      "漢"
                             :l-form     "カイセキ"
                             :pron-base  "カイセキ"
                             :i-form     "*"
                             :f-form     "*"
                             :a-con-type "C2",
                             :type       "体",
                             :i-con-type "*",
                             :kana-base  "カイセキ",
                             :lemma-id   "5785",
                             :lid        "1590177315299840",
                             :form-base  "カイセキ",
                             :f-con-type "*",
                             :form       "カイセキ",
                             :a-mod-type "*",
                             :a-type     "0",
                             :kana       "カイセキ"}
            #:mecab.features{:orth       "する"
                             :f-type     "*"
                             :i-type     "*"
                             :lemma      "為る"
                             :c-form     "終止形-一般"
                             :pos-1      "動詞"
                             :pos-2      "非自立可能"
                             :pos-3      "*"
                             :pron       "スル"
                             :pos-4      "*"
                             :orth-base  "する"
                             :c-type     "サ行変格"
                             :goshu      "和"
                             :l-form     "スル"
                             :pron-base  "スル"
                             :i-form     "*"
                             :f-form     "*"
                             :a-con-type "C5",
                             :type       "用",
                             :i-con-type "*",
                             :kana-base  "スル",
                             :lemma-id   "19537",
                             :lid        "5370298291593899",
                             :form-base  "スル",
                             :f-con-type "*",
                             :form       "スル",
                             :a-mod-type "*",
                             :a-type     "0",
                             :kana       "スル"}]))))

(deftest parse-sentence-ipadic-test
  (testing "Parsing with IPAdic."
    (is (= (with-dictionary (if (-> dictionaries-info :dictionaries/dirs :ipadic-utf8) :ipadic-utf8 :ipadic)
                            (parse-sentence "解析する"))
           [#:mecab.features{:orth      "解析"
                             :c-form    "*"
                             :pos-1     "名詞"
                             :pos-2     "サ変接続"
                             :pos-3     "*"
                             :pron      "カイセキ"
                             :pos-4     "*"
                             :kana      "カイセキ"
                             :orth-base "解析"
                             :lemma     "解析"
                             :c-type    "*"}
            #:mecab.features{:orth      "する"
                             :c-form    "基本形"
                             :pos-1     "動詞"
                             :pos-2     "自立"
                             :pos-3     "*"
                             :pron      "スル"
                             :pos-4     "*"
                             :kana      "スル"
                             :orth-base "する"
                             :lemma     "する"
                             :c-type    "サ変・スル"}]))))

(deftest spec-test
  (testing "Sentence parsing spec including OOV for all dictionaries"
    (doseq [dic (keys (:dictionaries/dirs dictionaries-info))]
      (is (s/valid? (s/coll-of :mecab/morpheme)
                    (with-dictionary dic (parse-sentence "解析する❥．")))))))
