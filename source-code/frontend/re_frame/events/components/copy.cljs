(ns frontend.re-frame.events.components.copy
  (:require [frontend.starter-kit.utils.basic :as utils]
            [frontend.wizard.utils.layers :as layers]
            [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.nodes :as nodes]
            [frontend.re-frame.events.components.event-helpers :as helpers]))


(defn copy-to-new-layer--diff-level [db source-path target-path area]
  (let [new-layer-key   (utils/generate-keyword)
        new-layer-path  (helpers/get-new-layer-path target-path new-layer-key)
        new-layer-areas (helpers/add-to-resetted-areas db target-path)
        new-layer-areas-fixed-order (areas/fix-order new-layer-areas)]
    (-> db
        (layers/duplicate-layer target-path new-layer-key)
        (helpers/copy-node        source-path new-layer-path area)
        (helpers/set-new-areas    new-layer-path  new-layer-areas-fixed-order)
        (helpers/select-new-layer new-layer-path new-layer-key))))

(defn copy--diff-level! [db source-path target-path area]
  (let [target-new-areas            (helpers/get-new-areas-after-add db target-path)
        target-impossible-config?   (areas/impossible-config? target-new-areas)
        target-new-areas-fixed-order (areas/fix-order target-new-areas)]
    (if target-impossible-config?
      (copy-to-new-layer--diff-level db source-path target-path area)
      (-> db
          (helpers/remove-unused-nodes  target-path target-new-areas-fixed-order)
          (helpers/copy-node source-path  target-path area)
          (helpers/set-new-areas target-path  target-new-areas)))))



(defn copy-to-new-layer [db old-layer-path area]
  (let [new-layer-key   (utils/generate-keyword)
        new-layer-path  (helpers/get-new-layer-path old-layer-path new-layer-key)
        new-layer-areas (helpers/add-to-resetted-areas db old-layer-path)
        new-layer-areas-fixed-order (areas/fix-order new-layer-areas)]
    (-> db
        (layers/duplicate-layer old-layer-path new-layer-key)
        (helpers/copy-node    old-layer-path new-layer-path area)
        (helpers/set-new-areas  new-layer-path  new-layer-areas-fixed-order)
        (helpers/select-new-layer new-layer-path new-layer-key))))


(defn copy-to-this-layer [db path area]
  (let [new-areas (helpers/get-new-areas-after-add db path)
        node                 (helpers/get-node-by-area db path area)
        new-areas-fixed-order (areas/fix-order new-areas)]
    (-> db
        (helpers/remove-unused-nodes  path new-areas)
        (helpers/add-node path (nodes/rename-keys-in-tree node))
        (helpers/set-new-areas path new-areas-fixed-order))))


(defn copy--same-level! [db path area]
  (let [impossible-config?   (helpers/is-impossible-config? db)]
    (if impossible-config?
      (copy-to-new-layer  db path area)
      (copy-to-this-layer db path area))))

(defn copy-active? [db]
  (let [overlapped-indexes-and-paths  (helpers/get-overlapped-indexes-and-paths db)]
    (not (= nil (first overlapped-indexes-and-paths)))))

(defn copy! [db [_ source-path area]]
  (let [target-path        (helpers/get-deepest-overlapped-path db)]
    (if (copy-active? db)
      (if (= source-path target-path)
        (copy--same-level! db source-path area)
        (copy--diff-level! db source-path target-path area))
      db)))