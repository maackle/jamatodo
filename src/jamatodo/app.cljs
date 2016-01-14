(ns jamatodo.app
  (:require [om.next :as om :refer-macros [defui]]
            [jamatodo.components :as components]
            [jamatodo.parsing :refer [parser]]))

(def initial-data
  {:todos [{:todo/id 1
            :todo/description "funky"
            :todo/completed? false}]})

(def Root components/App)

(def reconciler
  (om/reconciler {:parser parser
                  :state initial-data}))
