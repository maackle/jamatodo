(ns jamatodo.parsing
  (:require [om.next :as om :refer-macros [defui]])
  (:require-macros
    [jamatodo.macros :refer [inspect]]))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :default
  [{:keys [query state] :as env} k params]
  (let [st @state]
    {:value (om/db->tree query (get st k) st)}))

(defmethod mutate 'todo/delete
  [{:keys [query state]} k {:keys [todo/id]}]
  (let [st @state]
    (inspect id)
    {:action (fn []
               (swap! state update :todos
                      (fn [x] (remove (partial = [:todo/by-id id]) x)))
               )
     :value {:keys [:todos]}}))

(def parser (om/parser {:read read
                        :mutate mutate
                        }))
