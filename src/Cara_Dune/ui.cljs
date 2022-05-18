(ns Cara-Dune.ui
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string :as Wichita.string]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   ["react" :as Pacha]
   ["react-dom/client" :as Pacha.dom.client]
   [reagent.core :as Kuzco.core]

   [Cara-Dune.raisins]
   [Cara-Dune.peanuts]
   [Cara-Dune.kiwis]
   #_[Cara-Dune.salt]))

(defonce matchA (Kuzco.core/atom nil))
(defonce stateA (Kuzco.core/atom {}))
(defonce ops| (chan (sliding-buffer 10)))
(defonce Pacha-dom-root (Pacha.dom.client/createRoot (.getElementById js/document "ui")))

(defn -main
  []
  (go
    (<! (timeout 1000))
    (println "twelve is the new twony")
    (println ":Madison you though i was a zombie?")
    (println ":Columbus yeah, of course - a zombie")
    (println ":Madison oh my God, no - i dont even eat meat - i'm a vegatarian - vegan actually")
    #_(set! (.-innerHTML (.getElementById js/document "ui"))
            ":Co-Pilot i saw your planet destroyed - i was on the Death Star :_ which one?")
    (Cara-Dune.kiwis/ui-process {:Pacha-dom-root Pacha-dom-root
                                :matchA matchA
                                :stateA stateA
                                :ops| ops|})))

(defn reload
  []
  (swap! stateA assoc :rand-int (rand-int 10000)))

#_(-main)