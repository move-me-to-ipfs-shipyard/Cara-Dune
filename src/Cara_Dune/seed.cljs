(ns Cara-Dune.seed
  (:require
   [clojure.core.async :as a
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]))

(defonce os (js/require "os"))
(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))

(defmulti op :op)

(defonce root (let [program-data-dirpath (or
                                          (some->
                                           (.. js/global.process -env -CARA_DUNE_PATH)
                                           (clojure.string/replace-first  #"~" (.homedir os)))
                                          (.join path (.homedir os) ".Cara-Dune"))]
                {:program-data-dirpath program-data-dirpath
                 :state-file-filepath (.join path program-data-dirpath "Cara-Dune.edn")
                 :orbitdb-data-dirpath (.join path program-data-dirpath "orbitdb")
                 :port (or (try (.. js/global.process -env -PORT)
                                (catch js/Error ex nil))
                           3344)
                 :stateA (atom nil)
                 :windowA (atom nil)
                 :host| (chan 1)
                 :ops| (chan 10)
                 :ui-send| (chan 10)}))