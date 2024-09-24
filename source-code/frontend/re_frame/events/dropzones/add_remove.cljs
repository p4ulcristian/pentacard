(ns frontend.re-frame.events.dropzones.add-remove)



(def dropzone-path [:overlays :areas :area-dropzones])


(defn add! [db [_  id e]]
  (let [dropzones       (get-in db dropzone-path {})
        new-dropzones   (assoc dropzones id e)]
    (assoc-in db dropzone-path new-dropzones)))


(defn remove! [db [_  id]]
  (let [dropzones       (get-in db dropzone-path {})
        new-dropzones   (dissoc dropzones id)]
    (assoc-in db dropzone-path new-dropzones)))