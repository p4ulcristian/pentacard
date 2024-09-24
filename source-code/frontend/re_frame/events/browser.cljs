(ns frontend.re-frame.events.browser
  (:require 
   [re-frame.core :refer [reg-sub]]
   [my-re-frame :refer [reg-event-db]]
   ["scroll-lock" :refer [disablePageScroll enablePageScroll]]))
 

(reg-event-db
 :browser/set-pointer!
 (fn [db [_ pointer-data]]
   (assoc-in db [:browser :pointer] pointer-data)))

(reg-sub
 :browser/pointer-x
 (fn [db [_]]
   (-> db :browser :pointer :x)))

(reg-sub
 :browser/pointer-y
 (fn [db [_]]
   (-> db :browser :pointer :y)))

(defn reset-app-height []
  (let [visual-height  (.-height js/visualViewport) 
        app (js/document.getElementById "app")]
    (set! (.-height (.-style app)) (str visual-height "px"))))



(defn disable-scroll! []
  (disablePageScroll))
                                       
(defn enable-scroll! []
  (enablePageScroll))
 
(reg-event-db
 :browser/disable-scroll!
 (fn [db [_]]
   (.addEventListener js/window "scroll" (fn [e] 
                                           (.preventDefault e)
                                           (.scrollTo js/window #js {:top 1,
                                                                     :left 0,
                                                                     :behavior "instant"})))
                                           
   ;(disable-scroll!)
   db))

(reg-event-db
 :browser/enable-scroll!
 (fn [db [_]]
   (enable-scroll!)
   db))

(reg-event-db
 :browser/set-window!
 (fn [db [_]]
   (let [window-data
         {:visual-width  (.-width  js/visualViewport)
          :visual-height (.-height js/visualViewport)
          :width         (.-innerWidth  js/window)
          :height        (.-innerHeight js/window)}] 
     (reset-app-height) 
     (-> db
         (assoc-in [:browser :window]
                   window-data)))))


(reg-sub
 :browser/mobile-keyboard-open?
 (fn [db [_]]
   (let [height (-> db :browser :window :height)
         visual-height (-> db :browser :window :visual-height)]
     (not= height visual-height))))

(reg-sub 
 :browser/window-width 
 (fn [db [_]]
   (-> db :browser :window :width)))

(reg-sub
 :browser/window-height
 (fn [db [_]]
   (-> db :browser :window :height)))