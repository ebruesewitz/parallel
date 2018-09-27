(ns demo.ui.ant
  (:require [demo.ui.core :as ui]
            [demo.util :as util]))

(def directions
  {0 [2 0 2 4]
   1 [4 0 0 4]
   2 [4 2 0 2]
   3 [4 4 0 0]
   4 [2 4 2 0]
   5 [0 4 4 0]
   6 [0 2 4 2]
   7 [0 0 4 4]})

; LIS & TESSA: modify the color of the ant to match the colony that its from
(defn ant-colony-color [ant]
  (if (= (:colony ant) "A") :green :blue))

(defn ant-color [ant]
  (if (:food ant) :red (ant-colony-color ant)))

(defn next-loc [dir loc config]
  (-> dir directions (util/delta (util/scale loc (:scale config)))))

(defn render-ant [ant img config x y]
  (ui/make-line img {:color (ant-color ant)
                     :border (next-loc (:dir ant) [x y] config)}))
