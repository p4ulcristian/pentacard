(ns frontend.re-frame.events.dropzones.resize
  (:require [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.common :as wizard-utils]
            [frontend.starter-kit.utils.basic :as utils]
            [frontend.wizard.utils.nodes :as nodes]
            [frontend.re-frame.events.dropzones.helpers :as helpers]
            ))

(defn reset-after-resize! [db [_]]
  (let [areas       (get-in db [:overlays :areas])
        new-areas   (merge areas
                           {:impossible-config?    false
                            :overlapped            nil
                            :dragged               nil
                            :dragged-path          nil
                            :resized               nil
                            :resize-direction      nil
                            :resized-area-rect     nil})]
    (assoc-in db [:overlays :areas] new-areas)))


(defn reset-before-resize! [db [_  dom-id]]
  (let [resized-rect   (wizard-utils/get-rect-data-by-id dom-id)
        areas          (get-in db [:overlays :areas])
        new-areas      (merge areas
                              {:resized-area-rect    resized-rect
                               :resize-delta         {:x    0
                                                      :y    0}})]
    (assoc-in db [:overlays :areas] new-areas)))



(defn set-overlapped-zones--resize! [db [_  dom-id area direction path delta]]
  (let [{:keys    [x y]}         (utils/js->>clj delta)
        overlapped-areas         (helpers/get-overlapping-areas-on-resize)
        indexes-overlapped       (mapv first overlapped-areas) 
        modified-areas           (areas/move-area {:area         area
                                                   :areas        (nodes/path->areas path)
                                                   :indexes      indexes-overlapped})
        impossible-config?       (areas/impossible-config? modified-areas)
        overlay-config-path      [:overlays :areas]
        overlay-config           (get-in db overlay-config-path)

        sub-nodes                (nodes/path->sub-nodes path)
        dragged-key              (nodes/area-position->node-key
                                  (areas/letter-to-number area)
                                  sub-nodes)
        dragged-path             (vec (concat path [:components dragged-key]))
        new-overlay-config       (merge overlay-config
                                        {:impossible-config?    impossible-config?
                                         :overlapped            overlapped-areas
                                         :dragged               area
                                         :dragged-path          dragged-path
                                         :resized               area
                                         :resize-direction      direction
                                         :resize-delta          {:x    x
                                                                 :y    y}})]
    (assoc-in db overlay-config-path new-overlay-config)))

