(ns tudu.cards
  (:require
    [om.next :as om :refer-macros [defui ui]]
    [om.dom :as dom :include-macros true]
    [sablono.core :as sab :include-macros true]
    [tudu.components :as components]
    [tudu.parsing :as parsing]
    [tudu.app :as app])
  (:require-macros
    [devcards.core :as dc :refer [defcard deftest defcard-om-next]]))

(enable-console-print!)

(defcard-om-next app-root
  components/App
  app/reconciler)

#_(defcard-om-next app-root
  (om/factory components/App)
  (om/reconciler {:state {}
                  :parser (om/parser {:read #()})}))


#_(defcard app-mock-root
  (om/mock-root
    (om/reconciler {:state {}
                    :parser (om/parser {:read #()})})
    #_app/reconciler
    components/App))

