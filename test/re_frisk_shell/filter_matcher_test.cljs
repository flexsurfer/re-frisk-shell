(ns re-frisk-shell.filter-matcher-test
  (:require [cljs.test :refer [deftest is]]
            [re-frisk-shell.filter-matcher :as sut]))

(deftest test-paths
  ;; Exact matches match
  (is (sut/match [{:expr :a} {:expr :b} {:expr :c}] [:a :b :c]))
  ;; Internal nodes and prefixes are skipped
  (is (sut/match [{:expr :a} {:expr :b} {:expr :c}] ["" :a :b :c]))
  (is (sut/match [{:expr :a} {:expr :b} {:expr :c}] ["" "" :a :b :c]))
  (is (sut/match [{:expr :a} {:expr :b} {:expr :c}] [:a "" :b "" :c]))
  ;; The last entry always has to match
  (is (not (sut/match [{:expr :a} {:expr :b} {:expr :c}] [:a :b :c ""]))))

(deftest test-expr
  ;; symbol matches itself
  (is (sut/match [{:expr 'foo}] ['foo]))
  ;; But nothing else
  (is (not (sut/match [{:expr 'foo}] ["foo"])))
  (is (not (sut/match [{:expr 'foo}] [:foo])))
  ;; keyword matches itself
  (is (sut/match [{:expr :foo}] [:foo]))
  ;; But nothing else
  (is (not (sut/match [{:expr :foo}] ["foo"])))
  (is (not (sut/match [{:expr :foo}] ['foo])))
  ;; character matches itself and string
  (is (sut/match [{:expr \n}] [\n]))
  (is (sut/match [{:expr \n}] ["n"]))
  ;; But nothing else
  (is (not (sut/match [{:expr \n}] ["nnn"])))
  (is (not (sut/match [{:expr \n}] ['n])))
  ;; string matches itself
  (is (sut/match [{:expr "foo"}] ["foo"]))
  ;; But nothing else
  (is (not (sut/match [{:expr "foo"}] ["foobar"])))
  (is (not (sut/match [{:expr "foo"}] [:foo])))
  (is (not (sut/match [{:expr "foo"}] ['foo])))
  ;; string matches itself
  (is (sut/match [{:expr "foo"}] ["foo"]))
  ;; But nothing else
  (is (not (sut/match [{:expr "foo"}] ["foobar"])))
  (is (not (sut/match [{:expr "foo"}] [:foo])))
  (is (not (sut/match [{:expr "foo"}] ['foo])))
  ;; set matches itself
  (is (sut/match [{:expr #{1}}] [#{1}]))
  ;; But nothing else
  (is (not (sut/match [{:expr #{1}}] [[1]])))
  (is (not (sut/match [{:expr #{1}}] ["1"])))
  ;; map matches itself
  (is (sut/match [{:expr {:a :b}}] [{:a :b}]))
  ;; but nothing else
  (is (not (sut/match [{:expr {:a :b}}] [[:a :b]])))
  (is (not (sut/match [{:expr {:a :b}}] [#{:a}])))
  (is (not (sut/match [{:expr {:a :b}}] ["{:a :b}"]))))

(deftest test-string-prefix
  ;; string prefix matches string prefixes
  (is (sut/match [{:string-prefix "abc"}] ["abc"]))
  (is (sut/match [{:string-prefix "abc"}] ["abcdef"]))
  ;; but not substrings
  (is (not (sut/match [{:string-prefix "abc"}] ["defabc"])))
  (is (not (sut/match [{:string-prefix "abc"}] ["defabcdef"])))
  ;; and not other types
  (is (not (sut/match [{:string-prefix "abc"}] [:abc])))
  (is (not (sut/match [{:string-prefix "abc"}] ['abc])))
  (is (not (sut/match [{:string-prefix "abc"}] [{:abc :abc}]))))

(deftest test-free
  ;; freeform match matches any substring
  (is (sut/match [{:free "a"}] ["a"]))
  (is (sut/match [{:free "a"}] ["bab"]))
  (is (sut/match [{:free "a"}] ['a]))
  (is (sut/match [{:free "a"}] ['bab]))
  (is (sut/match [{:free "a"}] [:a]))
  (is (sut/match [{:free "a"}] [[:a]]))
  (is (sut/match [{:free "a"}] [{:a 1}]))
  (is (sut/match [{:free "a"}] [{1 :a}]))
  (is (sut/match [{:free "a"}] [#{'a}]))
  (is (not (sut/match [{:free "a"}] ["b"])))
  (is (not (sut/match [{:free "a"}] [""])))
  (is (sut/match [{:free "17"}] [17]))
  )
