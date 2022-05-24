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
   [goog.events]
   [cljs.reader :refer [read-string]]

   ["react" :as Pacha]
   ["react-dom/client" :as Pacha.dom.client]
   [reagent.core :as Kuzco.core]
   [reagent.dom :as Kuzco.dom]

   [reitit.frontend :as Yzma.frontend]
   [reitit.frontend.easy :as Yzma.frontend.easy]
   [reitit.coercion.spec :as Yzma.coercion.spec]
   [reitit.frontend.controllers :as Yzma.frontend.controllers]
   [reitit.frontend.history :as Yzma.frontend.history]
   [spec-tools.data-spec :as Yzma.data-spec]

   ["antd/lib/layout" :default ThemeSongGuyLayout]
   ["antd/lib/menu" :default ThemeSongGuyMenu]
   ["antd/lib/button" :default ThemeSongGuyButton]
   ["antd/lib/row" :default ThemeSongGuyRow]
   ["antd/lib/col" :default ThemeSongGuyCol]
   ["antd/lib/input" :default ThemeSongGuyInput]
   ["antd/lib/table" :default ThemeSongGuyTable]

   [clojure.test.check.generators :as Pawny.generators]
   [clojure.spec.alpha :as Wichita.spec]

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

(defonce matchA (Kuzco.core/atom nil))
(defonce stateA (Kuzco.core/atom {}))
(defonce ops| (chan (sliding-buffer 10)))
(defonce Pacha-dom-root (Pacha.dom.client/createRoot (.getElementById js/document "ui")))

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
  (let []))

(defn rc-main-page
  [match stateA ops|]
  [:> (.-Content ThemeSongGuyLayout)
   {:style {:background-color "white"}}
   [:div {}
    [:div ":Co-Pilot i saw your planet destroyed - i was on the Death Star"]
    [:div ":_ which one?"]]])


(defn rc-game-page
  [match stateA ops|]
  [:> (.-Content ThemeSongGuyLayout)
   {:style {:background-color "white"}}
   [:> ThemeSongGuyRow
    [:> ThemeSongGuyCol
     {:flex 1}
     [rc-game match stateA ops|]]]])

(defn rc-settings-page
  [match stateA ops|]
  [:> (.-Content ThemeSongGuyLayout)
   {:style {:background-color "white"}}
   [:> ThemeSongGuyRow
    "settings"
    #_(str "settings" (:rand-int @stateA))]])

(defn rc-current-page
  [matchA stateA ops|]
  (Kuzco.core/with-let
    [route-keyA (Kuzco.core/cursor matchA [:data :name])]
    (let [route-key @route-keyA]
      [:> ThemeSongGuyLayout
       [:> ThemeSongGuyMenu
        {:mode "horizontal"
         :size "large"
         :selectedKeys [route-key]
         :items [{:type "item"
                  :label
                  (Kuzco.core/as-element [:a {:href (Yzma.frontend.easy/href :rc-main-page)} "program"])
                  :key :rc-main-page
                  :icon nil}
                 {:type "item"
                  :label
                  (Kuzco.core/as-element [:a {:href (Yzma.frontend.easy/href :rc-game-page)} "game"])
                  :key :rc-game-page
                  :icon nil}
                 {:type "item"
                  :label
                  (Kuzco.core/as-element [:a {:href (Yzma.frontend.easy/href :rc-settings-page)} "settings"])
                  :key :rc-settings-page
                  :icon nil}]
         #_[{:label "program"
             :key "program"
             :icon nil
             :children []}]}]
       (when-let [match @matchA]
         [(-> match :data :view) match stateA])])
    #_[:<>

       [:ul
        [:li [:a {:href (Yzma.frontend.easy/href :rc-main-page)} "game"]]
        [:li [:a {:href (Yzma.frontend.easy/href :rc-settings-page)} "settings"]]]
       (when-let [match @matchA]
         [(-> match :data :view) match stateA])]))

(defn ui-process
  [{:keys [Pacha-dom-root
           matchA
           stateA
           ops|]
    :as opts}]
  (let [history (Yzma.frontend.easy/start!
                 (Yzma.frontend/router
                  ["/"
                   [""
                    {:name :rc-main-page
                     :view rc-main-page
                     :controllers [{:start (fn [_]
                                             (js/console.log "start rc-main-page"))
                                    :stop (fn [_]
                                            (js/console.log "stop rc-main-page"))}]}]

                   ["game"
                    {:name :rc-game-page
                     :view rc-game-page
                     :controllers [{:start (fn [_]
                                             (js/console.log "start rc-game-page"))
                                    :stop (fn [_]
                                            (js/console.log "stop rc-game-page"))}]}]
                   ["setting"
                    {:name :rc-settings-page
                     :view rc-settings-page
                     :controllers [{:start (fn [_]
                                             (js/console.log "start rc-settings-page"))
                                    :stop (fn [_]
                                            (js/console.log "stop rc-settings-page"))}]}]]
                  {:data {:controllers [{:start (fn [_]
                                                  (js/console.log "start program"))
                                         :stop (fn [_]
                                                 (js/console.log "stop program"))}]
                          :coercion Yzma.coercion.spec/coercion}})
                 (fn [new-match]
                   (swap! matchA (fn [old-match]
                                   (if new-match
                                     (assoc new-match :controllers (Yzma.frontend.controllers/apply-controllers (:controllers old-match) new-match))))))
                 {:use-fragment false})]
    (goog.events/unlistenByKey (:click-listen-key history))
    (goog.events/listen js/document goog.events.EventType.CLICK
                        (fn [event]
                          (when-let [element (Yzma.frontend.history/closest-by-tag
                                              (Yzma.frontend.history/event-target event) "a")]
                            (let [uri (.parse goog.Uri (.-href element))]
                              (when (Yzma.frontend.history/ignore-anchor-click? (.-router history) event element uri)
                                (.preventDefault event)
                                (let [path (str (.getPath uri)
                                                (when (.hasQuery uri)
                                                  (str "?" (.getQuery uri)))
                                                (when (.hasFragment uri)
                                                  (str "#" (.getFragment uri))))]
                                  (.pushState js/window.history nil "" path)
                                  (Yzma.frontend.history/-on-navigate history path)))))) true)


    #_(Yzma.frontend.easy/push-state :rc-main-page)
    (.render Pacha-dom-root (Kuzco.core/as-element [rc-current-page matchA stateA ops|]))))

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
    (ui-process {:Pacha-dom-root Pacha-dom-root
                 :matchA matchA
                 :stateA stateA
                 :ops| ops|})))

(defn reload
  []
  (swap! stateA assoc :rand-int (rand-int 10000)))

#_(-main)