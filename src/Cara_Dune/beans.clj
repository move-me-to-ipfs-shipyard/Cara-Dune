(ns Cara-Dune.beans
  (:require
   [clojure.core.async
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io]
   [clojure.string]
   [clojure.repl]

   [Cara-Dune.seed]))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))