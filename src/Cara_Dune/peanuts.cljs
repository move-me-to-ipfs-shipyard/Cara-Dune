(ns Cara-Dune.peanuts
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

   [clojure.test.check.generators :as Pawny.generators]
   [clojure.spec.alpha :as Wichita.spec]
   
   [reagent.core :as Kuzco.core]
   ["konva/lib/shapes/Rect"]
   ["konva" :default Konva]
   ["react-konva" :as ReactKonva :rename {Stage KonvaStage
                                          Layer KonvaLayer
                                          Rect KonvaRect
                                          Path KonvaPath
                                          Circle KonvaCircle
                                          Group KonvaGroup
                                          Wedge KonvaWedge
                                          RegularPolygon KonvaRegularPolygon}]

   ["@react-spring/web" :as ReactSpring :rename {animated ReactSpringAnimated
                                                 Spring ReactSpringSpring}]))


(def ^:const box-size 32)
(def ^:const rows 16)
(def ^:const cols 16)

(def colors
  {:sands "#edd3af" #_"#D2B48Cff"
   :Korvus "lightgrey"
   :signal-tower "brown"
   :recharge "#30ad23"
   :Cara-Dune "blue"})

(defn rc-background-layer
  []
  (Kuzco.core/with-let
    []
    [:> (.-Layer ReactKonva)
     {:id "background-layer"}
     [:> (.-Rect ReactKonva) {:width (* box-size cols)
                              :height (* box-size rows)
                              :id "background-rect"
                              :x 0
                              :y 0
                              :fill "#ffffff"
                              :strokeWidth 0
                              :stroke "white"}]]))

(defn rc-game
  [match stateA ops|]
  (Kuzco.core/with-let
    []
    [:> (.-Stage ReactKonva)
     {:width (* box-size cols)
      :height (* box-size rows)}
     [rc-background-layer]]))

(defn canvas-process
  [{:keys []
    :as opts}]
  (let []
    
    
    ))