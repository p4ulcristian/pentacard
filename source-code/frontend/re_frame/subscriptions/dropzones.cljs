(ns frontend.re-frame.subscriptions.dropzones
   (:require [re-frame.core :refer [reg-sub]]))
             


(reg-sub
 :dropzones/is-dropzone-overlapped?
 (fn [db [_ index-and-area]]
   (let [overlapped-index-and-areas (get-in db [:overlays :areas :overlapped])]
    (boolean (some (fn [overlapped-index-and-area] 
                     (= index-and-area overlapped-index-and-area))
                   overlapped-index-and-areas)))))
