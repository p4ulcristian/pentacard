(ns frontend.re-frame.events.components.add
  (:require [frontend.starter-kit.utils.basic :as utils]
            [frontend.wizard.utils.layers :as layers]
            [frontend.wizard.utils.areas :as areas]
            [frontend.re-frame.events.components.event-helpers :as helpers]))


(defn add-to-new-layer [db component]
  (let [path              (helpers/get-deepest-overlapped-path db)
        new-layer-key     (utils/generate-keyword)
        new-layer-path    (helpers/get-new-layer-path path new-layer-key)
        new-layer-areas   (helpers/add-to-resetted-areas db path)
        new-layer-areas-fixed-order (areas/fix-order new-layer-areas)]
    (-> db
        (layers/duplicate-selected-layer  path new-layer-key)
        (helpers/set-new-areas       new-layer-path new-layer-areas-fixed-order)
        (helpers/add-node            new-layer-path  component)
        (helpers/select-new-layer    path new-layer-key))))


(defn add-to-this-layer [db component]
  (let [path                (helpers/get-deepest-overlapped-path db)
        new-areas           (helpers/add-to-areas db path)
        new-areas-fixed-order (areas/fix-order new-areas)]
    (-> db
        (helpers/remove-unused-nodes path new-areas)
        (helpers/set-new-areas path new-areas-fixed-order)
        (helpers/add-node  path  component))))


(defn add!  [db [_ component]]
  (let [impossible-config?           (helpers/is-impossible-config? db)]
    (if impossible-config?
      (add-to-new-layer  db component)
      (add-to-this-layer db component))))