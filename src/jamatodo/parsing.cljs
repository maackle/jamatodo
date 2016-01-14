(ns jamatodo.parsing
  (:require
    [om.next :as om :refer-macros [defui]]
    [cljs.pprint :refer [pprint]]
    [jamatodo.components :as components])
  (:require-macros
    [jamatodo.macros :refer [inspect]]))

(defn gen-uuid
  "Poor man's UUID generator"
  [] (rand-int 99999999))

(def state-history (atom {:stack ()}))

(defn- push-state! [state description]
  (swap! state-history update :stack conj @state)
  (swap! state assoc :undo-description description)
  )

(defn- pop-state! []
  (let [st @state-history]
    (when (-> st :stack count (> 0))
      (swap! state-history update :stack rest))
    (first (:stack st))))

(defmulti read om/dispatch)

(defmethod read :undo-description
  [{:keys [state]} k _]
  {:value (get @state k)})
(defmethod read :default
  [{:keys [query state] :as env} k params]
  (let [st @state]
    {:value (om/db->tree query (get st k) st)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'todos/add
  [{:keys [state] :as env} k todo]
  (let [st @state
        todo (if (:todo/id todo)
               todo
               (merge todo {:todo/id (gen-uuid)}))
        id (:todo/id todo)]
    {:action (fn []
               (push-state! state (str "add \"" (:todo/description todo) "\""))
               (swap! state #(-> %
                                 (update :todos conj [:todo/by-id id])
                                 (assoc-in [:todo/by-id id] todo)
                                 ))
               )}))

(defmethod mutate 'todo/update
  [{:keys [query state ref] :as env} k {:keys [todo/id] :as props}]
  (let [st @state]
    {:action (fn []
               (push-state! state "update todo")
               (swap! state update-in ref merge props)
               )}))

(defmethod mutate 'todo/delete
  [{:keys [state ref]} k]
  (letfn [(remover [x]
                   (into [] (remove (partial = ref)) x))]
    (let [st @state
          todo (get-in st ref)]
      {:action (fn []
                 (push-state! state (str "delete \"" (:todo/description todo) "\""))
                 (swap! state update :todos remover))})))

(defmethod mutate 'todo/toggle-check
  [{:keys [query state ref]} k _]
  (let [st @state
        path (conj ref :todo/completed?)
        completed? (get-in st path)]
    {:action (fn []
               (push-state! state (str "mark " (if completed? "todo" "done")))
               (swap! state update-in path not))
     }))

(defmethod mutate 'todos/archive
  [{:keys [query state ref]} k params]
  (let [st @state
        completed-ref? #(get-in st (conj % :todo/completed?))
        archiver (fn [st]
                   (let [completed (vec (filter completed-ref? (get st :todos)))]
                     (-> st
                         (update :todos (partial into [] (remove completed-ref?)))
                         (update :todos/archived (comp vec concat) completed))))]
    {:action (fn []
               (push-state! state "archive completed")
               (swap! state archiver))
     }))

(defmethod mutate 'todos/undo
  [{:keys [query state]} k params]

  {:action #(reset! state (pop-state!))})

;; -----------------------------------------------------------------------------

(def parser (om/parser {:read read
                        :mutate mutate
                        }))
