(ns frontend.re-frame.events.components.move-section
  (:require [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.nodes :as nodes]
            [frontend.wizard.utils.common :as wizard-utils]
            [frontend.re-frame.events.components.event-helpers :as helpers]))




(defn section-area--move! [db source-path target-path area]
  (let [next-area                   (nodes/path->next-area target-path)
        source-areas-path           (nodes/path->areas-path source-path)
        source-new-areas            (helpers/get-new-areas-after-remove         db source-path area)
        source-impossible-config?   (areas/impossible-config? source-new-areas)

        target-areas-path           (nodes/path->areas-path target-path)
        target-new-areas            (helpers/get-new-areas-after-add db target-path)
        target-impossible-config?   (areas/impossible-config? target-new-areas)

        removed-positions           (helpers/get-removed-node-positions-after-move db target-path next-area)]

    (if (or target-impossible-config? source-impossible-config?)
      db
      (-> db
          (nodes/remove-nodes-at-positions  target-path        removed-positions)
          (helpers/move-section                      source-path        target-path area)
          (assoc-in                   source-areas-path  source-new-areas)
          (assoc-in                   target-areas-path  target-new-areas)))))




(defn move-section! [db [_ source-path area]]
  (let [overlapped-areas   (helpers/get-overlapped-indexes-and-paths db)
        overlapped-paths   (map second overlapped-areas)
        target-path        (wizard-utils/get-deepest-path overlapped-paths)]
    (section-area--move! db source-path target-path area)))
