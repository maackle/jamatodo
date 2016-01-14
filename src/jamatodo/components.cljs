(ns jamatodo.components
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab])
  (:require-macros
    [jamatodo.macros :refer [inspect]]))

(defui TodoItem

  static om/Ident
  (ident [_ {:keys [todo/id]}]
         [:todo/by-id id])

  static om/IQuery
  (query [_]
         `[:todo/id :todo/description])

  Object
  (render
    [this]
    (let [{:keys [todo/id todo/description]} (om/props this)]
      (sab/html
        [:div
         [:input {:type "checkbox"
                  :onClick (fn [e] (.preventDefault e))}]
         [:span description]
         [:div.controls
          [:button "✎"]
          [:button "×"]]]))))

(def make-TodoItem (om/factory TodoItem))

(defui App
  static om/IQuery
  (query [_]
         [{:todos (om/get-query TodoItem)}])

  Object
  (render
    [this]
    (let [{:keys [todos]} (om/props this)]
      (sab/html
        [:div.todo-list
         (map make-TodoItem todos)] )
      )))
