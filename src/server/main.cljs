(ns server.main
  ; Import NPM modules directly 
  ; https://shadow-cljs.github.io/docs/UsersGuide.html#npm
  (:require ["crypto" :as crypto])
  (:require ["http" :as http])
  (:require ["rhea" :as rhea]))

; This is standard ClojureScript code and the output should be run using Node.
;
; More details at https://shadow-cljs.github.io/docs/UsersGuide.html#target-node-script

(def connectionOptions {:host "127.0.0.1" :port 5672 :username "guest" :password "guest" :transport "tcp"})
(def conn (.create_connection rhea (clj->js connectionOptions)))

(defn send-message []
      (js/console.log "server.main send-message entry")
      (try
        ;(rhea/on (clj->js "sendable") (fn [context] (toastr/success "sendable")))
        ;(toastr/success "rhea on worked!")
        (.on conn (clj->js "connection_open") (fn [context] (js/console.log "event fired: connection_open")))
        (.on conn (clj->js "connection_close") (fn [context] (js/console.log "event fired: connection_close")))
        (.on conn (clj->js "connection_error") (fn [context] (js/console.log "event fired: connection_error")
                                                   (js/console.log context)))
        (.on conn (clj->js "protocol_error") (fn [context] (js/console.log "event fired: protocol_error")
                                                 (js/console.log context)))
        (.on conn (clj->js "error") (fn [context] (js/console.log "event fired: error")))
        (.on conn (clj->js "disconnected") (fn [context] (js/console.log "event fired: disconnected")))
        (.on conn (clj->js "settled") (fn [context] (js/console.log "event fired: settled")))
        (js/console.log "event handlers registered for connection")

        (.set_reconnect conn false)

        (.connect conn)

        (let [sender (.open_sender conn (clj->js "some.queue"))]
             (.on sender (clj->js "sendable") (fn [context] (js/console.log "event fired: sendable")))
             (.on sender (clj->js "accepted") (fn [context] (js/console.log "event fired: accepted")))
             (.on sender (clj->js "released") (fn [context] (js/console.log "event fired: released")))
             (.on sender (clj->js "rejected") (fn [context] (js/console.log "event fired: rejected")))
             (.on sender (clj->js "modified") (fn [context] (js/console.log "event fired: modified")))
             (.on sender (clj->js "sender_open") (fn [context] (js/console.log "event fired: sender_open")))
             (.on sender (clj->js "sender_draining") (fn [context] (js/console.log "event fired: sender_draining")))
             (.on sender (clj->js "sender_flow") (fn [context] (js/console.log "event fired: sender_flow")))
             (.on sender (clj->js "sender_error") (fn [context] (js/console.log "event fired: sender_error")))
             (.on sender (clj->js "sender_close") (fn [context] (js/console.log "event fired: sender_close")))
             (.on sender (clj->js "settled") (fn [context] (js/console.log "event fired: settled")))
             (js/console.log "sender created and handlers registered")
             (.send sender (clj->js { :body "Hello World!"}))
             (js/console.log "sender sending message to queue"))

        ;(.close conn)

        (js/console.log "server.main send-message exit")
        true

        (catch js/Error e
          (js/console.log "server.main send-message ERROR")
          (js/console.log e)
          false)))

(defn retrieve-message []
      (js/console.log "server.main retrieve-message entry")
      (try
        ;(rhea/on (clj->js "sendable") (fn [context] (toastr/success "sendable")))
        ;(toastr/success "rhea on worked!")
        (.on conn (clj->js "connection_open") (fn [context] (js/console.log "event fired: connection_open")))
        (.on conn (clj->js "connection_close") (fn [context] (js/console.log "event fired: connection_close")))
        (.on conn (clj->js "connection_error") (fn [context] (js/console.log "event fired: connection_error")))
        (.on conn (clj->js "protocol_error") (fn [context] (js/console.log "event fired: protocol_error")
                                                 (js/console.log context)))
        (.on conn (clj->js "error") (fn [context] (js/console.log "event fired: error")))
        (.on conn (clj->js "disconnected") (fn [context] (js/console.log "event fired: disconnected")))
        (.on conn (clj->js "settled") (fn [context] (js/console.log "event fired: settled")))
        (js/console.log "event handlers registered for connection")

        (.set_reconnect conn false)

        (.connect conn)

        (let [recv (.open_receiver conn (clj->js "some.queue"))]
             (.on recv (clj->js "message")
                  (fn [context]
                      (js/console.log "event fired: message")
                      (js/console.log (js->clj context))))

             (js/console.log "receiver created and handlers registered"))

        (js/console.log "server.main retrieve-message exit")
        true

        (catch js/Error e
          (js/console.log "server.main retrieve-message ERROR")
          (js/console.log e)
          false)))

(defn request-handler [req res]
      (let [url (.-url req)]
           (js/console.log url)
           (case url
                 "/send" (send-message)
                 "/retrieve" (retrieve-message)
                 (js/console.log "bad url")))

      (.end res "foo"))

; a place to hang onto the server so we can stop/start it
(defonce server-ref
         (volatile! nil))

(defn reload! []
  (println "Code updated."))


(defn -main []
      (js/console.log "starting server")
      (let [server (http/createServer #(request-handler %1 %2))]

           (.listen server 3000
                           (fn [err]
                               (if err
                                                 (js/console.error "server start failed")
                                                 (js/console.info "http server running"))))


           (vreset! server-ref server)))
