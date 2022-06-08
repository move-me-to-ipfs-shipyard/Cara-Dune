(ns Cara-Dune.main
  (:require
   [clojure.core.async :as a
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.java.io]
   [clojure.string]
   [clojure.pprint]
   [clojure.repl]

   [cheshire.core]

   [Cara-Dune.seed :refer [root op]]
   [Cara-Dune.window]
   [Cara-Dune.raisins]
   [Cara-Dune.peanuts]
   [Cara-Dune.kiwis]
   [Cara-Dune.salt]
   [Cara-Dune.carrots]
   [Cara-Dune.rolled-oats])
  (:import
   (java.io File))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn reload
  []
  (require
   '[Cara-Dune.seed]
   '[Cara-Dune.window]
   '[Cara-Dune.raisins]
   '[Cara-Dune.peanuts]
   '[Cara-Dune.kiwis]
   '[Cara-Dune.salt]
   '[Cara-Dune.carrots]
   '[Cara-Dune.rolled-oats]
   :reload))

(defmethod op :ping
  [value]
  (go
    (clojure.pprint/pprint value)
    (put! (:ui-send| root) {:op :pong
                            :from :program
                            :meatbuster :Jesus})))

(defmethod op :pong
  [value]
  (go
    (clojure.pprint/pprint value)))

(defmethod op :game
  [value]
  (go
    ))

(defmethod op :leave
  [value]
  (go
    ))

(defmethod op :discover
  [value]
  (go))

(defmethod op :settings
  [value]
  (go))

(defn ops-process
  [{:keys []
    :as opts}]
  (go
    (loop []
      (when-let [value (<! (:ops| root))]
        (<! (op value))
        (recur)))))

(defn -main
  [& args]
  (println ":_ Mandalorian isn't a race")
  (println ":Mando it's a Creed")
  (println "i dont want my next job")
  (println "Kuiil has spoken")

  (let []
    (clojure.java.io/make-parents (:program-data-dirpath root))
    (reset! (:stateA root) {})

    (remove-watch (:stateA root) :watch-fn)
    (add-watch (:stateA root) :watch-fn
               (fn [ref wathc-key old-state new-state]

                 (when (not= old-state new-state))))

    (ops-process {})

    (Cara-Dune.window/process {})))