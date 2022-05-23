(ns Cara-Dune.salt
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

   ["@codemirror/closebrackets" :refer [closeBrackets]]
   ["@codemirror/fold" :as fold]
   ["@codemirror/gutter" :refer [lineNumbers]]
   ["@codemirror/highlight" :as highlight]
   ["@codemirror/history" :refer [history historyKeymap]]
   ["@codemirror/state" :refer [EditorState]]
   ["@codemirror/view" :as Metro-Man.view :refer [EditorView]]
   ["lezer" :as Titan.core]
   ["lezer-generator" :as Titan.generator]
   ["lezer-tree" :as Titan.tree]
   [nextjournal.clojure-mode :as Ritchi.core]
   [nextjournal.clojure-mode.extensions.close-brackets :as close-brackets]
   [nextjournal.clojure-mode.extensions.formatting :as Ritchi.extensions.formatting]
   [nextjournal.clojure-mode.extensions.selection-history :as sel-history]
   [nextjournal.clojure-mode.keymap :as keymap]
   [nextjournal.clojure-mode.live-grammar :as live-grammar]
   [nextjournal.clojure-mode.node :as n]
   [nextjournal.clojure-mode.selections :as sel]
   [nextjournal.clojure-mode.test-utils :as test-utils]
   [reagent.core :as r]
   [reagent.dom :as rdom]

   [applied-science.js-interop :as Minion.js-interop]
   [sci.core :as Batty.core]
   [nextjournal.clojure-mode.extensions.eval-region :as eval-region]
   [nextjournal.clojure-mode.util :as u]))

(defonce context (Batty.core/init {:namespaces {'clojure.core.async {'go go
                                                                     'timeout timeout
                                                                     '<! <!}
                                                'clojure.core {'println println}}}))

(defn eval-string [source]
  (try (Batty.core/eval-string* context source)
       (catch js/Error e
         (str e))))

(Minion.js-interop/defn eval-at-cursor [on-result ^:js {:keys [state]}]
  (some->> (eval-region/cursor-node-string state)
           (eval-string)
           (on-result))
  true)

(Minion.js-interop/defn eval-top-level [on-result ^:js {:keys [state]}]
  (some->> (eval-region/top-level-string state)
           (eval-string)
           (on-result))
  true)

(Minion.js-interop/defn eval-cell [on-result ^:js {:keys [state]}]
  (-> (str "(do " (.-doc state) " )")
      (eval-string)
      (on-result))
  true)

(defn keymap* [modifier]
  {:eval-cell
   [{:key "Mod-Enter"
     :doc "Evaluate cell"}]
   :eval-at-cursor
   [{:key (str modifier "-Enter")
     :doc "Evaluates form at cursor"}]
   :eval-top-level
   [{:key (str modifier "-Shift-Enter")
     :doc "Evaluates top-level form at cursor"}]})

(defn extension [{:keys [modifier
                         on-result]}]
  (.of Metro-Man.view/keymap
       (Minion.js-interop/lit
        [{:key "Mod-Enter"
          :run (partial eval-cell on-result)}
         {:key (str modifier "-Enter")
          :shift (partial eval-top-level on-result)
          :run (partial eval-at-cursor on-result)}])))

#_(js/console.log (.-theme Metro-Man.view/EditorView))

#_(def theme
    (Metro-Man.view/EditorView.theme
     (Minion.js-interop/lit {".cm-content" {:white-space "pre-wrap"
                                            :padding "10px 0"}
                             "&.cm-focused" {:outline "none"}
                             ".cm-line" {:padding "0 9px"
                                         :line-height "1.6"
                                         :font-size "16px"
                                         :font-family "var(--code-font)"}
                             ".cm-matchingBracket" {:border-bottom "1px solid var(--teal-color)"
                                                    :color "inherit"}
                             ".cm-gutters" {:background "transparent"
                                            :border "none"}
                             ".cm-gutterElement" {:margin-left "5px"}
                  ;; only show cursor when focused
                             ".cm-cursor" {:visibility "hidden"}
                             "&.cm-focused .cm-cursor" {:visibility "visible"}})))

(defonce extensions #js [#_theme
                        (history)
                        highlight/defaultHighlightStyle
                        (Metro-Man.view/drawSelection)
                                        ;(lineNumbers)
                        (fold/foldGutter)
                        (.. EditorState -allowMultipleSelections (of true))
                        (if false
                          ;; use live-reloading grammar
                          #js[(Ritchi.core/syntax live-grammar/parser)
                              (.slice Ritchi.core/default-extensions 1)]
                          Ritchi.core/default-extensions)
                        (.of Metro-Man.view/keymap Ritchi.core/complete-keymap)
                        (.of Metro-Man.view/keymap historyKeymap)])


(defn editor [source {:keys [eval?]}]
  (r/with-let [!view (r/atom nil)
               last-result (when eval? (r/atom (eval-string source)))
               mount! (fn [el]
                        (js/console.log Metro-Man.view/EditorView)
                        (when el
                          (reset! !view (Metro-Man.view/EditorView.
                                         (Minion.js-interop/obj :state
                                                                (test-utils/make-state
                                                                 (cond-> #js [extensions]
                                                                   eval? (.concat #js [(extension {:modifier  "Alt"
                                                                                                   :on-result (partial reset! last-result)})]))
                                                                 source)
                                                                :parent el)))))]
    [:div
     [:div {:class "rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
            :ref mount!
            :style {:max-height 410}}]
     (when eval?
       [:div.mt-3.mv-4.pl-6 {:style {:white-space "pre-wrap" :font-family "var(--code-font)"}}
        (prn-str @last-result)])]
    (finally
      (Minion.js-interop/call @!view :destroy))))


(defn rc-editor []
  (into [:<>]
        (for [source [(str
                       '(+ 3 4)
                       #_'(loop [n 3]
                            (when (> n 0)
                              (println :n-is n)
                              (recur (dec n))))
                       #_'(go
                            (loop [n 3]
                              (when (> n 0)
                                (<! (timeout 1000))
                                (println 3)
                                (recur (dec n))))))]]
          [editor source {:eval? true}])))

(defn linux? []
  (some? (re-find #"(Linux)|(X11)" js/navigator.userAgent)))

(defn mac? []
  (and (not (linux?))
       (some? (re-find #"(Mac)|(iPhone)|(iPad)|(iPod)" js/navigator.platform))))

(defn key-mapping []
  (cond-> {"ArrowUp" "↑"
           "ArrowDown" "↓"
           "ArrowRight" "→"
           "ArrowLeft" "←"
           "Mod" "Ctrl"}
    (mac?)
    (merge {"Alt" "⌥"
            "Shift" "⇧"
            "Enter" "⏎"
            "Ctrl" "⌃"
            "Mod" "⌘"})))

(defn render-key [key]
  (let [keys (into [] (map #(get ((memoize key-mapping)) % %) (Wichita.string/split key #"-")))]
    (into [:span]
          (map-indexed (fn [i k]
                         [:<>
                          (when-not (zero? i) [:span " + "])
                          [:kbd.kbd k]]) keys))))

(defn key-bindings-table []
  [:table.w-full.text-sm
   [:thead
    [:tr.border-t
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 "Command"]
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 "Keybinding"]
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 "Alternate Binding"]
     [:th.px-3.py-1.align-top.text-left.text-xs.uppercase.font-normal.black-50 {:style {:min-width 290}} "Description"]]]
   (into [:tbody]
         (->> keymap/paredit-keymap*
              (merge (keymap* "Alt"))
              (sort-by first)
              (map (fn [[command [{:keys [key shift doc]} & [{alternate-key :key}]]]]
                     [:<>
                      [:tr.border-t.hover:bg-gray-100
                       [:td.px-3.py-1.align-top.monospace.whitespace-nowrap [:b (name command)]]
                       [:td.px-3.py-1.align-top.text-right.text-sm.whitespace-nowrap (render-key key)]
                       [:td.px-3.py-1.align-top.text-right.text-sm.whitespace-nowrap (some-> alternate-key render-key)]
                       [:td.px-3.py-1.align-top doc]]
                      (when shift
                        [:tr.border-t.hover:bg-gray-100
                         [:td.px-3.py-1.align-top [:b (name shift)]]
                         [:td.px-3.py-1.align-top.text-sm.whitespace-nowrap.text-right
                          (render-key (str "Shift-" key))]
                         [:td.px-3.py-1.align-top.text-sm]
                         [:td.px-3.py-1.align-top]])]))))])

#_(defn ^:dev/after-load render []
    (rdom/render [rc-editor] (js/document.getElementById "editor"))

    (.. (js/document.querySelectorAll "[clojure-mode]")
        (forEach #(when-not (.-firstElementChild %)
                    (rdom/render [editor (Wichita.string/trim (.-innerHTML %))] %))))

    (let [mapping (key-mapping)]
      (.. (js/document.querySelectorAll ".mod,.alt,.ctrl")
          (forEach #(when-let [k (get mapping (.-innerHTML %))]
                      (set! (.-innerHTML %) k)))))

    (rdom/render [key-bindings-table] (js/document.getElementById "docs"))

    (when (linux?)
      (js/twemoji.parse (.-body js/document))))