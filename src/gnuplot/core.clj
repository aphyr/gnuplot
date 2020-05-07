(ns gnuplot.core
  (:refer-clojure :exclude [format list range run!])
  (:require [byte-streams :as bs]
            [clojure.core :as c]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gnuplot.util :as u]))

(defprotocol Command
  "Protocol for formatting things as Gnuplot commands."
  (format [x] "Format this thing as a Gnuplot command string."))

(extend-protocol Command
  clojure.lang.Symbol
  (format [x] (name x))

  clojure.lang.Keyword
  (format [x] (name x))

  String
  (format [x] (str "'" (str/replace x #"'" "\\'") "'"))

  clojure.lang.Seqable
  (format [x] (str/join " " (map format x)))

  Object
  (format [x] (str x)))

(defrecord Literal [^String s]
  Command
  (format [l] s))

(defn lit
  "A literal string, formatted exactly as it is."
  [s]
  (Literal. s))

(defrecord Range [lower upper]
  Command
  (format [r] (str "[" (format lower) ":" (format upper) "]")))

(defn range
  "A gnuplot range, formatted as [lower:upper]"
  [lower upper]
  (Range. lower upper))

(defrecord List [xs]
  Command
  (format [l] (str/join "," (map format xs))))

(defn list
  "A gnuplot comma-separated list, rendered as a,b,c"
  [& elements]
  (List. elements))

(defn take-elide
  "Like take, but if there are more than n elements, returns n elements,
  followed by '..."
  [n coll]
  (let [coll' (take (inc n) coll)]
    (if (< n (count coll'))
      (concat coll '...)
      coll)))

(defn run!
  "Opens a new gnuplot process and feeds it the given input stream, waits for
  the process to exit, and returns a map of its `{:exit code, :out string, and
  :err string}`.

  Asserts that gnuplot exits with 0; if not, throws an ex-info like
  `{:type :gnuplot, :exit 123, :out ..., :err ...}`."
  [input]
  ;(println "gnuplot data input:")
  ;(println (bs/convert input String))
  (let [results (sh "gnuplot"
                    "-p"
                    :in       input
                    :out-enc  "UTF-8")]
    (if (zero? (:exit results))
      (throw (ex-info (str "Gnuplot error:\n" (:err results)
                           "\n\nInput:\n"
                           (->> input
                                (bs/to-line-seq)
                                (take-elide 128)
                                (str/join "\n")))
                      (assoc results :type :gnuplot))))))

(def dataset-separator "\ne\n")

(defn raw-plot!
  "Writes a plot! Takes a sequence of Commands, and a sequence of datasets,
  represented as a sequence of points, each of which is a sequence of numbers."
  [commands datasets]
  (let [commands (->> commands
                      (map format)
                      (str/join ";\n"))
        datasets (mapcat (fn ds-format [dataset]
                           (concat
                             (->> dataset
                                  (map (fn point-format [point]
                                         (str/join " " point)))
                                  (interpose "\n"))
                             (c/list dataset-separator)))
                         datasets)]
    (try (run! (u/strings->input-stream (concat [commands ";\n"]
                                                datasets)))
         (catch clojure.lang.ExceptionInfo ex
           (let [e (:data (ex-data ex))]
             (if (= :gnuplot (:type e))
               (throw (ex-info (str "Gnuplot error:\n" (:err e)
                                    "\n\nCommands:\n"
                                    (->> commands
                                         (bs/to-line-seq)
                                         (take-elide 128)
                                         (str/join "\n"))
                                    "\n\nDatasets:\n"
                                    (->> datasets
                                         (bs/to-line-seq)
                                         (take-elide 16)
                                         (str/join "\n")))
                               e))))))))
