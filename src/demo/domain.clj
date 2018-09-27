(ns demo.domain)

; LIS & TESSA: update Ant to also have a colony
(defrecord Ant
  [dir agent food colony])

; LIS & TESSA: update a cell to have both homes and both pheromones
(defrecord Cell
  [ant food home home2 location phera pherb])

; LIS & TESSA: initialize cell with none of either pheromone and both homes set to false
(defn build-cell [args]
  (map->Cell (merge {:food 0 :phera 0 :pherb 0 :home false :home2 false :location [0 0]} args)))

(defn build-ant [args]
  (map->Ant args))
