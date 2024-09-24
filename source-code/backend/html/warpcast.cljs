(ns backend.html.warpcast
  (:require ["node:process" :as process]
            [cljs.reader :as cljs.reader]
            [config   :refer [port]]
            [backend.env :as env]
            [reagent.dom.server :as reagent-dom]))
        
(defn with-random-version [url]
  (str url "?v=" (js/Date.now)))


(def host 
  (let [railway-public-domain env/RAILWAY_PUBLIC_DOMAIN]
    (if railway-public-domain
      (str "https://" railway-public-domain) 
      (str "http://localhost:" port))))
  


(defn with-host [url]
  (str host url))


(defn button [{:keys [label type target index]}]
  (let [button-basic-property        (str "fc:frame:button:" index)
        button-action-property (str "fc:frame:button:" index ":action")
        button-target-property (str "fc:frame:button:" index ":target")]
    [:<> 
     [:meta {:property button-basic-property :content  label}]
     [:meta {:property button-action-property :content type}]
     [:meta {:property button-target-property :content target}]]))




(defn button-configs [{:keys [slide-index slide-count snapshot-id]}]
  (let [next-slide (if (= slide-index slide-count)
                     nil (inc slide-index))
        prev-slide (if (= slide-index 1) nil (dec slide-index))
        slide-url  (with-host (str "/scroll-snapshot/" snapshot-id "/" slide-count "/"))
        prev-slide-url (str slide-url prev-slide)
        next-slide-url (str slide-url next-slide)]
    (remove nil?
     [(when prev-slide {:label   "←"
                        :index   1
                        :type    "post"
                        :target  prev-slide-url})
      {:label "Magic ✨"
       :index 2
       :type "link"
       :target  (with-host (str "/scroll-snapshot/"  snapshot-id))}
      (when next-slide {:label   "→"
                        :index   3
                        :type    "post"
                        :target  next-slide-url})])))

(defn slider-buttons [slider-config] 
  [:<>
   (map-indexed 
    (fn [index config] ^{:key index} [button (assoc config :index (inc index))])
    (button-configs slider-config))])
 

(defn html-wrapper [warpcast-meta]
  [:html
   [:head warpcast-meta] 
   [:body "Hello warpcast"]])


(defn og-tags-component [{:keys [img-url  snapshot-id]}]
  (let [og-tags {:title "Wizard"
                 :type "website"
                 :image img-url
                 :url (with-host (str "/scroll-snapshot/" snapshot-id))
                 :description "A page casted by a wizard."
                 :site-name "Spell"
                 :locale "en_US"}] 
    [:<>
     [:meta {:property "og:title" :content (og-tags :title)}]
     [:meta {:property "og:type" :content (og-tags :type)}]
     [:meta {:property "og:image" :content (og-tags :image)}]
     [:meta {:property "og:url" :content (og-tags :url)}]
     [:meta {:property "og:description" :content (og-tags :description)}]
     [:meta {:property "og:site_name" :content (og-tags :site-name)}]
     [:meta {:property "og:locale" :content (og-tags :locale)}]]))

(defn frame-and-image-meta [{:keys [snapshot-id image-path]}]
  [:<>
   [:meta {:property "fc:frame" :content "vNext"}] 
   [:meta {:property "fc:frame:image" :content (with-random-version (with-host image-path))}]
   ; Not a logical placement, but to be honest, it's all opengraph.
   [og-tags-component {:img-url (with-random-version (with-host image-path))
                       :snapshot-id    snapshot-id}]])

(defn next-frame [{:keys [slide-path 
                          slide-index 
                          slide-count 
                          snapshot-id]}]
  [:<>
  ;;  [:meta {:property "fc:frame:state"
  ;;          :content  (str (js/encodeURIComponent
  ;;                          (.stringify js/JSON (clj->js {:a "b"}))))}]  
   [frame-and-image-meta {:image-path (str slide-path (str slide-index ".png")) 
                          :snapshot-id snapshot-id}]
   [slider-buttons {:slide-index slide-index 
                    :slide-count slide-count 
                    :snapshot-id snapshot-id}]])



(defn slider-next-frame [^js req ^js res]
  (let [params         (.-params req)
        snapshot-id    (.-snapshot_id params)
        slide-count    (.-slide_count params)
        slide-index    (.-slide_index params) 
        slide-count-number (cljs.reader/read-string slide-count)
        slide-index-number (cljs.reader/read-string slide-index)]  
    (.send res
           (reagent-dom/render-to-static-markup
            [html-wrapper
             [next-frame {:slide-path  (str "/screenshots/"  snapshot-id "/")
                          :slide-index slide-index-number
                          :slide-count slide-count-number
                          :snapshot-id snapshot-id}]]))))


(defn slider-initial-frame [{:keys [slide-count slide-path 
                                    snapshot-id] :as slide-config}]
  (let [initial-frame-url (str slide-path "1.png")]
    [:<>
     [frame-and-image-meta {:image-path initial-frame-url
                            :snapshot-id snapshot-id}]
     [slider-buttons {:slide-count slide-count 
                      :slide-index 1 
                      :snapshot-id snapshot-id}]]))

