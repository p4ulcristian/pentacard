(ns frontend.pentacard.events
  (:require 
   [re-frame.alpha :refer [dispatch]]
   [frontend.pentacard.events.animations :as animations]
   [frontend.pentacard.events.state :as state]
   [frontend.pentacard.events.render :as render]
   [frontend.pentacard.events.game :as game]
   [frontend.pentacard.events.communications :as communications]))

(dispatch [:state/setup!])