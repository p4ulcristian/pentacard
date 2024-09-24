(ns frontend.re-frame.events.dropzones.copy
  (:require [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.common :as wizard-utils] 
            [frontend.wizard.utils.nodes :as nodes]
            [frontend.re-frame.events.dropzones.helpers :as helpers]))
          


(defn get-areas-after-copy [deepest-path indexes-overlapped]
  (areas/copy-area
   {:area          (nodes/path->next-area  deepest-path)
    :areas         (nodes/path->areas      deepest-path)
    :indexes       indexes-overlapped}))


(defn set-overlapped-zones--copy! [db [_  path area]]
  (let [overlapped-areas      (helpers/get-overlapping-areas-on-move)
        overlapped-paths      (mapv second overlapped-areas)
        dragged-path          (helpers/get-dragged-path path area) 
        indexes-overlapped    (mapv first overlapped-areas)
        deepest-path          (wizard-utils/get-deepest-path overlapped-paths)
        copied-area           (areas/number-to-letter (inc (areas/letter-to-number area)))
        new-areas             (get-areas-after-copy deepest-path indexes-overlapped)
        impossible-config?    (areas/impossible-config? new-areas)]
    (helpers/set-new-overlay-config db {:impossible-config? impossible-config?
                                        :overlapped-paths overlapped-areas
                                        :area copied-area
                                        :dragged-path dragged-path})))

