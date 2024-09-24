(ns backend.html.core
  (:require [backend.filesystem.core :as filesystem]
            [cljs.core.async :refer [<! go]] 
            [reagent.dom.server :as reagent-dom]
            ))

(def version (atom ""))

(go (reset! version (<! (filesystem/get-file "VERSION"))))

(defn with-version [url]
  (str url "?version=" @version))

(defn css [url]
  [:link {:type    "text/css"
          :href    url
          :rel     "stylesheet"}])

(defn js [url]
  [:script {:type    "text/javascript"
            :src     url}])

(defn js-anon [url]
  [:script {:type            "text/javascript"
            :src             url
            :cross-origin    "anonymous"}])

(defn loading-animation-style []
  [:style "#loading-container { 
   
    background: purple;
    animation: rainbow 4s ease infinite;}

@-webkit-keyframes rainbow {
  0% { background: #FFB6C1; } /* pastel pink */
  16.67% { background: #FFD700; } /* pastel yellow */
  33.33% { background: #98FB98; } /* pastel green */
  50% { background: #87CEFA; } /* pastel blue */
  66.67% { background: #FFA07A; } /* pastel orange */
  83.33% { background: #FFC0CB; } /* pastel rose */
  100% { background: #D8BFD8; } /* pastel purple */
}
           #logo-animation .inner {animation: pulse 1s infinite ease-in-out alternate;}

              @keyframes pulse {
                          from { transform: scale(0.8); }
                          to { transform: scale(1.2); }}"])

(defn loading-animation-component []
  [:div#loading-container
   {:style {:display :flex 
            :justify-content :center 
            :align-items :center
            :position :fixed
            :height "100dvh" 
            :width "100dvw"
            :z-index 10000}}
   [:div#logo-animation
    {:style    {;:background         "#333"
                :display            :flex 
                :font-family        "sans-serif"
                :font-weight        :bold
                :color              :white
                :flex-direction     :column
                :justify-content    :center
                :align-items        :center
                :position           :fixed
                :z-index            1000}}
    [:div.inner {:style {:display :flex
                         :justify-content :center
                         :align-items :center
                         :gap 10
                         :font-size "16px"
                         :flex-direction :column
                         :color "#333"
                         
                         :max-width    "70vw"
                         :text-align :center 
                         :line-height "1.5"}}
     [:img {:style    {:width        "300px"
                       :max-width    "80vw"}
            :src      "/images/logo.webp"}]]]])

(defn home-page-css []
  [:<> 
   [css (with-version "/css/normalize.css")]
   [css (with-version "/css/wizard.css")] 
   [css (with-version "/css/tippy.css")]
   [css (with-version "/css/generator.css")] 
   ])


(defn home-page-js []
  [:<>
   [js-anon  "/external-js/fontawesome-2024-01-06.js"]
   [js       "/external-js/iro.js"]
   [js       (with-version "/js/libs/node-modules.js")]
   [js       (with-version "/js/core.js")]])


(defn basic-head-content []
  [:<>
   [:title "Pentacard"]
   [:link {:rel     "icon"
           :type    "image/png"
           :href    "/images/favicon.png"}]
   [:link {:rel "manifest" :href "/manifest.json"}]
   [:meta {:charset    "utf-8"}]
   [:meta {:name       "viewport"
           :content    "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"}]])


(defn app-div []
  [:div#app {:style {:position :relative}}])


(defn body-container [content]
  [:body#body {:style {:position :fixed
                       :top 0 
                       :left 0
                       :width "100%"
                       :height "100%"
                       :overflow :hidden
                       :margin 0
                       :background :transparent}}
   content])

(defn home-page-html []
  [:html
   [:head
    [basic-head-content]] 
   [body-container
    [:<> 
     [loading-animation-style]
     [loading-animation-component] 
     [home-page-css]
     [app-div]
     [home-page-js]]]])


(defn home-page [^js req ^js res]
  (.send res 
         (reagent-dom/render-to-static-markup [home-page-html])))
