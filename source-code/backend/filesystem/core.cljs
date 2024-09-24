(ns backend.filesystem.core
  (:require ["fs" :as fs]
            [cljs.reader :as cljs.reader]
            [clojure.string :as cljs-str] 
            [cljs.core.async :refer [go <! >! chan]])) 


(defn count-files [folder-path]
  (let [channel (chan)]
    (.readdir fs folder-path
              (fn [err files]
                (go 
                  (if err
                    (>! channel {:error (str "Error reading the directory: " err)
                                 :result 0})
                    (>! channel {:result (or (count files) 0)})))))
    channel))

(defn get-file [file-path]
  (let [channel (chan)]
    (.readFile fs file-path
               (fn [_err data]
                 (go (>! channel (str data)))))
    channel))

(defn get-file-edn [file-path] 
  (let [channel (chan)]
    (.readFile fs file-path
                   (fn [_err data]
                     (let [data (cljs.reader/read-string
                                 (str data))]
                       (when data (go (>! channel data))))))
    channel))



(defn create-directory-if-not-exists [directory-path]
  (let [channel (chan)] 
    (.access fs directory-path
             (fn [err] 
               (if err
                 (.mkdir fs directory-path
                         #js {:recursive true}
                         (fn [err]
                           (go
                             (if err
                               (>! channel {:error  (str "Directory creation failed: " err)
                                            :success false}) 
                               (>! channel {:result "Directory creation succeeded"
                                            :success true}))))) 
                 (go (>! channel {:result "Directory already exists"
                                  :success true})))))
    channel))


(defn file-path->folder-path [file-path]
  (cljs-str/join 
   "/" 
   (drop-last 
    (cljs-str/split file-path #"/"))))


(defn write-file [file-path data]
  (let [channel (chan)]
    (go
      (let [created-folder (<! (create-directory-if-not-exists (file-path->folder-path file-path)))
            folder-success? (:success created-folder)]
        (if (and (string? file-path) (string? data))
          (.writeFile fs file-path data
                      (fn [err _data]
                        (go
                          (if err
                            (>! channel {:success false 
                                         :message (str "Error on write: " err)})
                            (>! channel {:success true 
                                         :message "Write succeeded"})))))
          (>! channel {:success false 
                       :message "Error on write: file-path and data must be strings"}))))
                          
    channel))


(defn remove-file [file-path]
  (let [channel (chan)]
    (.unlink fs file-path
             (fn [_err]
               (go (>! channel {:result (str "Successful deletion of " file-path)}))))
    channel))

(defn remove-folder [folder-path]
  (let [channel (chan)]
    (.rmdir fs folder-path
            #js {:recursive true
                 :force true}
             (fn [err]
               (go (>! channel {:result 
                                (if err
                                  (str "Error while deleting " folder-path ": " err)
                                  (str "Successful deletion of " folder-path))}))))
    channel))