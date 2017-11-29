# ring-http-header-session

Similar to ring session middleware, using HTTP Header, instead of HTTP Cookie.

## Installation

    [tendant/ring-http-header-session "0.1.0"]

## Usage

    (require '[ring.middleware.http-header-session :as http-header-session])
    (require '[ring.middleware.session.memory :as mem])
    
    (http-header-session/wrap-http-header-session app {:store (mem/memory-store)
                                                       :header-name "x-http-header-session"})

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
