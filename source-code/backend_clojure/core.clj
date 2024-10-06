(ns backend-clojure.core
  (:require
   [clojure.core.async :refer [<! go-loop]]
   [taoensso.sente :as sente]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]] ; <--- Recommended
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   
       ;; Uncomment a web-server adapter --->  
   [taoensso.sente.server-adapters.http-kit      :refer [get-sch-adapter]]
       ;; [taoensso.sente.server-adapters.immutant      :refer [get-sch-adapter]]
       ;; [taoensso.sente.server-adapters.nginx-clojure :refer [get-sch-adapter]]
       ;; [taoensso.sente.server-adapters.aleph         :refer [get-sch-adapter]] 
   [reitit.ring :as ring]
   [backend-clojure.html :as html]
   [org.httpkit.server :as hk-server]
   [ring.middleware.params :refer [wrap-params]])
  (:gen-class))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket-server! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids)) ; Watchable, read-only atom
  

(defn handler []
  (ring/ring-handler
   (ring/router
    [["/" {:get (fn [req] {:status 200 :body (html/home-page)})}] 
     ["/api" {:get (fn [req] {:status 200 :body "API is up!"})}]
     ["/chsk" {:get ring-ajax-get-or-ws-handshake
               :post ring-ajax-post}]]) 
   (ring/routes
    (ring/create-resource-handler {:path "/"
                                   :root "/frontend/public"}))
   {:middleware [#(wrap-reload % {:dirs ["source-code/backend_clojure"]})
                 wrap-params 
                 ring.middleware.keyword-params/wrap-keyword-params 
                 ring.middleware.anti-forgery/wrap-anti-forgery
                 ring.middleware.session/wrap-session]} 
   ))

;; Step 4: Run the server
(defn start-server []
  (hk-server/run-server (handler) {:port 3000 :join? false})
  (println "Server running on http://localhost:3000"))


;; Step 6: Initialize everything
(defn -main []
  (start-server))