(ns config)

;; Frontend

(def default-scale 0.7)
(def area-boundary 1)
(def grid-boundary 0.5)

(defn number-to-editor-mode [number]
  (cond
    (< number grid-boundary) :section
    (< number area-boundary) :grid
    (< area-boundary number) :area))

(defn editor-mode->number [mode]
  (case mode
    :section (- grid-boundary 0.1)
    :grid (+ grid-boundary 0.1)
    :area 1.01))


;; Backend

(def port 4000)

(def privy-app-id "clmkp2zb003r5mq0fqx8a0efa")
(def privy-app-secret "3RfznuRdNFopoSnAZVvUJXm4NzhdWtuuExSwMyBhyeuJKfPuSj185cjRpQR9Ctjz9bTEdTjaEMFraFyBTvmyTdNC")
