(defproject clj-loga "0.5.2"
  :description "Library for custom log formatting and other helpers leveraging Timbre"
  :url "https://github.com/FundingCircle/clj-loga"
  :license {:name "BSD 3-Clause License"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]                    ;; JSON/JSONB encoding/decoding
                 [clj-time "0.11.0"]                   ;; Joda Time wrapper
                 [com.taoensso/timbre "4.3.1"]         ;; Logging and profiling
                 [environ "0.5.0"]                     ;; Environment variables
                 [robert/hooke "1.3.0"]                ;; Hooks
                 ]
  :repositories [["releases"
                  {:url "https://clojars.org/repo"
                   :sign-releases false
                   :username [:gpg :env/clojars_user]
                   :password [:gpg :env/clojars_password]}]
                 ["snapshots"
                  {:url "https://clojars.org/repo"
                   :sign-releases false
                   :username [:gpg :env/clojars_user]
                   :password [:gpg :env/clojars_password]}]]
  :plugins [[lein-cljfmt  "0.3.0"]
            [lein-environ "1.0.1"]
            [test2junit "1.1.2"]]
  :test2junit-output-dir ~(or (System/getenv "CIRCLE_TEST_REPORTS") "target/test2junit")
  :profiles {:test {:env {:enable-loga "true"}}})
