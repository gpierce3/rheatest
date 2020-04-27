(ns server.main
  ; Import NPM modules directly 
  ; https://shadow-cljs.github.io/docs/UsersGuide.html#npm
  (:require ["crypto" :as crypto])
  (:require ["http" :as http]))

; This is standard ClojureScript code and the output should be run using Node.
;
; More details at https://shadow-cljs.github.io/docs/UsersGuide.html#target-node-script

(defn send-message []
      (js/console.log "sending a message to queue"))

(defn retrieve-message []
      (js/console.log "retrieving a message to queue"))

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
