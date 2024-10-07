(ns backend-clojure.sente
  "Official Sente reference example: server"
  {:author "Peter Taoussanis (@ptaoussanis)"}

  (:require
   [clojure.string     :as str]
   [ring.middleware.defaults]
   [ring.middleware.anti-forgery :as anti-forgery] 
   [hiccup.core        :as hiccup]
   [clojure.core.async :as async  :refer [<! <!! >! >!! put! chan go go-loop]]
   [taoensso.encore    :as encore :refer [have have?]]
   [taoensso.timbre    :as timbre]
   [taoensso.sente     :as sente]

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
;; (add-watch connected-uids_ :connected-uids
;;            (fn [_ _ old new]
;;              (when (not= old new)
;;                (timbre/infof "Connected uids change: %s" new))))

;;;; Ring handlers

(defn landing-pg-handler [ring-req]
  (hiccup/html
   (let [csrf-token
          ;; (:anti-forgery-token ring-req) ; Also an option
         (force anti-forgery/*anti-forgery-token*)]
     [:div#sente-csrf-token {:data-token csrf-token}])

    ;; Convey server's min-log-level to client
   [:div#sente-min-log-level {:data-level (name @min-log-level_)}]

   [:h3 "Sente reference example"]
   [:p
    "A " [:i "random"] " " [:strong [:code ":ajax/:auto"]]
    " connection mode has been selected (see " [:strong "client output"] ")."
    [:br]
    "To " [:strong "re-randomize"] ", hit your browser's reload/refresh button."]
   [:ul
    [:li [:strong "Server output:"] " → " [:code "*std-out*"]]
    [:li [:strong "Client output:"] " → Below textarea and/or browser console"]]
   [:textarea#output {:style "width: 100%; height: 200px;" :wrap "off"}]

   [:section
    [:h4 "Standard Controls"]
    [:p
     [:button#btn-send-with-reply {:type "button"} "chsk-send! (with reply)"] " "
     [:button#btn-send-wo-reply   {:type "button"} "chsk-send! (without reply)"] " "]
    [:p
     [:button#btn-test-broadcast        {:type "button"} "Test broadcast (server>user async push)"] " "
     [:button#btn-toggle-broadcast-loop {:type "button"} "Toggle broadcast loop"]]
    [:p
     [:button#btn-disconnect {:type "button"} "Disconnect"] " "
     [:button#btn-reconnect  {:type "button"} "Reconnect"]]
    [:p
     [:button#btn-login  {:type "button"} "Log in with user-id →"] " "
     [:input#input-login {:type :text :placeholder "user-id"}]]
    [:ul {:style "color: #808080; font-size: 0.9em;"}
     [:li "Log in with a " [:a {:href "https://github.com/ptaoussanis/sente/wiki/Client-and-user-ids#user-ids" :target :_blank} "user-id"]
      " so that the server can directly address that user's connected clients."]
     [:li "Open this page with " [:strong "multiple browser windows"] " to simulate multiple clients."]
     [:li "Use different browsers and/or " [:strong "Private Browsing / Incognito mode"] " to simulate multiple users."]]]

   [:hr]

   [:section
    [:h4 "Debug and Testing Controls"]
    [:p
     [:button#btn-toggle-logging       {:type "button"} "Toggle minimum log level"] " "
     [:button#btn-toggle-bad-conn-rate {:type "button"} "Toggle simulated bad conn rate"]]
    [:p
     [:button#btn-break-with-close {:type "button"} "Simulate broken conn (with on-close)"] " "
     [:button#btn-break-wo-close   {:type "button"} "Simulate broken conn (w/o on-close)"]]
    [:p
     [:button#btn-repeated-logins  {:type "button"} "Test repeated logins"] " "
     [:button#btn-connected-uids   {:type "button"} "Print connected uids"]]]

   [:script {:src "main.js"}] ; Include our cljs target
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