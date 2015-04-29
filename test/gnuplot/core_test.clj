(ns gnuplot.core-test
  (:require [clojure.test :refer :all]
            [gnuplot.core :as g]
            [clojure.pprint :refer [pprint]]))

(deftest simple-test
  (pprint (g/plot! [[:set :title "Simple Plots" :font ",20"]
                    [:set :key :left :box]
                    [:set :samples 50]
                    [:set :style :data :points]
                    [:plot (g/range -10 10) (g/list (g/lit "sin(x)")
                                                    (g/lit "atan(x)")
                                                    (g/lit "cos(atan(x))"))]]
                   [])))
