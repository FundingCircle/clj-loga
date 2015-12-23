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
(require '[clj-loga.core :refer [init-logging set-tag]])

;; initialize formatter
(init-logging)

;; easily log with timbre
(timbre/info "Log it out.")

;; tag logs
(set-tag "smart-tag"
           (timbre/info "Log it tagged."))

;; handle exceptions
(timbre/error (Exception. "Something went wrong"))
```

## Features in progress
### Multiple tags
Currently, user can set one tag which will be merged with the default format. It would be benefitial if user can log multiple information tags in the business flow. It allows more granular filtering in log aggreagation tools.

```clojure
(set-tag "tracking-id" (:tracking-id msg) ;; (set-tag "tag-name" "tag-value")
  (process-message msg)                   ;; every log will contain the `tracking-id` tag
  (info "Finished processing message")) 
```

###Log with tag - *once*
Similar to multiple tags, but this will append the tag only to the specific log event. Next event will not include this tag.

```clojure
(log-with-tag "Processing event" "tracking-id" (:tracking-id msg)) ;; (log-with-tag "message" "tag-name" "value")
```

## License
 
Copyright © 2015 Funding Circle

Distributed under the BSD 3-Clause License.
