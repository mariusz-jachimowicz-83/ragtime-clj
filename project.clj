(defproject com.mjachimowicz/ragtime-clj "0.1.2"
  :description "Extension for ragtime that handles migrations as clj files"
  :url         "https://github.com/mariusz-jachimowicz-83/ragtime-clj"
  :license     {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure   "1.9.0-beta4"]
                 [ragtime               "0.7.2"]]

  :deploy-repositories [["clojars" {:sign-releases false}]]

  ;; lein cloverage --fail-threshold 95
  ;; lein kibit
  ;; lein eastwood
  :profiles {:dev {:dependencies [[org.clojure/java.jdbc "0.5.8"]
                                  [com.h2database/h2 "1.3.160"]]
                   :plugins [[lein-cloverage  "1.0.10"]
                             [lein-kibit      "0.1.6"]
                             [jonase/eastwood "0.2.5"]]}})
