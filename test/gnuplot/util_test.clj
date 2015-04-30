(ns gnuplot.util-test
  (:require [gnuplot.util :as u]
            [clojure.test :refer :all]
            [byte-streams :as bs]))

(deftest strings->input-stream-test
  (let [rt (fn [s] "round-trip check"
             (is (= (apply str s)
                    (-> s u/strings->input-stream (bs/convert String)))))]
    (rt [])
    (rt ["a"])
    (rt ["abc"])
    (rt [(apply str (range 10000))])
    (rt (map str (range 5000)))))
