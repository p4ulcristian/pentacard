(ns frontend.re-frame.events.animations
  (:require
   [re-frame.alpha :refer [dispatch reg-event-fx reg-sub reg-fx]] 
   [my-re-frame :refer [reg-event-db]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Events 
;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-fx
 :side-effect
 (fn [the-function]
   (the-function)))


(defn add-timeout [the-fn]
 (.setTimeout js/window the-fn 10))

(defn init-app-animation [] 
  (let [body-element (.getElementById js/document "loading-container")]
    (.remove body-element)))


(reg-event-fx
 :animation/init-app!
 (fn [db [_]]
   {:side-effect    #(init-app-animation)}))
