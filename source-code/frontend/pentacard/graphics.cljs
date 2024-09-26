(ns frontend.pentacard.graphics
  (:require ["@react-three/cannon" :refer [Physics usePlane useBox useCylinder useRaycastVehicle]]
            ["@react-three/drei" :refer [useGLTF Sky Environment PerspectiveCamera Html RoundedBox Box, OrbitControls
                                         useBoundingBox
                                         Center
                                         useTexture
                                         GizmoViewport
                                         useHelper
                                         GizmoHelper
                                         Text Text3D]]
            [frontend.pentacard.controllers.core]
            ["react" :as react]
            [frontend.starter-kit.utils.basic :as starter-kit]
            [frontend.pentacard.cards :as cards]
            [frontend.pentacard.deck  :as deck]
            ["@react-three/drei" :refer [Box Plane Grid]]
            ["@react-three/fiber" :refer [useLoader Canvas useFrame useLoader]]
            ["react" :refer [useRef Suspense useEffect useMemo]]
            ["@react-spring/three" :refer [useSpring useSpringValue, animated]]
            ["three" :as THREE :refer [DoubleSide DirectionalLightHelper PointLightHelper]]
            ["three/addons/loaders/FontLoader.js" :refer [FontLoader]]
            ["three/addons/geometries/TextGeometry.js" :refer [TextGeometry]]
            ["three/addons/utils/BufferGeometryUtils.js" :as BufferGeometryUtils :refer [mergeGeometries]]
            ["three/addons/loaders/GLTFLoader.js" :refer [GLTFLoader]]
            ["three/src/loaders/TextureLoader.js" :refer [TextureLoader]]
           ; ["three/examples/jsm/utils/BufferGeometryUtils.js" :refer [BufferGeometryUtils]]
            [re-frame.alpha :refer [dispatch subscribe]] 
            [reagent.core :as r]))


(defn render-one-letter [letter]
  (let [text-ref  (useRef) 
        font (useLoader FontLoader "/fonts/font.json") 
        text-geometry (new TextGeometry letter #js {:font font
                                                    :size 1.2
                                                    :height 0.1}) 
        text-material (THREE/MeshPhongMaterial. #js {:color "#333"})]
    
    [:mesh {:ref text-ref
            :geometry text-geometry
            :material text-material 
            :position [0 0 -10]
            :scale 5}]))



(defn side-length-from-radius
  "Calculate the side length of a regular pentagon given its radius."
  [radius]
  (* 2 radius (Math/sin (/ Math/PI 5))))

(defn player-board [color]
  [:mesh {}
   [:planeGeometry {:castShadow true
                    :receiveShadow true
                    :args [0.29 0.29]}]
   [:meshPhongMaterial {:color color
                        :side DoubleSide}]])

(defn pentagon-plane  [index radius position rotation]
  (let [side-length (side-length-from-radius radius)]
    [:group {:position position
             :rotation [0 0 rotation]} 
     [player-board "lightgreen"] 
     [:> Html index]
     [cards/player-cards position]]))




;; Usage

(defn pentagon [] 
  (let [pentagon-points (subscribe [:db/get [:positions :pentagon :points]])]
    [:group 
     {:rotation [0 (.-PI js/Math) 0]}
     (map-indexed
      (fn [i coordinate] [pentagon-plane i 0.5 coordinate (* i 
                                                                (* 72 (/ (.-PI js/Math) 180)))]) 
      @pentagon-points)]))
   
  
  

(defn mountains []
  (let [object (useLoader GLTFLoader "/models/mountain.glb")]
    (let [scene (.-scene object)
          texture (useTexture "/images/logo.webp")
          copied-scene (react/useMemo (fn [] (.clone scene))
                                      #js [scene])]
      
      [:> (.-mesh animated) {:rotation [0 0 0]
                             :position [0 -3 0]
                             :scale [0.01 0.01 0.01]}
                              
       [:primitive {:scale 20 
                    
                    :receiveShadow true}]])))

(defn lights []
  (let [directional-light-ref (react/useRef)]
    ;(useHelper directional-light-ref PointLightHelper)
    [:group
     [:pointLight {:ref directional-light-ref
                   :intensity 0.7
                   :color "#fff"
                   :castShadow true
                   :position  [0 0 -0.6]
                   :shadow-mapSize [1024 1024]}]
     [:pointLight {:ref directional-light-ref
                   :intensity 1
                   :color "#fff"
                   :castShadow true
                   :position  [0 0 -0.6]
                   :shadow-mapSize [1024 1024]}]]))


(defn state-viewer []
  (let [data @(subscribe [:db/get []])
        filter-vector []
        filtered-data (get-in data filter-vector)]
    [:div {:style {:opacity 0.3
                   :position :fixed 
                   :z-index 1000}}
     [:button {:on-click #(dispatch [:game/draw!])}
      "Draw card"]
     [:button {:on-click #(dispatch [:game/start!])}
      "Start game"]
     [:button {:on-click #(dispatch [:game/discard!])}
      "Discard card"]
     ;[:div (str "ekjadhsj" @(subscribe [:area :garage]))]
     [:pre {:style {:background :white
                    :height "200px"
                    :overflow-y :scroll}}
      (starter-kit/pretty-print-string 
       filtered-data)]]))
   
  

(defn view [] 
  [:div
   [state-viewer]
   [:> Canvas {
               :camera {:dpr [1 2]
                        :fov 75 
                        :near 0.1 
                        :far 1000
                        :position [0 -0.1 -1]}} 
    [:> OrbitControls]
    ;; [:> GizmoHelper 
    ;;  {:alignment "bottom-right"}
    ;;  [:> GizmoViewport]]
    [:> Grid]
    [:> Sky] 
    [lights] 
    [deck/cards]
    ;[mountains]
    
    [pentagon]]])