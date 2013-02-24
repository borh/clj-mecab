(ns clj-mecab.parse-test
  (:require [clojure.test :refer :all]
            [clj-mecab.parse :refer :all]))

(deftest parse-sentence-unidic-test
  (testing "Parsing with UniDic."
    (is (= (with-dictionary :unidic (parse-sentence "解析する"))
           [{:orth "解析"
             :fType "*"
             :iType "*"
             :lemma "解析"
             :cForm "*"
             :pos1 "名詞"
             :pos2 "普通名詞"
             :pos3 "サ変可能"
             :pron "カイセキ"
             :pos4 "*"
             :orthBase "解析"
             :cType "*"
             :goshu "漢"
             :lForm "カイセキ"
             :pronBase "カイセキ"
             :iForm "*"
             :fForm "*"}
            {:orth "する"
             :fType "*"
             :iType "*"
             :lemma "為る"
             :cForm "終止形-一般"
             :pos1 "動詞"
             :pos2 "非自立可能"
             :pos3 "*"
             :pron "スル"
             :pos4 "*"
             :orthBase "する"
             :cType "サ行変格"
             :goshu "和"
             :lForm "スル"
             :pronBase "スル"
             :iForm "*"
             :fForm "*"}]))))

(deftest parse-sentence-ipadic-test
  (testing "Parsing with IPAdic."
    (is (= (with-dictionary :ipadic (parse-sentence "解析する"))
           [{:orth "解析"
             :cForm "*"
             :pos1 "名詞"
             :pos2 "サ変接続"
             :pos3 "*"
             :pron "カイセキ"
             :pos4 "*"
             :kana "カイセキ"
             :orthBase "解析"
             :cType "*"}
            {:orth "する"
             :cForm "基本形"
             :pos1 "動詞"
             :pos2 "自立"
             :pos3 "*"
             :pron "スル"
             :pos4 "*"
             :kana "スル"
             :orthBase "する"
             :cType "サ変・スル"}]))))
