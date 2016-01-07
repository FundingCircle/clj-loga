# clj-loga [![Circle CI](https://circleci.com/gh/FundingCircle/clj-loga/tree/master.svg?style=svg)](https://circleci.com/gh/FundingCircle/clj-loga/tree/master)

Library for custom log formatting and other helpers leveraging [Timbre](https://github.com/ptaoussanis/timbre/).

Supports logging with timbre >= 4.1.1.

## Usage

**Installation**

[![Clojars Project](http://clojars.org/clj-loga/latest-version.svg)](http://clojars.org/clj-loga)

**Environment**

```bash
#export env variable to apply formatting
ENABLE_LOGA=true
```

**Repl**

```clojure
(require '[clj-loga.core :refer [setup-loga set-log-tag log-wrapper]])

;; initialize formatter
(setup-loga)

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
