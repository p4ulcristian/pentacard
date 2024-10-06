(ns frontend.websocket
  ; Usually a .cljs file
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   ;; <other stuff>
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente  :as sente :refer (cb-success?)] ; <--- Add this
   ))

;;; Add this: --->

(def ?csrf-token
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
       "/chsk" ; Note the same path as before
       ?csrf-token
       {:type :auto ; e/o #{:auto :ajax :ws}
        })]

  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(println "HMMM" ?csrf-token)
(chsk-send! ; Using Sente
 [:some/request-id {:name "Rich Hickey" :type "Awesome"}] ; Event
 8000 ; Timeout
  ;; Optional callback:
 (fn [reply] ; Reply is arbitrary Clojure data
   (if (sente/cb-success? reply) ; Checks for :chsk/closed, :chsk/timeout, :chsk/error
     (println "heeey" reply)
     (println "oooh" reply))))