# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [0.5.0] - 27/03/2016
###Added
1. Enabled logging using loga directly providing wrappers around Timbre's logging macros. Supported log levels are: `:log, :trace, :debug, :info, :warn, :error, :fatal, :report`
0. Bump up Timbre version.

## [0.5.1] - 22/04/2016
1. Add support for `ex-info`. `ex-data` are accessible on `:exception-data` key as part of the log message.
2. Add error message to the exception message is accessible on `exception-message` key as part of the log message.

## [0.5.3] - 18/05/2016
1. Handle exception in processing log event.

## [0.6.0] - 25/05/2016
1. Log exception message to message if no message was supplied.

## [0.6.1] - 07/06/2016
1. Fallback to default timbre output function if exception thrown during processing logs.

## [0.7.0] - 28/02/2017
1. Update dependencies. 
