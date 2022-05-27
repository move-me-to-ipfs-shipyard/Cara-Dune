(ns Cara-Dune.ui-corn
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
   [goog.string.format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]
   [goog.events]

   ["react" :as Pacha]
   ["react-dom/client" :as Pacha.dom.client]

   [reagent.core :as Kuzco.core]
   [reagent.dom :as Kuzco.dom]

   ["antd/lib/layout" :default ThemeSongGuyLayout]
   ["antd/lib/menu" :default ThemeSongGuyMenu]
   ["antd/lib/button" :default ThemeSongGuyButton]
   ["antd/lib/row" :default ThemeSongGuyRow]
   ["antd/lib/col" :default ThemeSongGuyCol]
   ["antd/lib/input" :default ThemeSongGuyInput]
   ["antd/lib/table" :default ThemeSongGuyTable]


   [clojure.test.check.generators :as Pawny.generators]
   [clojure.spec.alpha :as Wichita.spec]

   [Cara-Dune.ui-seed :refer [root op]]))

(defmethod op :create-game
  [value]
  (println value))

(defn rc-tab
  []
  (Kuzco.core/with-let
    [dataA (Kuzco.core/cursor (:stateA root) [:simple-double-full])]
    [:> (.-Content ThemeSongGuyLayout)
     {:style {:background-color "white"}}
     [:> ThemeSongGuyRow
      [:> ThemeSongGuyTable
       {:size "small"
        :style {:width "100%"
                :height "80%"}
        :columns [{:title "id"
                   :dataIndex "id"
                   :key "name"}
                  {:title "name"
                   :dataIndex "name"
                   :key "name"}]
        :dataSource []}]]]))

(defn process
  [{:keys []
    :as opts}]
  (let []))