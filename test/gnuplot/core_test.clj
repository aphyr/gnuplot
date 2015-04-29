(ns gnuplot.core-test
  (:require [clojure.test :refer :all]
            [gnuplot.core :as g]
            [clojure.pprint :refer [pprint]]))

(deftest simple-test
  (g/raw-plot! [[:set :title "simple-test"]
                [:plot (g/range 0 5)
                 (g/list ["-" :title "rising" :with :lines]
                         ["-" :title "falling" :with :impulse])]]
               [[[0 0]
                 [1 1]
                 [2 2]
                 [3 1]
                 [4 3]
                 [5 4]]
                [[0 5]
                 [1 4]
                 [2 3]
                 [3 2]
                 [4 1]
                 [5 0]]]))
