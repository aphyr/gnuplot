(defproject gnuplot "0.1.3"
  :description "A Clojure interface to Gnuplot"
  :url "http://github.com/aphyr/gnuplot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[byte-streams "0.2.5-alpha2"
                  :exclusions [riddley
                               org.clojure/clojure]]]
  :java-source-paths ["src"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.7.0"]
                                  [riddley "0.1.10"]]}})
