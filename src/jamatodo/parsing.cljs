(ns jamatodo.parsing
  (:require
    [om.next :as om :refer-macros [defui]]
    [cljs.pprint :refer [pprint]])
  (:require-macros
    [jamatodo.macros :refer [inspect]]))

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [query state] :as env} k params]
  (let [st @state]
    {:value (om/db->tree query (get st k) st)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'todo/update
  [{:keys [query state ref] :as env} k {:keys [todo/id] :as props}]
  (let [st @state]
    {:action (fn []
               (swap! state update-in ref merge props)
               )}))

(defmethod mutate 'todo/delete
  [{:keys [query state ref]} k {:keys [todo/id]}]
  (let [st @state]
    {:action (fn []
               (swap! state update :todos
                      (fn [x] (remove (partial = ref) x)))
               )}))

(defmethod mutate 'todo/toggle-check
  [{:keys [query state ref]} k {:keys [todo/id]}]
  (let [st @state]
    {:action (fn []
               (swap! state update-in (conj ref :todo/completed?) not)
               )
     :values {:keys [:todos]}}))

(def parser (om/parser {:read read
                        :mutate mutate
                        }))
