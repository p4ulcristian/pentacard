(ns frontend.pentacard.controllers.game
  (:require [re-frame.core :refer [reg-event-db dispatch]]))


(reg-event-db
 :dsastate/setup!
 (fn [db []]
   db))