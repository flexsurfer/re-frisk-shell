(ns re-frisk-shell.filter-parser-test
  (:require [cljs.test :refer [deftest is]]
            [re-frisk-shell.filter-parser :as sut]))

(deftest test-empty
  (is (= [] (sut/parse "")))
  (is (= [] (sut/parse "    "))))

(deftest test-clojure-literal
  (is (= [{:expr '(1 2)} (sut/parse "(1 2)")]))
  (is (= [{:expr '(1 2)} (sut/parse "   (1 2) ")]))
  (is (= [{:expr [1 2]}  (sut/parse "   [1 2] ")]))
  (is (= [{:expr :foo} (sut/parse "   :foo ")]))
  (is (= [{:expr '(2)} {:free "foobar"}] (sut/parse "( 2) foobar"))))

(deftest test-string
  (is (= [{:expr "123"}] (sut/parse "\"123\"")))
  (is (= [{:expr ""}] (sut/parse "\"\""))))

(deftest test-stringish
  (is (= [{:string-prefix "123"}] (sut/parse "\"123"))))

(deftest test-freeform
  (is (= [{:free ":"}] (sut/parse ":")))
  (is (= [{:free "0x0"}] (sut/parse "0x0")))
  (is (= [{:free "0argh"}] (sut/parse "0argh"))))

(deftest test-many
  (is (= [{:free ":"} {:expr [1]} {:string-prefix "123"}]
         (sut/parse ": [1] \"123"))))

(deftest test-full-path
  ;; If the whole expression is wrapped in [], parse it is a path
  ;; FIXME: maybe don't output [] for paths instead?
  (is (= [{:free "1"} {:free "2"} {:free "3"}] (sut/parse "[1 2 3]"))))
