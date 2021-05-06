(defproject clj-loga "0.7.2"
  :description "Library for custom log formatting and other helpers leveraging Timbre"
  :url "https://github.com/FundingCircle/clj-loga"
  :license {:name "BSD 3-Clause License"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.10.0"]                    ;; JSON/JSONB encoding/decoding
                 [com.taoensso/timbre "4.10.0"]         ;; Logging and profiling
                 [environ "1.1.0"]                     ;; Environment variables
                 [robert/hooke "1.3.0"]                ;; Hooks
                 ]

  :repositories [["releases"
                  {:url "https://clojars.org/repo"
                   :sign-releases false
                   :username [:gpg :env/clojars_user]
                   :password [:gpg :env/clojars_password]}]]

  :plugins [[lein-cljfmt  "0.3.0"]
            [lein-environ "1.1.0"]]
  :profiles {:test {:env {:enable-loga "true"}}})
