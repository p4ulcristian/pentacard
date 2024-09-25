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
   ["three/addons/geometries/TextGeometry.js" :refer [TextGeometry]]
   ["@react-spring/three" :refer [SpringValue]]))


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


(defn callback-x [^js ref ^js spring-value]
  (let [has-animated? (.-hasAnimated spring-value)
        is-animating? (.-isAnimating spring-value)
        finished?  (and has-animated? (not is-animating?))] 
    (when-not finished?
      (set! (-> ^js ref .-current .-position .-x) (.get spring-value))
      (.requestAnimationFrame js/window #(callback-x ref spring-value)))))

(defn callback-z [^js ref ^js spring-value]
  (let [has-animated? (.-hasAnimated spring-value)
        is-animating? (.-isAnimating spring-value)
        finished?  (and has-animated? (not is-animating?))] 
    (when-not finished?
      (set! (-> ^js ref .-current .-position .-z) (.get spring-value))
      (.requestAnimationFrame js/window #(callback-z ref spring-value)))))

(defn animate-card [ref index from to]
  (let [from-position @(subscribe [:db/get [:positions from]])
        to-position   @(subscribe [:db/get [:positions to]])
        [from-x from-y from-z] from-position
        [to-x to-y to-z] to-position
        x-spring (new SpringValue from-x 
                      #js {:to to-x 
                           :config #js {:mass 2}}) 
        z-spring (new SpringValue from-z
                      #js {:to (* 0.01 index)})]
    (println from-position)
    (.requestAnimationFrame js/window #(callback-x ref x-spring))
    (.requestAnimationFrame js/window #(callback-z ref z-spring))))

(defn one-card [i card]
  (let [[card-id card-data] card
        {:keys [rank suit suit-emoji origin ref index]} card-data
        ref (react/useRef)
        texture (useTexture "/images/logo.webp")
        [old-origin set-old-origin] (react/useState origin)]
    (react/useEffect (fn []
                       (dispatch [:db/set [:cards card-id :ref] ref])
                       (fn []))
                     #js [])
    (react/useEffect (fn []
                       (when (not= old-origin origin) 
                         (animate-card ref index old-origin origin))
                       (fn []))
                     #js [origin]) 
    [:group {:rotation [(- (/ (.-PI js/Math) 2)) 0 0]}
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
      [render-one-letter rank]]]))
     

(defn decks []
  (let [deck (subscribe [:db/get [:cards]])]
    [:group
     (map-indexed 
      (fn [i a] ^{:key i}[one-card i a])
      @deck)]))

