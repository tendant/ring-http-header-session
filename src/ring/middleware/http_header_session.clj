(ns ring.middleware.http-header-session
  (:require [ring.middleware.session.store :as store]
            [ring.middleware.session.memory :as mem]))

(defn- session-options
  [options]
  {:store (options :store (mem/memory-store))
   :header-name (options :header-name "x-http-header-session")})

(defn- session-request
  ([request]
   (session-request request {}))
  ([request {:keys [store header-name]}]
   (let [req-key (get-in request [:headers header-name])
         session (store/read-session store req-key)
         session-key (if session req-key)]
     (merge request {:session (or session {})
                     :session/key session-key}))))

(defn- session-response
  ([response request]
   (session-response response request {}))
  ([response {:keys [session-key] :as request} {:keys [store header-name] :as options}]
   (if response
     (let [new-session-key (if (contains? response :session)
                             (if-let [session (response :session)]
                               (if (:recreate (meta session))
                                 (do
                                   (store/delete-session store session-key)
                                   (->> (vary-meta session dissoc :recreate)
                                        (store/write-session store nil)))
                                 (store/write-session store session-key session))
                               (if session-key
                                 (store/delete-session store session-key))))]
       ;; No change to response for http-header-session
       response))))

(defn wrap-http-header-session
  "Reads in the current HTTP HEADERS, and adds it to the :session key on
  the request. If a :session key is added to the respsonse by the
  handler, the session is updated with the new value. If the value is
  nil, the session is deleted.

  Accepts the following options:

  :store  An implementation of the SessionStore protocol in the
  ring.middleware.session.store namespace. This determines how the
  session is stored. Defaults to in-memory storage using ring.middleware.session.store/memory-store.

  :header-name  The name of the http header that holds the session key. Defaults to `x-http-header-session`"

  ([handler]
   (wrap-http-header-session handler {}))
  ([handler options]
   (let [options (session-options options)]
     (fn
       ([request]
        (let [request (session-request request options)]
          (-> (handler request)
              (session-response request options))))
       ([request respond raise]
        (let [request (session-request request options)]
          (handler request
                   (fn [response]
                     (respond (session-response request options)))
                   raise)))))))