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

   [Cara-Dune.seed]
   [Cara-Dune.microwaved-potatoes]
   [Cara-Dune.corn]
   [Cara-Dune.beans]))

(defonce os (js/require "os"))
(defonce fs (js/require "fs"))
(defonce path (js/require "path"))
(defonce express (js/require "express"))
(set! (.-defaultMaxListeners (.-EventEmitter (js/require "events"))) 100)
(set! (.-AbortController js/global) (.-AbortController (js/require "node-abort-controller")))
(defonce OrbitDB (js/require "orbit-db"))
(defonce IPFSHttpClient (js/require "ipfs-http-client"))
(defonce IPFS (js/require "ipfs"))


(defonce ^:const port 3344)
(def server (express))
(def api (express.Router.))

(.get api "/Little-Rock" (fn [request response]
                           (go
                             (<! (timeout 1000))
                             (.send response (str {})))))

(.use server (.static express "ui"))
(.use server "/api" api)

(.get server "*" (fn [request response]
                   (.sendFile response (.join path js/__dirname  "ui" "index.html"))))

(defn -main []
  (go
    (let [complete| (chan 1)]
      (.listen server port (fn [] (put! complete| true)))
      (<! complete|)
      (println ":_ Mandalorian isn't a race")
      (println ":Mando it's a Creed")
      (println (format "http://localhost:%s" port))
      (println "i dont want my next job")
      (println "Kuiil has spoken")
      (let [ipfs (<p! (.create IPFS (clj->js
                                     {:repo (.join path (.homedir os) ".Cara-Dune" "ipfs")})))
            orbitdb (<p!
                     (->
                      (.createInstance
                       OrbitDB ipfs
                       (clj->js
                        {"directory" (.join path (.homedir os) ".Cara-Dune" "orbitdb")}))
                      (.catch (fn [ex]
                                (println ex)))))]
        (println (.. orbitdb -identity -id))))))


(comment

  (let [ipfs (.create IPFSHttpClient "http://127.0.0.1:5001")
        orbitdb (<p!
                 (->
                  (.createInstance
                   OrbitDB ipfs
                   (clj->js
                    {"directory" (.join path (.homedir os) ".Cara-Dune" "orbitdb")}))
                  (.catch (fn [ex]
                            (println ex)))))]
    (println (.. orbitdb -identity -id)))

  ;
  )