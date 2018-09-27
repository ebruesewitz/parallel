(ns demo.ui.world
  (:require [demo.ui.ant :as ui-ant]
            [demo.world :as world]
            [demo.ui.core :as ui]
            [demo.util :as util]))

(defn food-color [config food]
  (ui/color [255 0 0] food (:food-scale config)))

; LIS & TESSA: create two different colors for the different types of pheromones
(defn pheromone-a-color [config pheromone]
  (ui/color [0 255 0] pheromone (:pher-scale config)))

(defn pheromone-b-color [config pheromone]
  (ui/color [0 0 255] pheromone (:pher-scale config)))

; LIS & TESSA: modify to take in a pheromone type and color
(defn render-place-as-pheromone [img config pher x y color]
  (ui/make-rect img {:color (color config pher)
                     :fill [(* x (:scale config))
                            (* y (:scale config))
                            (:scale config) (:scale config)]}))
                            
(defn render-place-as-food [img config food x y]
  (ui/make-rect img {:color (food-color config food)
                     :fill [(* x (:scale config))
                            (* y (:scale config))
                            (:scale config) (:scale config)]}))

(defn render-home [img {:keys [scale home-off nants-sqrt]}]
  (ui/make-rect img
                {:color :blue
                 :border [(* scale home-off) (* scale home-off)
                          (* scale nants-sqrt) (* scale nants-sqrt)]}))

(defn fill-world-bg [img]
  (ui/make-rect img {:color :white
                     :fill [0 0 (.getWidth img) (.getHeight img)]}))

(defn render-all-places [img config world]
  (doseq [x (range (:dim config)), y (range (:dim config))]
    (let [{:keys [phera pherb food ant]} (-> world (get-in [x y]) deref)]
    ; LIS & TESSA: change the color of the space depending on the kind of pheromone thats there.
      (when (pos? phera) (render-place-as-pheromone img config phera x y pheromone-a-color))
      (when (pos? pherb) (render-place-as-pheromone img config pherb x y pheromone-b-color))
      (when (pos? food) (render-place-as-food img config food x y))
      (when ant (ui-ant/render-ant ant img config x y)))))
