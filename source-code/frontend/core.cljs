(ns frontend.core
  (:require [frontend.router :as router]
            [re-frame.core :refer [dispatch]]
            [reagent.core :as reagent]
            [reagent.dom  :as reagent-dom] 
            [reagent.dom.client :as reagent-dom-client]
            [reagent.impl.template :as reagent-template]))
            

(def functional-compiler (reagent/create-compiler {:function-components    true}))

(reagent-template/set-default-compiler! functional-compiler)

(def root  (reagent-dom-client/create-root (.getElementById js/document "app")))

(dispatch [:editor/init!])

(defn start! [] (reagent-dom-client/render root [router/ui-router] ))

