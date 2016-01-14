(ns jamatodo.core
  (:require
   [om.next :as om :include-macros true]
   [om.dom :as dom]
   [sablono.core :as sab :include-macros true]
   [jamatodo.app :as app]
   [jamatodo.components :as components]
   )
  (:require-macros
    [jamatodo.macros :refer [inspect]]
    [devcards.core :as dc :refer [defcard deftest]]))


(enable-console-print!)

(om/add-root!
  app/reconciler
  components/App
  (.getElementById js/document "main-app-area"))

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

