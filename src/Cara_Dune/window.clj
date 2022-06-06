(ns Cara-Dune.window
  (:require
   [clojure.core.async :as a
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io]
   [clojure.string]
   [clojure.repl]

   [cljfx.api]
   [cljfx.prop]
   [cljfx.mutator]
   [cljfx.lifecycle])
  (:import
   (javafx.scene.web WebView)
   (javafx.scene.image Image)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn process
  [{:keys []
    :as opts}]
  (let [ext-with-html (cljfx.api/make-ext-with-props
                       {:html (cljfx.prop/make
                               (cljfx.mutator/setter (fn [web-view value]
                                                       #_(println :value value)
                                                       #_(.loadContent (.getEngine ^WebView web-view) value)))
                               cljfx.lifecycle/scalar)
                        :ui (cljfx.prop/make
                             (cljfx.mutator/setter (fn [^WebView web-view ^java.net.URL value]
                                                     (.load (.getEngine ^WebView web-view) value)))
                             cljfx.lifecycle/scalar)})]
    (go
      (cljfx.api/on-fx-thread
       (cljfx.api/create-component
        {:fx/type :stage
         :showing true
         :icons [{:is (clojure.java.io/input-stream (clojure.java.io/resource "icon.png"))}]
         :title "one X-Wing? great - we're saved"
         :width 1600
         :height 900
         :scene {:fx/type :scene
                 :root {:fx/type ext-with-html
                        :props {:html "<h1>dank farrik!</h1>"
                                :ui (.toExternalForm (clojure.java.io/resource "index.html"))}
                        :desc {:fx/type :web-view}}}})))))