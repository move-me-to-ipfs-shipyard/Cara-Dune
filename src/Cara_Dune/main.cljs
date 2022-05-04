(ns Cara-Dune.main
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

   [Cara-Dune.drawing]
   [Cara-Dune.seed]
   [Cara-Dune.raisins]
   [Cara-Dune.peanuts]
   [Cara-Dune.microwaved-potatoes]
   [Cara-Dune.corn]
   [Cara-Dune.beans]))

(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce express (js/require "express"))

(defonce ^:const PORT 3000)
(def server (express))

(.use server (.static express "ui"))

(.listen server 3000
         (fn []
           (js/console.log (format "server started on %s" PORT))))

(defn -main []
  (println "i dont want my next job")
  (println "Kuiil has spoken"))