(ns frontend.re-frame.events.components.api
  (:require
   ;[akiroz.re-frame.storage :refer [persist-db]]
   [my-re-frame :refer [reg-event-db]]
   [frontend.re-frame.events.undo :as undo :refer [undoable]] 
   [frontend.re-frame.events.components.add :as add]
   [frontend.re-frame.events.components.move :as move]
   [frontend.re-frame.events.components.move-section :as move-section]
   [frontend.re-frame.events.components.copy :as copy]
   [frontend.re-frame.events.components.resize :as resize]
   [frontend.wizard.spec :as spec]))


(reg-event-db
 :components/add!
 [;(persist-db :pages-local :pages)
  (undoable)
  spec/spec-rollback-interceptor]
 add/add!)


(reg-event-db
  :components/move-section!
  [;(persist-db :pages-local :pages)
   (undoable)
   spec/spec-rollback-interceptor]
  move-section/move-section!)


(reg-event-db
  :components/move!
  [;(persist-db :pages-local :pages)
   (undoable)
   spec/spec-rollback-interceptor]
  move/move!)


(reg-event-db
  :components/copy!
  [;(persist-db :pages-local :pages)
   (undoable)
   spec/spec-rollback-interceptor]
  copy/copy!)


(reg-event-db
  :components/resize!
  [;(persist-db :pages-local :pages)
   (undoable)
   spec/spec-rollback-interceptor]
  resize/resize!)

