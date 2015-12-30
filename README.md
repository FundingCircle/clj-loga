# clj-loga [![Circle CI](https://circleci.com/gh/FundingCircle/clj-loga/tree/master.svg?style=svg)](https://circleci.com/gh/FundingCircle/clj-loga/tree/master)

Library for custom log formatting and other helpers leveraging Timbre.

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
(require '[clj-loga.core :refer [setup-loga set-log-tag operation-log-wrapper]])

;; initialize formatter
(setup-loga)

;; easily log with timbre
(timbre/info "Log it out.")

;; tag logs
(set-log-tag "smart-tag"
           (timbre/info "Log it tagged."))

;; handle exceptions
(timbre/error (Exception. "Something went wrong"))

;; Related forms can be grouped as an operation (e.g. event processing) and wrapped around with a log messages.
;; - macro *operation-log-wrapper* requires a map with a name of the operation e.g. {:operation "a-operation"}
;; - optional keys:
;;   - :tag - to tag log events
;;   - :pre-log-msg  - custom log message before body execution
;;   - :post-log-msg - custom log message after body execution

(operation-log-wrapper {:operation "processing message" :tag "smart-tag"}
                         (do (prn "executing all the work") :return-value))

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
