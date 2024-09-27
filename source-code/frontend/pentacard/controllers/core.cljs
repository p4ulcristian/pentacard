(ns frontend.pentacard.controllers.core
  (:require [re-frame.alpha :refer [reg-event-db dispatch]]
            [frontend.pentacard.controllers.state :as state]
            [frontend.pentacard.controllers.game-steps :as game-steps]
            [frontend.pentacard.controllers.render :as render]))

(dispatch [:state/setup!])