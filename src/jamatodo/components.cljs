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
         `[:todo/id :todo/description :todo/completed?])

  Object
  (getInitialState
    [this]
    {:editing? false
     :edit-data nil})

  (render
    [this]
    (let [{:keys [todo/id todo/description todo/completed?] :as props} (om/props this)
          {:keys [editing? edit-data]} (om/get-state this)
          className (if completed? "todo-item completed" "todo-item")]
      (letfn [(begin-edit
                [] (om/update-state! this assoc
                                      :editing? true
                                      :edit-data props))
              (handle-cancel-edit
                [] (om/update-state! this assoc
                                      :editing? false
                                      :edit-data nil))
              (handle-save-edit
                []
                (let [{data :edit-data} (om/get-state this)]
                  (handle-cancel-edit)
                  (om/transact! this `[(todo/update ~data)])))

              (handle-change
                [e] (om/update-state! this assoc-in [:edit-data :todo/description] (.. e -target -value)))

              (handle-remove
                [] (om/transact! this `[(todo/delete ~{:todo/id id}) :todos]))

              (handle-checkbox
                [e]
                (. e preventDefault)
                (om/transact! this `[(todo/toggle-check) :todos]))]
      (sab/html
        [:div {:class className}
         [:input {:type "checkbox"
                  :checked completed?
                  :onClick handle-checkbox}]
         (if editing?
           [:input {:value (:todo/description edit-data)
                    :onChange handle-change}]
           [:span.description description])
         (if editing?
           ;; then
           [:div.controls
            [:button {:onClick handle-save-edit}
             "save"]
            [:button {:onClick handle-cancel-edit}
             "cancel"]]
           ;; else
           [:div.controls
            [:button {:onClick begin-edit}
             "edit"]
            [:button {:onClick handle-remove}
             "×"]])])))))

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
