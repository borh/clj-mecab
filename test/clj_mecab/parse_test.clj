(ns clj-mecab.parse-test
  (:require [clojure.test :refer :all]
            [clj-mecab.parse :refer :all]))

(deftest parse-sentence-unidic-test
  (testing "Parsing with UniDic."
    (is (= (with-dictionary :unidic (parse-sentence "解析する"))
           [{:orth "解析"
             :f-type "*"
             :i-type "*"
             :lemma "解析"
             :c-form "*"
             :pos-1 "名詞"
             :pos-2 "普通名詞"
             :pos-3 "サ変可能"
             :pron "カイセキ"
             :pos-4 "*"
             :orth-base "解析"
             :c-type "*"
             :goshu "漢"
             :l-form "カイセキ"
             :pron-base "カイセキ"
             :i-form "*"
             :f-form "*"}
            {:orth "する"
             :f-type "*"
             :i-type "*"
             :lemma "為る"
             :c-form "終止形-一般"
             :pos-1 "動詞"
             :pos-2 "非自立可能"
             :pos-3 "*"
             :pron "スル"
             :pos-4 "*"
             :orth-base "する"
             :c-type "サ行変格"
             :goshu "和"
             :l-form "スル"
             :pron-base "スル"
             :i-form "*"
             :f-form "*"}]))))

(deftest parse-sentence-ipadic-test
  (testing "Parsing with IPAdic."
    (is (= (with-dictionary :ipadic (parse-sentence "解析する"))
           [{:orth "解析"
             :c-form "*"
             :pos-1 "名詞"
             :pos-2 "サ変接続"
             :pos-3 "*"
             :pron "カイセキ"
             :pos-4 "*"
             :kana "カイセキ"
             :orth-base "解析"
             :lemma "解析"
             :c-type "*"}
            {:orth "する"
             :c-form "基本形"
             :pos-1 "動詞"
             :pos-2 "自立"
             :pos-3 "*"
             :pron "スル"
             :pos-4 "*"
             :kana "スル"
             :orth-base "する"
             :lemma "する"
             :c-type "サ変・スル"}]))))
