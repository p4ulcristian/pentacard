(ns backend-clojure.core
  (:require
   [clojure.core.async :refer [<! go-loop]]
   [taoensso.sente :as sente]
   [ring.middleware.defaults :as middleware]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]] ; <--- Recommended
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.transit :refer [wrap-transit-params]]
   [ring.middleware.gzip :refer [wrap-gzip]]
   
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
  

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get (fn [req] {:status 200 :body (html/home-page)})}] 
     ["/api" {:get (fn [req] {:status 200 :body "API is up!"})}]
     ["/chsk" {:get ring-ajax-get-or-ws-handshake
               :post ring-ajax-post}]]) 
   (ring/routes
    (ring/create-resource-handler {:path "/"
                                   :root "/frontend/public"}))
   {:middleware [middleware/site-defaults
                 ring.middleware.keyword-params/wrap-keyword-params 
                 wrap-params 
                 ring.middleware.anti-forgery/wrap-anti-forgery
                 ring.middleware.session/wrap-session  
                 wrap-transit-params
                 ;wrap-gzip
                 ]}
   ))


(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and https://http-kit.github.io/migration.html#reload
  (reset! server (hk-server/run-server #'app {:port 3000})))
