(ns doll-smuggler.core-test
  (:require [clojure.test :refer :all]
            [doll-smuggler.core :refer :all]))


(defn run_one_test[x]
   (is (= 
           (with-out-str (-main (str "testcases/" x ".input")))
           (slurp (str "testcases/" x ".output"))
)))

(deftest test_break_heuristics
  (testing "testcase that breaks greedy heuristic solvers"
    (run_one_test "break_heuristics")))

(deftest test_take_it_all
  (testing "everything fits in the bag"
    (run_one_test "take_it_all")))

(deftest test_empty_bag
  (testing "nothing fits in the bag"
    (run_one_test "empty_bag")))

(deftest test_given
  (testing "testcase given with the challenge"
    (run_one_test "given")))

(deftest test_rand7
  (testing "7 items"
    (run_one_test "rand7")))

(deftest test_rand13
  (testing "13 items"
    (run_one_test "rand13")))

(deftest test_rand26
  (testing "26 items"
    (run_one_test "rand26")))

(deftest test_small1
  (testing "a small test"
    (run_one_test "small1")))

(deftest test_small2
  (testing "another small test"
    (run_one_test "small2")))
