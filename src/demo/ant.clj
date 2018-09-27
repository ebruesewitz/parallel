(ns demo.ant
  (:require [demo.util :refer [bound rank-by roulette]]
            [demo.world :as world]))

(defn drop-food [place]
  ;"increase food in place by 1, then remove food from ant"
  (-> place (update :food inc) (update :ant dissoc :food)))

; LIS & TESSA: create function to determine if this place is a home
(defn is-home [place]
  (or (:home place) (:home2 place))
)

; LIS & TESSA: create function to determine if pheremone A is allowed to be left behind currently
(defn is-pher-a-allowed [place ant]
  (not (or (is-home place) (= (:colony ant) "B")))
)

; LIS & TESSA: create function to determine if pheremone B is allowed to be left behind currently
(defn is-pher-b-allowed [place ant]
  (not (or (is-home place) (= (:colony ant) "A")))
)

; LIS & TESSA: update the places pheromones in the space if they are allowed to be updated.
(defn trail [place]
  (-> place (update :phera #(if (is-pher-a-allowed place (:ant place)) (inc %) %)) (update :pherb #(if (is-pher-b-allowed place (:ant place)) (inc %) %)) (dissoc :ant)))

(defn move [from-place to-place]
  [(trail from-place)
   (assoc to-place :ant (:ant from-place))])

(defn take-food [place]
  (-> place (update :food dec) (assoc-in [:ant :food] true)))

(defn turn [place amount]
  (update-in place [:ant :dir] (comp (partial bound 8) +) amount))

; LIS & TESSA: turn this into a function that takes in which pheromone type should be used for ranking
(defn rank-by-pher [pher] (partial rank-by pher))

; LIS & TESSA: turn this into a function that takes in which home should be used for ranking homes
(defn rank-by-home [home] (partial rank-by #(if (home %) 1 0)))

(def rank-by-food (partial rank-by :food))


; LIS & TESSA: turn this into a function that takes in which pheromone we should be following while foraging
(defn foraging [pher] (juxt rank-by-food (rank-by-pher pher)))

; LIS & TESSA: turn this into a function that takes in which pheromone and home we should be following while homing
(defn homing [home pher] (juxt (rank-by-home home) (rank-by-pher pher)))

(def turn-around #(turn % 4))

(defn rand-behavior [config world behavior place]
  (let [[ahead ahead-left ahead-right :as nearby] (world/nearby-places config world (:location place) (get-in place [:ant :dir]))
        ranks (apply merge-with + (behavior nearby))
        actions [#(move % ahead) #(turn % -1) #(turn % 1)]
        index (roulette [(if (:ant ahead) 0 (ranks ahead))
                         (ranks ahead-left)
                         (ranks ahead-right)])]
    ((actions index) place)))

; LIS & TESSA: pull up the behavior logic specific to colony so that we can reuse this code in behave. we could probably name
; this function something a bit more specific
(defn colonyBehavoir [config world place ahead pher home]
  (if (get-in place [:ant :food])
        (cond
          (home place) (-> place drop-food turn-around)
          (and (home ahead) (not (:ant ahead))) (move place ahead)
          :else (rand-behavior config world (homing home pher) place))
        (cond
          (and (pos? (:food place)) (not (home place))) (-> place take-food turn-around)
          (and (pos? (:food ahead)) (not (home ahead))) (move place ahead)
          :else (rand-behavior config world (foraging pher) place))
  )
)

; LIS & TESSA: use the colonyBehavior function above with phera attributes if the colony is A and pherb attributes
; if the colony is B
(defn behave [config world place]
  (let [[ahead & _] (world/nearby-places config world (:location place) (get-in place [:ant :dir]))]
    (if (= (get-in place [:ant :colony]) "A")
      (colonyBehavoir config world place ahead :phera :home )
      (colonyBehavoir config world place ahead :pherb :home2 )
    )
  )
)
