(ns jamatodo.parsing
  (:require [om.next :as om :refer-macros [defui]])
  (:require-macros
    [jamatodo.macros :refer [inspect]]))

(defmulti read om/dispatch)
#_(defmulti mutate om/dispatch)

(defmethod read :default
  [{:keys [query state] :as env} k params]
  (let [st @state]
    {:value (om/db->tree query (get st k) st)}))

(def parser (om/parser {:read read
                        ;; :mutate mutate
                        }))
