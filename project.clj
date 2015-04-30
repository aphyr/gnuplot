(defproject gnuplot "0.1.0-SNAPSHOT"
  :description "A Clojure interface to Gnuplot"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :java-source-paths ["src"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [byte-streams "0.2.0" :exclusions
                                   [riddley
                                    org.clojure/clojure]]
                                  [riddley "0.1.10"]]}})
