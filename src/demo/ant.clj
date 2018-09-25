(ns demo.ant
  (:require [demo.util :refer [bound rank-by roulette]]
            [demo.world :as world]))

(defn drop-food [place]
  ;"increase food in place by 1, then remove food from ant"
  (-> place (update :food inc) (update :ant dissoc :food)))

(defn no-pher-a [place ant]
  (or (:home place) (:home2 place) (= (:colony ant) "B"))
)

(defn no-pher-b [place ant]
  (or (:home place) (:home2 place) (= (:colony ant) "A"))
)

(defn trail [place]
  (-> place (update :phera #(if (no-pher-a place (:ant place)) % (inc %))) (update :pherb #(if (no-pher-b place (:ant place)) % (inc %))) (dissoc :ant)))

(defn move [from-place to-place]
  ; "returns an array with updated from-place and to-place"
  ; "remove ant from from-place and increase pheremones there"
  [(trail from-place)
  ;  "set the ant in the to place to be from-place.ant"
   (assoc to-place :ant (:ant from-place))])

(defn take-food [place]
  ; "update the place.ant.food to be true, decrease food from place.food"
  (-> place (update :food dec) (assoc-in [:ant :food] true)))

(defn turn [place amount]
  ; "update place.ant.dir to be amount?"
  (update-in place [:ant :dir] (comp (partial bound 8) +) amount))

(def rank-by-phera (partial rank-by :phera))
(def rank-by-pherb (partial rank-by :pherb))
; "if this is home, return 1, otherwise return 0"
(def rank-by-home (partial rank-by #(if (:home %) 1 0)))
(def rank-by-home2 (partial rank-by #(if (:home2 %) 1 0)))
; "rank by amount of food"
(def rank-by-food (partial rank-by :food))
; rank by amount of food and pher
(def foraginga (juxt rank-by-food rank-by-phera))
(def foragingb (juxt rank-by-food rank-by-pherb))
; rank by home and pher
(def homing (juxt rank-by-home rank-by-phera))
(def homing2 (juxt rank-by-home rank-by-pherb))
(def turn-around #(turn % 4))

(defn rand-behavior [config world behavior place]
  (let [[ahead ahead-left ahead-right :as nearby] (world/nearby-places config world (:location place) (get-in place [:ant :dir]))
        ranks (apply merge-with + (behavior nearby))
        actions [#(move % ahead) #(turn % -1) #(turn % 1)]
        index (roulette [(if (:ant ahead) 0 (ranks ahead))
                         (ranks ahead-left)
                         (ranks ahead-right)])]
    ((actions index) place)))

(defn behave [config world place]
  (let [[ahead & _] (world/nearby-places config world (:location place) (get-in place [:ant :dir]))]
    (if (= (get-in place [:ant :colony]) "A")
      (if (get-in place [:ant :food])
        (cond
          (:home place) (-> place drop-food turn-around)
          (and (:home ahead) (not (:ant ahead))) (move place ahead)
          :else (rand-behavior config world homing place))
        (cond
          (and (pos? (:food place)) (not (:home place))) (-> place take-food turn-around)
          (and (pos? (:food ahead)) (not (:home ahead))) (move place ahead)
          :else (rand-behavior config world foraginga place)))
      (if (get-in place [:ant :food])
        (cond
          (:home2 place) (-> place drop-food turn-around)
          (and (:home2 ahead) (not (:ant ahead))) (move place ahead)
          :else (rand-behavior config world homing2 place))
        (cond
          (and (pos? (:food place)) (not (:home2 place))) (-> place take-food turn-around)
          (and (pos? (:food ahead)) (not (:home2 ahead))) (move place ahead)
          :else (rand-behavior config world foragingb place))))))
