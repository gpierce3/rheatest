(ns server.main
  ; Import NPM modules directly 
  ; https://shadow-cljs.github.io/docs/UsersGuide.html#npm
  (:require ["crypto" :as crypto])
  (:require ["http" :as http])
  (:require ["rhea" :as rhea]))

; This is standard ClojureScript code and the output should be run using Node.
;
; More details at https://shadow-cljs.github.io/docs/UsersGuide.html#target-node-script

;
; gwp - qpid supports anonymous authentication, username and pw are not need
;       if you use it, you'll get an error with the current code
;       Need to play around with SASL auth mode PLAIN.  I didn't mess around with it
;
;(def connectionOptions {:host "127.0.0.1" :port 5672 :username "guest" :password "guest" :transport "tcp"})
(def connectionOptions {:host "127.0.0.1" :port 5672 :transport "tcp"})

;
; gwp - 'rhea' is main entrypoint into the rhea lib.
;       It is a javascript object called a container
;       This line is creating a Connection js object, NOT doing the actual connection yet.
;
(def conn (.create_connection rhea (clj->js connectionOptions)))

;
; gwp - This line of code creates a Sender js object we'll use to send messages to the broker.
;       Note the broker will need to have a queue named 'some' already set up.
;       I also experimented with creating the sender down in the 'let' code below.  No difference.
;
(def sender (.open_sender conn (clj->js "some")))

;
; gwp - This is a time delay function I wrote to slow things down so I could jump to qpid manager and
;       check on connections, etc.
;
(defn delayMe [mil]
      (let [delayEnd (+(.now js/Date) mil)]
           ;(js/console.log (.now js/Date))
           ;(js/console.log delayEnd)
           (while (< (.now js/Date) delayEnd))))
           ;(js/console.log (.now js/Date))))

(defn send-message []
      (js/console.log "server.main send-message entry")
      (try
        ;
        ; gwp - wiring up event handlers so we can see what happens
        ;       Tried wiring the 'open' handler to the conn object too.  No difference.
        ;
        (.on rhea (clj->js "connection_open") (fn [context] (js/console.log "event fired: connection_open")))
        (.on conn (clj->js "connection_close") (fn [context] (js/console.log "event fired: connection_close")))
        (.on conn (clj->js "connection_error") (fn [context] (js/console.log "event fired: connection_error")
                                                   (js/console.log context)))
        (.on conn (clj->js "protocol_error") (fn [context] (js/console.log "event fired: protocol_error")
                                                 (js/console.log context)))
        (.on conn (clj->js "error") (fn [context] (js/console.log "event fired: error")))
        (.on conn (clj->js "disconnected") (fn [context] (js/console.log "event fired: disconnected")))

        (.on conn (clj->js "settled") (fn [context] (js/console.log "event fired: settled")))
        (js/console.log "event handlers registered for connection")

        ;
        ; gwp - Comment this out once things are working.
        ;       Otherwise you'll get an unending stream of reconnect attempts and disconnect events getting fired.
        ;
        (.set_reconnect conn false)

        ;(js/console.log rhea)
        (js/console.log "BEFORE CONNECT: ")
        ;(js/console.log conn)

        ;
        ; gwp - here's where we try the actual connection
        ;
        (.connect conn)
        (delayMe 5000)
        (js/console.log "AFTER CONNECT: ")
        ;(js/console.log conn)
        ;(delayMe 15000)

        (let [
              testmsg (.-message rhea)]
             ;
             ; gwp - uncomment these to wire in event handlers to the Sender object.
             ;       None of these fired when I hooked them.  I believe because the sending is blocked.
             ;
             ;(.on sender (clj->js "sendable") (fn [context] (js/console.log "event fired: sendable")))
             ;(.on sender (clj->js "accepted") (fn [context] (js/console.log "event fired: accepted")))
             ;(.on sender (clj->js "released") (fn [context] (js/console.log "event fired: released")))
             ;(.on sender (clj->js "rejected") (fn [context] (js/console.log "event fired: rejected")))
             ;(.on sender (clj->js "modified") (fn [context] (js/console.log "event fired: modified")))
             ;(.on sender (clj->js "sender_open") (fn [context] (js/console.log "event fired: sender_open")))
             ;(.on sender (clj->js "sender_draining") (fn [context] (js/console.log "event fired: sender_draining")))
             ;(.on sender (clj->js "sender_flow") (fn [context] (js/console.log "event fired: sender_flow")))
             ;(.on sender (clj->js "sender_error") (fn [context] (js/console.log "event fired: sender_error")))
             ;(.on sender (clj->js "sender_close") (fn [context] (js/console.log "event fired: sender_close")))
             ;(.on sender (clj->js "settled") (fn [context] (js/console.log "event fired: settled")))
             ;(js/console.log "sender created and handlers registered")
             ;(js/console.log sender)
             ;(js/console.log testmsg)

             ;
             ; gwp - the actual send of a message.
             ;       Added the routing key in subject.  No difference.
             ;
             (.send sender (clj->js { :subject "some.binding" :body "Hello World!"}))
             (js/console.log "sender sending message to queue"))

        ;(.close conn)

        (js/console.log "server.main send-message exit")
        true

        (catch js/Error e
          (js/console.log "server.main send-message ERROR")
          ;(js/console.log e)
          false)))

;
; gwp - Since I wasn't able to send a message to the broker, I didn't really play around with code much.
;
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
