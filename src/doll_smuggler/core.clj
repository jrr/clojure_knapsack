(ns doll-smuggler.core
  (:gen-class)
  (:require [clojure.math.combinatorics])
)

(declare solve_exhaustive)
(declare greedy_load)
(declare solve_recur)
(declare print_result)

(defn -main
  "solve a knapsack-problem file"
  [& args]
  (if (= 0 (count args)) (do
    (println "usage: lein run [-v] file.input")
    (System/exit 1)
  ))

  (def filename (last args))
  (def verbose (= 2 (count args)))


  (def whole-test-file (slurp filename))
  ;(println "testcase:\n" whole-test-file "\n========\n")

  ; match the max weight, which happens to be the first integer in the file:
  (def max_weight (Integer. (re-find #"\d+" whole-test-file)))

  ; re-seq to build a sequence of vectors, matching each doll line in the file:
  (def dolls (re-seq #"(\w+)\s+(\d+)\s+(\d+)" whole-test-file))

  ; that included a column of the complete match, e.g. ["luke        9   150" "luke" "9" "150"]
  ; so here I discard the first column:
  (def dolls (map (fn[x](subvec x 1)) dolls))

  ; columns two and three are strings, but we want integers
  (def dolls (map (fn[x](vec [  (get x 0)
                                (Integer. (get x 1)) 
                                (Integer. (get x 2))])) dolls))

  ; I started out sorting by column num (e.g., (sort-by second > dolls) ), 
  ; then discovered how to name things and reference them symbolically:
  (def dolls (map (fn[[n w v]] {:name n :weight w :value v}) dolls))

  (defn total_weight[collection]
    (reduce + (map :weight collection)))

  (defn total_value[collection]
    (reduce + (map :value collection)))

  (defn under_budget[collection]
    (true? (<= (total_weight collection) max_weight)))

  (defn summarize_soln[s]
    (str "$" (total_value s) (str " (" (str (total_weight s)) ")")))

  (if (true? verbose) (do

    (println "max_weight:   " max_weight)

    (println "total weight: " (total_weight dolls))

    (println "total value:  " (total_value dolls))

    (println)

    (def dolls_by_weight (sort-by :weight < dolls))
    (def soln (greedy_load dolls_by_weight under_budget max_weight))
    (println "by weight:         " (summarize_soln soln))

    (def dolls_by_value (sort-by :value > dolls))
    (def soln (greedy_load dolls_by_value under_budget max_weight))
    (println "by value:          " (summarize_soln soln))

    (defn eff[d] (/ (get d :value) (get d :weight)))
    (def dolls_by_efficiency (sort-by eff > dolls))
    (def soln (greedy_load dolls_by_efficiency under_budget max_weight))
    (println "by efficiency:     " (summarize_soln soln))

    ; only execute exhaustive solver if testcase is small:
    (if (< (count dolls) 8) (do
      (def soln (solve_exhaustive dolls under_budget))
      (println "exhaustively:      " (summarize_soln soln))
    ))

  ))

  (def soln (solve_recur dolls max_weight))

  (if (true? verbose) (do
    (println "recursive solution:" (summarize_soln soln))
    (println)
  ))

  (print_result soln)

)

(defn print_result[dolls]
  (println "packed dolls:")
  (println)

  ; there's gotta be a better way to indent columns..
  (def max_name_len (count "name "))
  (doseq [d dolls]
     (def this_name_len (count (get d :name)))
     (def max_name_len (if(> this_name_len max_name_len) this_name_len max_name_len))
  )

  ; from http://gettingclojure.wikidot.com/cookbook:strings :
  (defn make-space[n]
    (apply str (repeat n \space)))

  (println (str "name" (make-space (- max_name_len (count "name"))) "weight value"))
  (doseq [d dolls]
    (print (get d :name))
    (def numspaces (- max_name_len (count (get d :name))))
    (print (make-space numspaces)) ; indent out to the longest name
    (printf " %4d" (get d :weight))
    (printf "  %4d" (get d :value))
    (println)
  )
)

(defn solve_exhaustive[dolls under_budget]

  ; Brute force: I want every possible subset. Google tells me to use math.combinatorics:
  ; https://stackoverflow.com/questions/15146784/clojure-permutations-of-subsets/15146913#15146913
  ; https://github.com/clojure/math.combinatorics
  ; (added dep to leiningen project)

  (def all_subsets (clojure.math.combinatorics/subsets dolls))

  ;(println (str (count all_subsets)) "subsets:\n")
  ;(doseq [s all_subsets]
  ;  (doseq [t s]
  ;    (println (str t))
  ;  )
  ;  (println "under budget?:" (str (under_budget s)))
  ;  (println "")
  ;)

  (def under_budget_combos (filter under_budget all_subsets))

  ;(println (count under_budget_combos) "under budget combos.")

  (def under_budget_combos (sort-by total_value > under_budget_combos))

  ;(doseq [s under_budget_combos]
  ;  (doseq [t s]
  ;    (println (str t)))
  ;  (println "")
  ;)

  (first under_budget_combos)

)

; load the knapsack starting from the top of the list of items
(defn greedy_load[dolls,under_budget,budget]

  (defn total_weight[collection]
    (reduce + (map :weight collection)))

  ; grab "affordable" items from l2 until there are no more
  (defn grab[l1 l2]
    (def remaining_budget (- budget (total_weight l1)))
    ;(println "remaining budget:" (str remaining_budget) ", remaining items:" (str (count l2)))
    (def affordable_items (filter (fn[x] (<= (get x :weight) remaining_budget)) l2))
    ;(println "affordable items:" (str (count affordable_items)))
    (if (= (count affordable_items) 0) 
      l1
      (do 
        (def next_item (first affordable_items))
        (grab (conj l1 next_item) (remove #(= next_item %) l2))
      )
    )
  )

  (def asdf (seq nil))
  (grab asdf dolls)

)

(defn maxval[a b]
  (defn total_value[collection]
    (reduce + (map :value collection)))

  ;(println "maxval " (str (total_weight a)) "," (str (total_weight b)))

  (if (> (total_value a) (total_value b)) a b)
)

; 0/1 dynamic programming solution based on
; http://en.wikipedia.org/wiki/Knapsack_problem#0.2F1_knapsack_problem
(defn solve_recur[dolls,budget]
  ;(print "solve_recur ")
  ;(doseq [d dolls] (print (get d :name) " "))
  ;(println (str budget))

  ; if number of dolls is 0, return that empty set; else
  (if (= 0 (count dolls)) dolls (do

    ; if the last item is individually over budget, drop it; else
    (if (> (get (last dolls) :weight) budget) (solve_recur (butlast dolls) budget) (do


      (def ret (maxval ; maximum total value of these two sets:

        ; same budget, all dolls but the last
        ;(do (print "  L ") 
        (solve_recur (butlast dolls) budget)
        ;budget) )

        ; union of this doll and the best-of-everything-but-this-doll
        ;(do (print "  R ")
        ;(print "[remaining budget " (str  (- budget (get (last dolls) :weight))) "]")
        (conj
          (solve_recur (butlast dolls) (- budget (get (last dolls) :weight)))
          (last dolls)
        )
        ;)
      ))
      ;(print " .. ")(doseq [d ret] (print (get d :name) " "))(println)

      ret

    ))

  ))
)
