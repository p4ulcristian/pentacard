(ns frontend.pentacard.drawing-deck
  (:require ["@react-three/drei" :refer [useTexture]]
            ["@react-three/fiber" :refer [useLoader useLoader]]
            ["react" :as react]
            ["three" :as THREE :refer [DoubleSide]]
            ["three/addons/geometries/TextGeometry.js" :refer [TextGeometry]]
            ["three/addons/loaders/FontLoader.js" :refer [FontLoader]]
            [re-frame.alpha :refer [dispatch subscribe]]))



(defn render-one-letter [letter]
  (let [text-ref  (react/useRef)
        ;a (.log js/console "h " FontLoader) 
        font (useLoader FontLoader "/fonts/font.json")
        text-geometry (new TextGeometry letter #js {:font font
                                                    :size 0.04
                                                    :height 0.005}) 
        text-material (THREE/MeshPhongMaterial. #js {:color "#0F0"})] 
    [:mesh {:ref text-ref
            :geometry text-geometry
            :material text-material}]))

(defn card-texture []
  (let [texture (useTexture "/images/logo.webp")]
    [:mesh
     [:BoxGeometry {:castShadow true
                    :receiveShadow true
                    :args [0.1 0.1 0.005]}]
     [:meshPhongMaterial {:map texture
                          :side DoubleSide}]]) )

(defn one-card [card [x y z]]
  (let [[card-id card-data] card
        {:keys [rank suit suit-emoji origin ref index]} card-data
        ref (react/useRef)]
    (react/useEffect (fn [] 
                       (dispatch [:db/set [:cards card-id :ref] ref])
                       (fn []))
                     #js []) 
    [:group {:rotation [0 0 0]
             :position [x y (* index 0.01)]
             :ref ref}
     [card-texture]
     [render-one-letter (str index)]]))


(defn view []
  (let [deck (subscribe [:db/get [:drawing-deck :cards]])
        position [-0.1 0 0]]
    [:group
     (map
      (fn [[card-id card-data]] ^{:key card-id} [one-card [card-id card-data] position])
      @deck)]))

