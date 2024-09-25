(ns frontend.pentacard.deck
  (:require
   ["@react-three/fiber" :refer [useLoader Canvas useFrame useLoader]]
   ["react" :as react]
   [re-frame.core :refer [subscribe dispatch]] 
   ["three" :as THREE :refer [DoubleSide]]
   ["@react-three/drei" :refer [Box Plane Grid]]
   ["@react-three/drei" :refer [useGLTF Sky Environment PerspectiveCamera Html RoundedBox Box, OrbitControls
                                useBoundingBox
                                useTexture
                                Center
                                GizmoViewport
                                GizmoHelper
                                Text Text3D]] 
   ["three/addons/loaders/FontLoader.js" :refer [FontLoader]]
   ["three/addons/geometries/TextGeometry.js" :refer [TextGeometry]]))


(defn render-one-letter [letter]
  (let [text-ref  (react/useRef)
        ;a (.log js/console "h " FontLoader) 
        font (useLoader FontLoader "/fonts/font.json")
        text-geometry (new TextGeometry letter #js {:font font
                                                    :size 0.01
                                                    :height 0.005})
                                               ;:curveSegments 12})
                                               ;:bevelEnabled true
                                               ;:bevelThickness 0.1
                                               ;:bevelSize 0.2
                                               ;:bevelOffset 0
                                               ;:bevelSegments 1}) 
        text-material (THREE/MeshPhongMaterial. #js {:color "#fff"})]

    [:mesh {:ref text-ref
            :geometry text-geometry
            :material text-material
            :position [0 0 0]}]))

(defn one-card [index card]
  (let [[card-id card-data] card
        {:keys [rank suit suit-emoji]} card-data
        ref (react/useRef)
        texture (useTexture "/images/logo.webp")]
    (react/useEffect (fn []
                       (dispatch [:db/set [:cards card-id :ref] ref] )
                       (fn []))
                     #js [])
    [:group { :position [0
                         (* 0.006 index)
                         0]
             :scale [1 1 1]
             :rotation [(- (/ (.-PI js/Math) 2)) 0 0]}
     [:mesh {:ref ref}
      [:BoxGeometry {:castShadow true 
                     :receiveShadow true
                     :args [0.1 0.1 0.005]}]
      [:meshPhongMaterial {:color "orange"
                           :map texture
                           :side DoubleSide}]]
     [:group 
      {:position [0 0.04 0]}
      [render-one-letter suit-emoji]]
     [:group
      {:position [0.02 0.04 0]}
      [render-one-letter rank]]
     ]))

(defn drawing-deck []
  (let [deck (subscribe [:db/get [:cards]])]
    [:group 
    {:position [-0.1 0 0]}
   ;;   [:> Html [:div {:style {:background :white}} 
   ;;             (str @deck)]]
     (map-indexed 
      (fn [i a] [one-card i a])
      @deck)]))



(defn discard-deck []
  (let [deck (subscribe [:db/get [:drawing-deck]])
        ref (react/useRef)]
    (react/useEffect 
     (fn []
       (dispatch [:db/set [:objects :discard-deck :ref] ref])
       (fn [] (dispatch [:db/unset [:objects :discard-deck]]))))
    [:group 
     {:position [0.1 0 0]
      :ref ref}]))
   ;;   [:> Html [:div {:style {:background :white}} 
   ;;             (str @deck)]]
   ;;   (map-indexed
   ;;    (fn [i a] [one-card i a])
   ;;    @deck)]))

(defn decks []
  [:group
   [drawing-deck]
   [discard-deck]])