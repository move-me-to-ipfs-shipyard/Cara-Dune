(ns Cara-Dune.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]
   [clojure.pprint :as Wichita.pprint]
   [clojure.repl :as Wichita.repl]

   [aleph.http :as Simba.http]

   [Cara-Dune.seed]
   [Cara-Dune.microwaved-potatoes]
   [Cara-Dune.corn]
   [Cara-Dune.beans])
  (:import
   (java.io File))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))
(defonce host| (chan 1))

(defn reload
  []
  (require
   '[Cara-Dune.seed]
   '[Cara-Dune.microwaved-potatoes]
   '[Cara-Dune.beans]
   '[Cara-Dune.main]
   :reload))

(defn -main
  [& args]
  (println ":_ Mandalorian isn't a race")
  (println ":Mando it's a Creed")
  (println "i dont want my next job")
  (println "Kuiil has spoken")

  (let [data-dir-path (or
                       (some-> (System/getenv "CARA_DUNE_PATH")
                               (.replaceFirst "^~" (System/getProperty "user.home")))
                       (.getCanonicalPath ^File (Wichita.java.io/file (System/getProperty "user.home") ".Cara-Dune")))
        state-file-path (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path "Cara-Dune.edn"))]
    (Wichita.java.io/make-parents data-dir-path)
    (reset! stateA {})


    (remove-watch stateA :watch-fn)
    (add-watch stateA :watch-fn
               (fn [ref wathc-key old-state new-state]

                 (when (not= old-state new-state))))


    (let [port (or (try (Integer/parseInt (System/getenv "PORT"))
                        (catch Exception e nil))
                   3344)]
      (Cara-Dune.microwaved-potatoes/process
       {:port port
        :host| host|}))

    (let [path-db (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path "Deep-Thought"))]
      (Wichita.java.io/make-parents path-db)
      (Cara-Dune.beans/process {:path path-db}))))