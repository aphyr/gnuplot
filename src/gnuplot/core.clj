(ns gnuplot.core
  (:refer-clojure :exclude [format list range])
  (:require [clojure.core :as c]
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

(defn run!
  "Opens a new gnuplot process, runs the given command string, and feeds it the
  given input stream, waits for the process to exit, and returns a map of its
  `{:exit code, :out string, and :err string}`.

  Asserts that gnuplot exits with 0; if not, throws an ex-info like
  `{:type :gnuplot, :exit 123, :out ..., :err ...}`."
  [commands input]
;  (println "gnuplot data input:")
;  (println (bs/convert input String))
  (let [results (sh "gnuplot"
                    "-p"
                    "-e"      commands
                    :in       input
                    :out-enc  "UTF-8")]
    (if (zero? (:exit results))
      results
      (throw (ex-info (str "Gnuplot error: " (:err results))
                      (assoc results :type :gnuplot))))))

(def dataset-separator "\ne\n")


(defn generate-cmds [ commands]
  (->> commands
             (map format)
             (str/join ";\n"))
  )

(defn generate-datas [ datasets]
  (-> (mapcat (fn ds-format [dataset]
             (concat
               (->> dataset
                    (map (fn point-format [point]
                           (str/join " " point)))
                    (interpose "\n"))
               (c/list dataset-separator)))
           datasets)
      (u/strings->input-stream))
  )


(defn raw-plot!
  "Writes a plot! Takes a sequence of Commands, and a sequence of datasets,
  represented as a sequence of points, each of which is a sequence of numbers."
  ([commands datasets]
    (run!
        (generate-cmds commands)
        (generate-datas datasets)
      ))
   ([commands datasets x]
     (let [ cmds (generate-cmds commands)
            datas (generate-datas datasets)]
       (if (= x :debug)
          (println (str "CMD:\n" cmds "\nData:" datas))
          (run! cmds datas)
     )))
)
