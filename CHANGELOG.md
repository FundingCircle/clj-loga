# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [0.5.0] - 27/03/2016
###Added
1. Enabled logging using loga directly providing wrappers around Timbre's logging macros. Supported log levels are: `:log, :trace, :debug, :info, :warn, :error, :fatal, :report`
0. Bump up Timbre version.

## [0.5.1] - 22/04/2016
1. Add support for `ex-info`. `ex-data` are accessible on `:exception-data` key as part of the log message.
2. Add error message to the exception message is accessible on `exception-message` key as part of the log message
