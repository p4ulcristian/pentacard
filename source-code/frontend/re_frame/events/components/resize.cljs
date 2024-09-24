(ns frontend.re-frame.events.components.resize
  (:require [frontend.starter-kit.utils.basic :as utils]
            [frontend.wizard.utils.layers :as layers]
            [frontend.wizard.utils.areas :as areas]
            [frontend.wizard.utils.nodes :as nodes]
            [frontend.re-frame.events.components.event-helpers :as helpers]))


(defn resize-to-new-layer [db area]
  (let [overlapped-indexes-and-paths (helpers/get-overlapped-indexes-and-paths db)
        old-layer-path       (helpers/get-deepest-overlapped-path db)
        old-layer-areas      (nodes/path->areas old-layer-path)
        old-layer-new-areas  (areas/remove-area {:area area
                                                 :areas old-layer-areas
                                                 :indexes overlapped-indexes-and-paths}) 
        old-layer-new-areas-fixed-order (areas/fix-order old-layer-new-areas) 
        new-layer-key        (utils/generate-keyword)
        new-layer-path       (helpers/get-new-layer-path old-layer-path new-layer-key)
        new-layer-areas      (helpers/add-to-resetted-areas db old-layer-path)
        new-layer-areas-fixed-order (areas/fix-order new-layer-areas)]

    (-> db
        (layers/duplicate-selected-layer old-layer-path new-layer-key)
        (helpers/move-node old-layer-path    new-layer-path area)
        (helpers/set-new-areas  old-layer-path    old-layer-new-areas-fixed-order)
        (helpers/set-new-areas  new-layer-path    new-layer-areas-fixed-order)
        (helpers/select-new-layer new-layer-path  new-layer-key))))


(defn resize-to-this-layer [db area]
  (let [path       (helpers/get-deepest-overlapped-path db)
        new-areas  (helpers/get-new-areas-after-move db path area)
        new-areas-fixed-order (areas/fix-order new-areas)] 
    (-> db
        (helpers/remove-unused-nodes path new-areas)
        (helpers/set-new-areas path new-areas-fixed-order))))


(defn resize-active? [db]
  (let [not-resized?            (not (helpers/is-resized? db))
        overlapped-indexes-and-paths  (helpers/get-overlapped-indexes-and-paths db)]
    (not (and not-resized? (empty? overlapped-indexes-and-paths)))))


(defn resize! [db [_ path area]]
  (if (resize-active? db)
    (if (helpers/is-impossible-config? db)
      (resize-to-new-layer db area)
      (resize-to-this-layer db area))
    db))
