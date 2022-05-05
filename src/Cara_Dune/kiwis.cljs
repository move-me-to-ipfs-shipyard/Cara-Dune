(ns Cara-Dune.kiwis
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

   [Cara-Dune.seed]

   ["react" :as Pacha]
   ["react-dom/client" :as Pacha.dom.client]
   [reagent.core :as Kuzco.core]
   [reagent.dom :as Kuzco.dom]

   [reitit.frontend :as Yzma.frontend]
   [reitit.frontend.easy :as Yzma.frontend.easy]
   [reitit.coercion.spec :as Yzma.coercion.spec]
   [reitit.frontend.controllers :as Yzma.frontend.controllers]
   [spec-tools.data-spec :as Yzma.data-spec]

   ["antd/lib/layout" :default ThemeSongGuyLayout]
   ["antd/lib/menu" :default ThemeSongGuyMenu]
   ["antd/lib/button" :default ThemeSongGuyButton]
   ["antd/lib/row" :default ThemeSongGuyRow]
   ["antd/lib/col" :default ThemeSongGuyCol]
   ["antd/lib/input" :default ThemeSongGuyInput]
   ["antd/lib/table" :default ThemeSongGuyTable]))



(defn rc-main-page
  [match stateA]
  [:> (.-Content ThemeSongGuyLayout)
   {:style {:background-color "white"}}
   [:> ThemeSongGuyRow
    ":Co-Pilot i saw your planet destroyed - i was on the Death Star :_ which one?"]])

(defn rc-settings-page
  [match stateA]
  [:> (.-Content ThemeSongGuyLayout)
   {:style {:background-color "white"}}
   [:> ThemeSongGuyRow
    "settings"
    #_(str "settings" (:rand-int @stateA))]])

(defn rc-current-page
  [matchA stateA]
  (Kuzco.core/with-let
    []
    [:> ThemeSongGuyLayout
     [:> ThemeSongGuyMenu
      {:mode "horizontal"
       :key "program"
       :size "small"
       :defaultSelectedKeys []
       :onClick (fn [event]
                  (js/console.log event))
       :items [{:label "program"
                :key "program"
                :icon nil
                :children [{:type "group"
                            :label (Kuzco.core/as-element
                                    [:div {:style {:cursor "pointer"}
                                           :onClick (fn [_] (Yzma.frontend.easy/push-state :rc-main-page))} "game"])
                            #_(Kuzco.core/as-element [:a {:href (Yzma.frontend.easy/href :rc-main-page)} "game"])
                            :key "game"
                            :icon nil}
                           {:type "group"
                            :label (Kuzco.core/as-element
                                    [:div {:style {:cursor "pointer"}
                                           :onClick (fn [_] (Yzma.frontend.easy/push-state :rc-settings-page))} "settings"])
                            #_(Kuzco.core/as-element [:a {:href (Yzma.frontend.easy/href :rc-main-page)} "settings"])
                            :key "settings"
                            :icon nil}]}]}]
     (when-let [match @matchA]
       [(-> match :data :view) match stateA])]
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
           rerender-rootA]
    :as opts}]
  (let []
    (Yzma.frontend.easy/start!
     (Yzma.frontend/router
      ["/"
       [""
        {:name :rc-main-page
         :view rc-main-page
         :controllers [{:start (fn [_]
                                 (js/console.log "start rc-main-page"))
                        :stop (fn [_]
                                (js/console.log "stop rc-main-page"))}]}]
       ["setting"
      ;; Shared data for sub-routes
        {:name :rc-settings-page
         :view rc-settings-page
         :controllers [{:start (fn [_]
                                 (js/console.log "start rc-settings-page"))
                        :stop (fn [_]
                                (js/console.log "stop rc-settings-page"))}]}]]
      {:data {:controllers [{:start (fn [_]
                                      (js/console.log "start root"))
                             :stop (fn [_]
                                     (js/console.log "stop root"))}]
              :coercion Yzma.coercion.spec/coercion}})
     (fn [new-match]
       (swap! matchA (fn [old-match]
                       (if new-match
                         (assoc new-match :controllers (Yzma.frontend.controllers/apply-controllers (:controllers old-match) new-match))))))
     {:use-fragment false})
    #_(Yzma.frontend.easy/push-state :rc-main-page)
    (.render Pacha-dom-root (Kuzco.core/as-element [rc-current-page matchA stateA]))))
