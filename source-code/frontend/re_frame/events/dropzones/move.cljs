(ns frontend.re-frame.events.dropzones.move
  (:require [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.common :as wizard-utils] 
            [frontend.wizard.utils.nodes :as nodes]
            [frontend.re-frame.events.dropzones.helpers :as helpers]))
           
(def default-dropzones {:overlapped            nil
                        :dragged               nil
                        :dragged-path          nil
                        :impossible-config?    false
                        :overlapping-areas     []})


(defn reset-after-move! [db [_  path dom-id area]]
  (let [overlay-config-path   [:overlays :areas]
        overlay-config        (get-in db overlay-config-path)
        new-overlay-config    (merge overlay-config
                                     default-dropzones)]

    (assoc-in db overlay-config-path new-overlay-config)))


(defn get-areas-after-move [indexes-overlapped deepest-path path area]
  (areas/fix-order
   (areas/move-area
    {:area       (if (= path deepest-path)
                   area
                   (nodes/path->next-area deepest-path))
     :areas      (nodes/path->areas deepest-path)
     :indexes    indexes-overlapped})))


(defn set-overlapped-zones--move! [db [_  path area]]
  (let [overlapped-areas      (helpers/get-overlapping-areas-on-move)
        overlapped-paths      (mapv second overlapped-areas)
        dragged-path          (helpers/get-dragged-path path area) 
        indexes-overlapped    (mapv first overlapped-areas)
        deepest-path          (wizard-utils/get-deepest-path overlapped-paths)
        new-areas             (get-areas-after-move indexes-overlapped deepest-path path area)
        impossible-config?    (areas/impossible-config? new-areas)] 
    (helpers/set-new-overlay-config db {:impossible-config? impossible-config?
                                        :overlapped-paths overlapped-areas 
                                        :area area
                                        :dragged-path dragged-path})))


