(ns jamatodo.app
  (:require [om.next :as om :refer-macros [defui]]
            [jamatodo.components :as components]
            [jamatodo.parsing :refer [parser]])
  (:require-macros
    [jamatodo.macros :refer [inspect]]))

(def sample-tasks
  ["do dishes"
   "do the hustle"
   "find 12 eggs"
   ])

(def initial-data
  (letfn [(maker [id desc]
                 {:todo/id id
                  :todo/description desc
                  :todo/completed? false})]
    {:todos (into []
                  (map maker (range) sample-tasks))
     :todos/archived []}))

(def Root components/App)

(def reconciler
  (om/reconciler {:parser parser
                  :state initial-data}))
