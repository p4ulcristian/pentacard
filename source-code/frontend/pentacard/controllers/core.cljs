(ns frontend.pentacard.controllers.core
  (:require [re-frame.core :refer [reg-event-db dispatch]]
            [frontend.pentacard.controllers.state :as state]))

(dispatch [:state/setup!])