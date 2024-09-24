(ns frontend.oz.events
  (:require 
   [re-frame.core :refer [reg-event-db reg-sub  dispatch]]
   [ajax.core :refer [GET]]
   [cljs.reader :as cljs.reader])) 


(def oz-scroll-path [:oz :scrolls])
(def oz-resource-url "/oz/scrolls")

(reg-sub 
 :oz/scrolls 
 (fn [db _]
   (get-in db oz-scroll-path)))

(reg-event-db 
 :oz/add-downloaded-scrolls 
 (fn [db [_ scrolls]]
   (assoc-in db oz-scroll-path scrolls)))


(reg-event-db 
 :oz/download-scrolls
 (fn [db [_]]
    (GET oz-resource-url
      {:handler (fn [response] 
                  (dispatch [:oz/add-downloaded-scrolls 
                             (cljs.reader/read-string response)]))}) 
    db))


