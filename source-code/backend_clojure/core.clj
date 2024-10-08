
(ns backend-clojure.core
  "Official Sente reference example: server"
  {:author "Peter Taoussanis (@ptaoussanis)"}

  (:require
   [clojure.string     :as str]
   [ring.middleware.defaults]
   [clojure.core.async :refer [<! go-loop]]
   [taoensso.sente :as sente]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.defaults :as middleware]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]] ; <--- Recommended
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session :refer [wrap-session]]
   [ring.middleware.transit :refer [wrap-transit-params]]
   [ring.middleware.gzip :refer [wrap-gzip]]
   [reitit.ring :as ring]
   [ring.middleware.anti-forgery :as anti-forgery]
   [compojure.core     :as comp :refer [defroutes GET POST]]
   [compojure.route    :as route]
   [backend-clojure.view :as view]
   [hiccup.core        :as hiccup]
   [clojure.core.async :as async  :refer [<! <!! >! >!! put! chan go go-loop]]
   [taoensso.encore    :as encore :refer [have have?]]
   [taoensso.timbre    :as timbre]

   ;;; TODO Choose (uncomment) a supported web server + adapter -------------
   [org.httpkit.server :as http-kit]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]

   ;; [immutant.web :as immutant]
   ;; [taoensso.sente.server-adapters.immutant :refer [get-sch-adapter]]

   ;; [nginx.clojure.embed :as nginx-clojure]
   ;; [taoensso.sente.server-adapters.nginx-clojure :refer [get-sch-adapter]]

   ;; [aleph.http :as aleph]
   ;; [taoensso.sente.server-adapters.aleph :refer [get-sch-adapter]]

   ;; [ring.adapter.jetty9.websocket :as jetty9.websocket]
   ;; [taoensso.sente.server-adapters.jetty9 :refer [get-sch-adapter]]
   ;;
   ;; See https://gist.github.com/wavejumper/40c4cbb21d67e4415e20685710b68ea0
   ;; for full example using Jetty 9

   ;; -----------------------------------------------------------------------

   ;; Optional, for Transit encoding:
   [taoensso.sente.packers.transit :as sente-transit]))

;;;; Logging config

(defonce   min-log-level_ (atom nil))
(defn- set-min-log-level! [level]
  (sente/set-min-log-level! level) ; Min log level for internal Sente namespaces
  (timbre/set-ns-min-level! level) ; Min log level for this           namespace
  (reset! min-log-level_    level))

(set-min-log-level! #_:trace :debug #_:info #_:warn)

;;;; Define our Sente channel socket (chsk) server

(let [;; Serialization format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-transit-packer) ; Needs Transit dep
      ]

  (defonce chsk-server
    (sente/make-channel-socket-server!
     (get-sch-adapter) {:packer packer})))

(let [{:keys [ch-recv send-fn connected-uids_ private
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      chsk-server]

  (defonce ring-ajax-post                ajax-post-fn)
  (defonce ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (defonce ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (defonce chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (defonce connected-uids_               connected-uids_)   ; Watchable, read-only atom
  (defonce conns_                        (:conns_ private)) ; Implementation detail, for debugging!
  )

;; We can watch this atom for changes
(add-watch connected-uids_ :connected-uids
           (fn [_ _ old new]
             (when (not= old new)
               (timbre/infof "Connected uids change: %s" new))))

;;;; Ring handlers

(defn landing-pg-handler [ring-req]
  (hiccup/html
   
     (view/home-page)
    ;; Convey server's min-log-level to client
   ))



(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-req]
  (let [{:keys [session params]} ring-req
        {:keys [user-id]} params]
    (timbre/debugf "Login request: %s" params)
    {:status 200 :session (assoc session :uid user-id)}))

(defroutes app
  (GET  "/"      ring-req (landing-pg-handler            ring-req))
  (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
  (POST "/login" ring-req (login-handler                 ring-req))
  (route/resources "/" {:root "frontend/public"}) ; Static files, notably public/main.js (our cljs target)
  (route/not-found "<h1>Page not found</h1>"))



(def main-ring-handler
  "**NB**: Sente requires the Ring `wrap-params` + `wrap-keyword-params`
  middleware to work. These are included with
  `ring.middleware.defaults/wrap-defaults` - but you'll need to ensure
  that they're included yourself if you're not using `wrap-defaults`.

  You're also STRONGLY recommended to use `ring.middleware.anti-forgery`
  or something similar."
  (ring.middleware.defaults/wrap-defaults
   app ring.middleware.defaults/site-defaults))

;;;; Some server>user async push examples

(defn broadcast!
  "Pushes given event to all connected users."
  [event]
  (let [all-uids (:any @connected-uids_)]
    (doseq [uid all-uids]
      (timbre/debugf "Broadcasting server>user to %s uids" (count all-uids))
      (chsk-send! uid event))))

(defn test-broadcast!
  "Quickly broadcasts 100 events to all connected users.
  Note that this'll be fast+reliable even over Ajax!"
  []
  (doseq [uid (:any @connected-uids_)]
    (doseq [i (range 100)]
      (chsk-send! uid [:example/broadcast (str {:i i, :uid uid})]))))

(comment (test-broadcast!))

(defonce broadcast-loop?_ (atom true))
(defonce ^:private auto-loop_
  (delay
    (go-loop [i 0]
      (<! (async/timeout 10000)) ; 10 secs

      (timbre/debugf "Connected uids: %s" @connected-uids_)
      (timbre/tracef "Conns state: %s"    @conns_)

      (when @broadcast-loop?_
        (broadcast!
         [:example/broadcast-loop
          {:my-message "A broadcast, pushed asynchronously from server"
           :i i}]))

      (recur (inc (long i))))))

;;;; Sente event handlers

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (timbre/debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:unmatched-event-as-echoed-from-server event}))))

(defmethod -event-msg-handler :chsk/uidport-open
  [{:as ev-msg :keys [ring-req]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (if uid
      (timbre/infof "User connected: user-id `%s`" uid)
      (timbre/infof "User connected: no user-id (user didn't have login session)"))))

(defmethod -event-msg-handler :chsk/uidport-close
  [{:as ev-msg :keys [ring-req]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (if uid
      (timbre/infof "User disconnected: user-id `%s`" uid)
      (timbre/infof "User disconnected: no user-id (user didn't have login session)"))))

(defmethod -event-msg-handler :example/test-broadcast
  [ev-msg] (test-broadcast!))

(defmethod -event-msg-handler :example/toggle-broadcast-loop
  [{:as ev-msg :keys [?reply-fn]}]
  (let [loop-enabled? (swap! broadcast-loop?_ not)]
    (?reply-fn loop-enabled?)))

(defmethod -event-msg-handler :example/toggle-min-log-level
  [{:as ev-msg :keys [?reply-fn]}]
  (let [new-val
        (case @min-log-level_
          :trace :debug
          :debug :info
          :info  :warn
          :warn  :error
          :error :trace
          :trace)]

    (set-min-log-level! new-val)
    (?reply-fn          new-val)))

(defmethod -event-msg-handler :example/toggle-bad-conn-rate
  [{:as ev-msg :keys [?reply-fn]}]
  (let [new-val
        (case sente/*simulated-bad-conn-rate*
          nil  0.25
          0.25 0.5
          0.5  0.75
          0.75 1.0
          1.0  nil)]

    (alter-var-root #'sente/*simulated-bad-conn-rate* (constantly new-val))
    (?reply-fn new-val)))

(defmethod -event-msg-handler :example/connected-uids
  [{:as ev-msg :keys [?reply-fn]}]
  (let [uids @connected-uids_]
    (timbre/infof "Connected uids: %s" uids)
    (?reply-fn                         uids)))

;; TODO Add your (defmethod -event-msg-handler <event-id> [ev-msg] <body>)s here...

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router!
           ch-chsk event-msg-handler)))

;;;; Init stuff

(defonce    web-server_ (atom nil)) ; (fn stop [])
(defn  stop-web-server! [] (when-let [stop-fn @web-server_] (stop-fn)))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [port 4000 ;(or port 0) ; 0 => Choose any available port
        ring-handler (var main-ring-handler)

        [port stop-fn]
        ;;; TODO Choose (uncomment) a supported web server ------------------
        (let [stop-fn (http-kit/run-server ring-handler {:port port})]
          [(:local-port (meta stop-fn)) (fn stop-fn [] (stop-fn :timeout 100))])
        ;;
        ;; (let [server (immutant/run ring-handler :port port)]
        ;;   [(:port server) (fn stop-fn [] (immutant/stop server))])
        ;;
        ;; (let [port (nginx-clojure/run-server ring-handler {:port port})]
        ;;   [port (fn stop-fn [] (nginx-clojure/stop-server))])
        ;;
        ;; (let [server (aleph/start-server ring-handler {:port port})
        ;;       p (promise)]
        ;;   (future @p) ; Workaround for Ref. https://goo.gl/kLvced
        ;;   ;; (aleph.netty/wait-for-close server)
        ;;   [(aleph.netty/port server)
        ;;    (fn stop-fn [] (.close ^java.io.Closeable server) (deliver p nil))])
        ;; ------------------------------------------------------------------

        uri (format "http://localhost:%s/" port)]

    (timbre/infof "HTTP server is running at `%s`" uri)
    (try
      (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
      (catch Exception _))

    (reset! web-server_ stop-fn)))

(defn stop!  [] (stop-router!) (stop-web-server!))
(defn start! []
  (timbre/reportf "Sente version: %s" sente/sente-version)
  (timbre/reportf "Min log level: %s" @min-log-level_)
  (start-router!)
  (let [stop-fn (start-web-server!)]
    @auto-loop_
    stop-fn))

(defn -main "For `lein run`, etc." [] (start!))

(comment
  (start!) ; Eval this at REPL to start server via REPL
  (test-broadcast!)

  (broadcast! [:example/foo])
  @connected-uids_
  @conns_)
