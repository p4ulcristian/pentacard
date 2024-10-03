(ns frontend.pentacard.new-method
  (:require [re-frame.alpha :refer [reg-event-db reg-event-fx dispatch subscribe]] 
            ["@react-spring/three" :refer [SpringValue]]
            ["react" :as react]))

(def spring (SpringValue. 0 #js {:to 0.3 :config #js {:mass 40
                                                      :tension 10000}}))

(defn set-position! []
  (dispatch [:db/set [:position] [0 (.get spring) 0.01]])
  (.requestAnimationFrame js/window set-position!))

(defn get-position [ref]
  (let [[x y z] @(subscribe [:db/get [:position]])]
    (set! (-> ref .-current .-position .-x) x)
    (set! (-> ref .-current .-position .-y) y)
    (set! (-> ref .-current .-position .-z) z))
    
  
  (.requestAnimationFrame js/window (fn [] (get-position ref))))



(defn view [] 
  (let [ref (react/useRef nil)]
    (react/useEffect 
     (fn [] 
       (.start spring)
       (dispatch [:db/set [:new-method-ref] ref])
       (dispatch [:render/add-callback (rand-int 100000) (set-position!)])
       (dispatch [:render/add-callback (rand-int 100000) (get-position ref)])
       
       (fn [])))
    [:mesh {:ref ref
            :position [0.3 0.3 0.01]}
     [:planeGeometry {:castShadow true
                      :receiveShadow true
                      :args [0.29 0.29]}]
     [:meshPhongMaterial {:color "red"}]]))