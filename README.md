# clj-loga [![Circle CI](https://circleci.com/gh/FundingCircle/clj-loga/tree/master.svg?style=svg)](https://circleci.com/gh/FundingCircle/clj-loga/tree/master)

Standing on the shoulders of the great logging library [Timbre](https://github.com/ptaoussanis/timbre/), loga extends Timbre's functionality by applying following features:
- custom json log appender - allows convenient way of parsing logs for log aggregators
- tagging log messages - allows to aggregate and track log events across multiple services based on the log
- obfuscation - prevent sensitive data to be readable in logs

Loga also provides wrappers around Timbre's logging macros to allow logging directly from the library. It removes the need to use Timbre in the application directly, thus reducing risk of conflicts between version of Timbre used in Logs and in the application.

Supports Timbre >= 4.1.1.

## Usage

### Installation

[![Clojars Project](http://clojars.org/clj-loga/latest-version.svg)](http://clojars.org/clj-loga)

### Environment
Loga features are mainly to benefit logging aggregators and its use makes logs less readable and inconvenient in development. Hence, it is required to allow loga explicitly.

```bash
# export env variable to apply formatting
ENABLE_LOGA=true
```

### REPL

```clojure
(require '[clj-loga.core :as loga :refer [setup-loga set-log-tag log-wrapper]])

;; initialize formatter
(setup-loga)

;; by default the log level is set to INFO. Lower levels will not be logged
;; - to specify custom log level pass it as a key value in setup
(setup-loga :level :debug)

;; Logging using Loga
;; supported levels:
;;   - log trace debug info warn error fatal
(loga/info "my log message")

;; obfuscate sensitive keys
(setup-loga :obfuscate [:password])

;; log worry-free
(timbre/info "Log event with params"
  {:password "secret" :bar "baz" :sub {:password "secret" :foo "bar"}})
;; =>
;; {"level":"INFO","timestamp":"2016-01-13T10:31:17.126Z","namespace":"clj-loga.core",
;; "message":"Log event with params {:password \"[FILTERED]\", :bar \"baz\", :sub {:password \"[FILTERED]\", :foo \"bar\"}}"}"\"}\"}"}

;; easily log with timbre
(timbre/info "Log it out.")

;; tag logs
(set-log-tag "smart-tag"
           (timbre/info "Log it tagged."))

;; handle exceptions
(timbre/error (Exception. "Something went wrong"))

;; Related forms can be wrapped with log messages.
;; - macro log-wrapper accepts a map with following keys:
;; - optional keys:
;;   - :tag - to tag log events
;;   - :pre-log-msg  - custom log message before body execution
;;   - :post-log-msg - custom log message after body execution
;;   - :operation - descriptive name for the wrapped forms

;; custom basic application:
(log-wrapper {:pre-log-msg "started processing kafka message"
              :post-log-msg "finished processing kafka message"
              :tag "message id"}
              (do
                (prn "all the work happening now")
                "return value"))

;; =>
;; {"timestamp":"2015-12-31T11:25:57.598Z","level":"INFO","message":"started processing kafka message","namespace":"clj-loga.core","tag":"message id"}
;; "all the work happening now"
;; {"timestamp":"2015-12-31T11:25:57.599Z","level":"INFO","message":"finished processing kafka message","namespace":"clj-loga.core","tag":"message id"}

;; or use defaults:
(log-wrapper {:operation "processing message" :tag "smart-tag"}
             (do (prn "executing all the work") :return-value))

;; =>
;; {"timestamp":"2015-12-31T11:19:28.146Z","level":"INFO","message":"started: processing message","namespace":"clj-loga.core","tag":"some-tag"}
;; '"all the work happening now"
;; {"timestamp":"2015-12-31T11:19:28.150Z","level":"INFO","message":"finished: processing message","namespace":"clj-loga.core","tag":"some-tag"}
```
**AOP logging with function metadata**

As an alternative, it is possible to use function metadata to log instead of cluttering the code with explicit logging calls. By default, the library will search in all the namespaces for target functions.

```Clojure
(require [clj-loga.hooks :as h])

(defn ^{::h/operation "executing function" ::h/tag "some-tag"}
    my-target-function [argument]
    (prn "hello from target function"))

(my-target-function "test")
;; =>
;; {"timestamp":"2015-12-31T11:19:28.146Z","level":"INFO","message":"started: executing function","namespace":"clj-loga.core","tag":"some-tag"}
;; '"hello from target function"
;; {"timestamp":"2015-12-31T11:19:28.150Z","level":"INFO","message":"finished: executing function","namespace":"clj-loga.core","tag":"some-tag"}

(defn ^{::h/pre-log-msg "executing function" ::h/post-log-msg "finished" ::h/tag "some-tag"}
    another-target-function [argument]
    (prn "target function logging arguments"))

(another-target-function {:a 1 :b 2})
;; =>
;; {"timestamp":"2015-12-31T11:19:28.146Z","level":"INFO","message":"executing function {:a 1 :b 2}","namespace":"clj-loga.core","tag":"some-tag"}
;; '"target function logging arguments"
;; {"timestamp":"2015-12-31T11:19:28.150Z","level":"INFO","message":"finished","namespace":"clj-loga.core","tag":"some-tag"}

;; narrow down target function search by specifying the namespaces
(setup-loga :level :debug :namespaces ["my-ns.*" "another-ns.core"])
```

## Features in progress
### Multiple tags
Currently, user can set one tag which will be merged with the default format. It would be benefitial if user can log multiple information tags in the business flow. It allows more granular filtering in log aggreagation tools.

```clojure
(process-message msg)
```

###Log with tag - *once*
Similar to multiple tags, but this will append the tag only to the specific log event. Next event will not include this tag.

```clojure
(set-log-tag "Processing event" "tracking-id" (:tracking-id msg)) ;; (set-log-tag "message" "tag-name" "value")
```

## License
 
Copyright © 2015 Funding Circle

Distributed under the BSD 3-Clause License.
