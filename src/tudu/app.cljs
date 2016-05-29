(ns tudu.app
  (:require [om.next :as om :refer-macros [defui]]
            [tudu.components :as components]
            [tudu.parsing :refer [parser]])
  (:require-macros
    [tudu.macros :refer [inspect]]))

(def sample-tasks
  [["create todo app" true]
   ["implement undo" true]
   ["write README" true]
   ["schedule interview" false]
   ["get hired" false]

   ])

(defonce initial-data
  (letfn [(maker [id [desc completed?]]
                 {:todo/id id
                  :todo/description desc
                  :todo/completed? completed?})]
    {:todos (into []
                  (map maker (range) sample-tasks))
     :todos/archived []}))

(def Root components/App)

(def reconciler
  (om/reconciler {:parser parser
                  :state initial-data}))
