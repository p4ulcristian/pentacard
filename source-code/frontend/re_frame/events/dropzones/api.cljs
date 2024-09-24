(ns frontend.re-frame.events.dropzones.api
  (:require 
   [my-re-frame :refer [reg-event-db]]
   [frontend.re-frame.events.dropzones.add-remove :as add-remove]
   [frontend.re-frame.events.dropzones.copy :as copy]
   [frontend.re-frame.events.dropzones.move :as move]
   [frontend.re-frame.events.dropzones.resize :as resize]))
  
;;; resize

(reg-event-db
 :dropzones/reset-after-resize!
 resize/reset-after-resize!)


(reg-event-db
  :dropzones/reset-before-resize!
  resize/reset-before-resize!)


(reg-event-db
  :dropzones/set-overlapped-zones--resize!
  resize/set-overlapped-zones--resize!) 


;;; move

(reg-event-db
  :dropzones/reset-after-move!
  move/reset-after-move!)


(reg-event-db
  :dropzones/set-overlapped-zones--move!
  move/set-overlapped-zones--move!)


;;; copy

(reg-event-db
  :dropzones/set-overlapped-zones--copy!
  copy/set-overlapped-zones--copy!) 

;;; add

(reg-event-db
  :dropzones/add!
  add-remove/add!)

;;; remove

(reg-event-db
  :dropzones/remove!
  add-remove/remove!)