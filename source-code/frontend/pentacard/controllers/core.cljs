(ns frontend.pentacard.controllers.core
  (:require [re-frame.core :refer [reg-event-db dispatch]]
            [frontend.pentacard.controllers.state :as state]
            [frontend.pentacard.controllers.game :as game]
            [frontend.pentacard.controllers.render :as render]))

(dispatch [:state/setup!])