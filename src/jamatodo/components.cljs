(ns jamatodo.components
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab])
  (:require-macros
    [jamatodo.macros :refer [inspect]]))

(defn input-updater
  [& args]
  (fn [e]
    (let [v (.. e -target -value)
          args (conj args v)]
      (apply om/update-state! args))))

(def e-value #(.. % -target -value))

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
          {:keys [editing? edit-data]} (om/get-state this)]
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
                  (om/transact! this `[(todo/update ~data)
                                       :undo-description])))

              (handle-change
                [e] (om/update-state! this assoc-in [:edit-data :todo/description] (e-value e)))

              (handle-remove
                [] (om/transact! this `[(todo/delete ~{:todo/id id}) :todos]))

              (handle-checkbox
                [e]
                (. e preventDefault)
                (om/transact! this `[(todo/toggle-check) :todos]))]
      (sab/html
        [:div {:class (if completed?
                        "todo-item completed"
                        "todo-item")}
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
         [:undo-description
          {:todos (om/get-query TodoItem)}
          {:todos/archived (om/get-query TodoItem)}])

  Object
  (getInitialState
    [_]
    {:new-task ""})

  (render
    [this]
    (let [{:keys [todos todos/archived undo-description]} (om/props this)
          {:keys [new-task]} (om/get-state this)]
      (letfn [(handle-new-task [e]
                               (om/transact! this `[(todos/add ~new-task)])
                               (om/update-state! this assoc :new-task {}))]
       (sab/html
        [:div.app-container
         [:div.top
          [:input.new-task {:type "text"
                            :value (:todo/description new-task)
                            :onChange (fn [e] (om/update-state! this assoc-in [:new-task :todo/description] (e-value e)))
                            :onKeyDown (fn [e] (when (= 13 (.. e -keyCode))
                                                 (handle-new-task e)))}]
          [:button {:onClick handle-new-task
                    :disabled (-> new-task :todo/description empty?)}
           "New Task"]]
         [:div.todo-list
          [:h2 "to do"]
          (map make-TodoItem todos)]
         [:div
          [:button {:onClick #(om/transact! this `[(todos/archive)])
                    :disabled (-> (filter :todo/completed? todos) count zero?)}
           "archive completed tasks"]
          (when undo-description
            [:button {:onClick #(om/transact! this `[(todos/undo)])
                      :disabled (not undo-description)}
             (str "undo " (inspect undo-description))])]
         [:div.archive
          (when-not (empty? archived)
            [:h2 "archived"])
          (for [todo archived]
            [:li (:todo/description todo)])]
         ] ))
      )))
