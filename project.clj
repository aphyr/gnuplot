(defproject gnuplot "0.1.0-SNAPSHOT"
  :description "A Clojure interface to Gnuplot"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[byte-streams "0.2.0" :exclusions [riddley
                                                    org.clojure/clojure]]
                 [riddley "0.1.10"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]]}})
