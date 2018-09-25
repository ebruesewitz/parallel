(ns demo.domain)

(defrecord Ant
  [dir agent food colony])

(defrecord Cell
  [ant food home location phera pherb])

(defn build-cell [args]
  (map->Cell (merge {:food 0 :phera 0 :pherb 0 :home false :home2 false :location [0 0]} args)))

(defn build-ant [args]
  (map->Ant args))
