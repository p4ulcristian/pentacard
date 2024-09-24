(ns frontend.re-frame.events.components.move
  (:require [frontend.starter-kit.utils.basic :as utils]
            [frontend.wizard.utils.layers :as layers]
            [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.nodes :as nodes]
            [frontend.re-frame.events.components.event-helpers :as helpers]))



(defn move-to-new-layer--diff-level [db source-path target-path area]
  (let [indexes-overlapped   (helpers/get-overlapped-area-indexes db)
        old-layer-areas        (areas/remove-area
                                {:area  area
                                 :areas (nodes/path->areas source-path)
                                 :indexes indexes-overlapped}) 
        old-layer-areas-fixed-order (areas/fix-order old-layer-areas)
        new-layer-key          (utils/generate-keyword)
        new-layer-path  (helpers/get-new-layer-path target-path new-layer-key)
        new-layer-areas (helpers/add-to-resetted-areas db target-path)]
    (-> db
        (layers/duplicate-layer target-path new-layer-key)
        (helpers/move-node    source-path new-layer-path area)
        (helpers/set-new-areas  source-path  old-layer-areas-fixed-order)
        (helpers/set-new-areas  new-layer-path  new-layer-areas)
        (helpers/select-new-layer new-layer-path new-layer-key))))


(defn move-to-new-layer [db old-layer-path area]
  (let [indexes-overlapped   (helpers/get-overlapped-area-indexes db)
        old-layer-areas        (areas/remove-area
                                {:area  area
                                 :areas (nodes/path->areas old-layer-path)
                                 :indexes indexes-overlapped})
        old-layer-areas-fixed-order (areas/fix-order old-layer-areas)
        new-layer-key          (utils/generate-keyword)
        new-layer-path  (helpers/get-new-layer-path old-layer-path new-layer-key)
        new-layer-areas (helpers/add-to-resetted-areas db old-layer-path)] 
    (-> db
        (layers/duplicate-selected-layer old-layer-path new-layer-key)
        (helpers/move-node    old-layer-path new-layer-path area)
        (helpers/set-new-areas  old-layer-path  old-layer-areas-fixed-order)
        (helpers/set-new-areas  new-layer-path  new-layer-areas)
        (helpers/select-new-layer new-layer-path new-layer-key))))

(defn move--diff-level [db source-path target-path area]
  (let [source-new-areas            (helpers/get-new-areas-after-remove db source-path area)
        source-new-areas-fixed-order (areas/fix-order source-new-areas)
        source-impossible-config?   (areas/impossible-config? source-new-areas)
        target-new-areas            (helpers/get-new-areas-after-add db target-path)
        target-new-areas-fixed-order (areas/fix-order target-new-areas)
        target-impossible-config?   (areas/impossible-config? target-new-areas)]

    (if (or target-impossible-config? source-impossible-config?)
      (move-to-new-layer--diff-level db source-path target-path area)
      (-> db
          (helpers/remove-unused-nodes  target-path target-new-areas)
          (helpers/move-node source-path target-path area)
          (helpers/set-new-areas source-path  source-new-areas-fixed-order)
          (helpers/set-new-areas target-path target-new-areas-fixed-order)))))


(defn move-to-this-layer [db path area]
  (let [new-areas  (helpers/get-new-areas-after-move db path area)
        new-areas-fixed-order (areas/fix-order new-areas)]
    (-> db
        (helpers/remove-unused-nodes  path new-areas)
        (helpers/set-new-areas path new-areas-fixed-order))))


(defn move--same-level [db path area]
  (let [impossible-config? (helpers/is-impossible-config? db)]
    (if impossible-config?
      (move-to-new-layer db path area)
      (move-to-this-layer db path area))))

(defn move-active? [db]
  (let [overlapped-indexes-and-paths  (helpers/get-overlapped-indexes-and-paths db)] 
    (not (= nil (first overlapped-indexes-and-paths)))))

(defn move! [db [_ source-path area]]
  (let [target-path        (helpers/get-deepest-overlapped-path db)
        same-source-and-target? (= source-path target-path)] 
    (if (move-active? db)
      (if same-source-and-target?
        (move--same-level db source-path area)
        (move--diff-level db source-path target-path area))
      db)))