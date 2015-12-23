(defproject clj-loga "0.2.0"
  :description "Library for custom log formatting and other helpers leveraging Timbre"
  :url "https://github.com/FundingCircle/clj-loga"
  :license {:name "Eclipse Public License"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]                                              ;; JSON/JSONB encoding/decoding
                 [clj-time "0.11.0"]                                             ;; Joda Time wrapper
                 [com.taoensso/timbre "4.1.1"]                                   ;; Logging and profiling
                 [dire "0.5.3"]                                                  ;; Hooks
                 [environ "0.5.0"]                                               ;; Environment variables
                 ]
  :plugins [[lein-cljfmt  "0.3.0"]
            [lein-environ "1.0.1"]]
  :profiles {:test {:env {:enable-loga "true"}}})
