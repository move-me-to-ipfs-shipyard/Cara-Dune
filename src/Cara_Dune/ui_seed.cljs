(ns Cara-Dune.ui-seed
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string :as Wichita.string]
   [clojure.pprint :as Wichita.pprint]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   ["react-dom/client" :as Pacha.dom.client]
   [reagent.core :as Kuzco.core]))

(defmulti op :op)

(defonce root (let []
                {:matchA (Kuzco.core/atom nil)
                 :stateA (Kuzco.core/atom {})
                 :ops| (chan (sliding-buffer 10))
                 :program-send| (chan 10)
                 :Pacha-dom-root (Pacha.dom.client/createRoot (.getElementById js/document "ui"))}))