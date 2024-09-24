(ns frontend.starter-kit.utils.react
  (:require ["react" :as react]
            ["react-dom" :as react-dom]
            [reagent.core :as reagent]))


(defn get-element []
  (try (.getElementById js/document "app")
       (catch js/Error e nil )))

(defn portal [content]
  (let [element (get-element)]
    [:<>
     (when element
       (react-dom/createPortal
        (reagent/as-element content)
        element))]))

(defn js-rect-data->edn-rect-data [bounding-rect]
  {:top       (.-top     bounding-rect)
   :bottom    (.-bottom  bounding-rect)
   :width     (.-width   bounding-rect)
   :height    (.-height  bounding-rect)
   :left      (.-left    bounding-rect)
   :right     (.-right   bounding-rect)})

(defn async-get-bounding-client-rect [el callback-fn]
  (let [observer (atom nil)
        observer-fn      (fn [handler]
                           (reset! observer (new js/IntersectionObserver handler)))
        observer-handler (fn [entries]
                           (doseq [entry entries]
                             (let [bounds (.-boundingClientRect entry)]

                               (callback-fn (js-rect-data->edn-rect-data bounds))
                               (.disconnect @observer))))]
    (observer-fn observer-handler)
    (.observe @observer el)))


(defn block-pointer-events [ref]
  (react/useEffect (fn []
                     (let [effect-fn (fn [e] (.stopPropagation e))
                           current-ref (.-current ref)]
                       (.addEventListener current-ref "pointerdown" effect-fn)
                       (.addEventListener current-ref "mousedown" effect-fn)
                       (.addEventListener current-ref "mouseup" effect-fn)
                       (fn []
                         (.removeEventListener current-ref "pointerdown" effect-fn)
                         (.removeEventListener current-ref "mousedown" effect-fn)
                         (.removeEventListener current-ref "mouseup" effect-fn))))
                   #js []))




(defn block-arrow-events [ref]
  (react/useEffect (fn []
                     (let [effect-fn (fn [e] (.stopPropagation e))
                           current-ref (.-current ref)]
                       (.addEventListener current-ref "keydown" effect-fn) 
                       (fn []
                         (.removeEventListener current-ref "keydown" effect-fn))))
                   #js []))

