(ns my-re-frame
  (:require [re-frame.core :as re-frame.core
             :refer [->interceptor console get-coeffect]]))

(def excluded-list [:browser/set-pointer!
                   ; :dropzones/add!
                    :dropzones/remove!
                    :dropzones/set-overlapped-zones--move!])

(defn excluded-event? [event-key]
  (boolean (some #(= event-key %) excluded-list)))

;; (def debug
;;   (->interceptor
;;    :id     :debug
;;   ;;  :before (fn debug-before
;;   ;;            [context]
;;   ;;            (console :log "Handling re-frame event:" (get-coeffect context :event))
;;   ;;            context)
;;    :after  (fn debug-after
;;              [context]
;;              (let [event   (get-coeffect context :event)
;;                    [event-key & event-params] event
;;                    orig-db (get-coeffect context :db)
;;                    new-db  (get-effect   context :db ::not-found)]
;;                (when-not (excluded-event? event-key)
;;                  (if (= new-db ::not-found)
;;                    (console :log "No app-db changes in:" event)
;;                    (let [[only-before only-after] (data/diff orig-db new-db)
;;                          db-changed?    (or (some? only-before) (some? only-after))]
;;                      (if db-changed?
;;                        (do (console :group "db clojure.data/diff for:" event)
;;                            (console :log "only before:" only-before)
;;                            (console :log "only after :" only-after)
;;                            (console :groupEnd))
;;                        (console :log "No app-db changes resulted from:" event)))))
;;                context))))

(def debug-beta
  (->interceptor
   :id     :debug
  ;;  :before (fn debug-before
  ;;            [context]
  ;;            (console :log "Handling re-frame event:" (get-coeffect context :event))
  ;;            context)
   :after  (fn debug-after
             [context]
             (let [event   (get-coeffect context :event)
                   [event-key & event-params] event]
               (when-not (excluded-event? event-key)
                 ;(console :log context)
                 (console :log event-key event-params))
               context))))

(def standard-interceptors  [;debug-beta
                             ])

(defn reg-event-db          ;; alternative to reg-event-db
  ([id handler-fn]
   (re-frame.core/reg-event-db id standard-interceptors handler-fn))
  ([id interceptors handler-fn]
   (re-frame.core/reg-event-db
    id
    [standard-interceptors interceptors]
    handler-fn)))
