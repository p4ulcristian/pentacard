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
            [re-frame.core :refer [dispatch subscribe]] 
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



(def pi (.-PI js/Math))
(println "ghedsadsa" js/Math)


(defn card [{:keys [x y z rotation color ^js object]}]
  (let [scene (.-scene object)
        [clicked? set-clicked?] (react/useState false)
        spring-obj (useSpring #js {:scale (if clicked? 2 1)})
        scale (.-scale spring-obj)
        
        copied-scene (react/useMemo (fn [] (.clone scene)) 
                                    #js [scene])] 
    [:> (.-mesh animated) {:rotation [0 0 rotation]
                           :position [x y (if clicked? -100 z)]
                           :scale scale
                           :onPointerDown (fn [e] 
                                            (.stopPropagation e)
                                            (.log js/console "hello card: " x)
                                            (set-clicked? true))
                           :onPointerUp (fn [] (set-clicked? false))} 
     [:group {:position [-15 -27 0]}
      [:mesh 
       [:BoxGeometry {:args [10 10 10]}]
       [:meshBasicMaterial {:color color}]]
      [render-one-letter "A"]]
                     
     [:primitive {:object copied-scene
                  :scale 20
                  
                  :receiveShadow true}]]))

            

    



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

(defn pentagon-plane [index radius position rotation]
  (let [side-length (side-length-from-radius radius)]
    [:group {:position position
             :rotation [(/ pi 2) 0 rotation]} 
     [player-board "lightgreen"] 
     [:> Html index]
     [cards/player-cards position]]))


(defn pentagon-points
  "Calculate the coordinates of the vertices of a regular pentagon,
   relative to the center of the pentagon.
   side-length: the length of each side.
   Returns a list of [x y] coordinates."
  [side-length]
  (let [n 5
        angle-increment  (/ (* 2 Math/PI) n)
        radius           (/ side-length (* 2 (Math/sin (/ Math/PI n))))]
    (for [i (range n)]
      (let [angle (- (* i angle-increment) (/ Math/PI 2)) ; rotate to align first point vertically
            x (* radius (Math/cos angle))
            z (* radius (Math/sin angle))]
        [x 0 z]))))

;; Usage
(println (pentagon-points 5))

(defn pentagon [radius] 
  [:group 
   {:rotation [0 pi 0]}
   (map-indexed
    (fn [i coordinate] [pentagon-plane i radius coordinate (* i 
                                                            (* 72 (/ pi 180)))]) 
    (pentagon-points radius))])
   
  
  

(defn mountains []
  (let [object (useLoader GLTFLoader "/models/mountain.glb")]
    (let [scene (.-scene object)
          texture (useTexture "/images/logo.webp")
          copied-scene (react/useMemo (fn [] (.clone scene))
                                      #js [scene])]
      (.log js/console object)
      [:> (.-mesh animated) {:rotation [0 0 0]
                             :position [0 -3 0]
                             :scale [0.01 0.01 0.01]}
                              
       [:primitive {:object copied-scene
                    :scale 20 
                    
                    :receiveShadow true}]])))

(defn lights []
  (let [directional-light-ref (react/useRef)]
    (useHelper directional-light-ref PointLightHelper)
    [:group
     [:pointLight {:ref directional-light-ref
                   :intensity 0.7
                   :color "#fff"
                   :castShadow true
                   :position [0 0.5 0]
                   :shadow-mapSize [1024 1024]}]
     [:pointLight {:ref directional-light-ref
                   :intensity 1
                   :color "#fff"
                   :castShadow true
                   :position [0 -0.3 0]
                   :shadow-mapSize [1024 1024]}]]))


(defn view [] 
  [:div
   [:div {:style {:background :white}} (str @(subscribe [:db/get [:objects]]))]
   [:> Canvas {
               :camera {:dpr [1 2]
                        :fov 75 
                        :near 0.1 
                        :far 1000
                        :position [0 0.8 0.6]}} 
    [:> OrbitControls]
    [:> GizmoHelper 
     {:alignment "bottom-right"}
     [:> GizmoViewport]]
    [:> Sky] 
    [lights] 
    [deck/decks]
    [mountains]
    [:> Grid]
    [pentagon 0.5]]])