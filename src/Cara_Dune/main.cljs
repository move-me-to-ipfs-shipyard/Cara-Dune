(ns Cara-Dune.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.string :as Wichita.string]
   [clojure.pprint :as Wichita.pprint]
   [cljs.core.async.impl.protocols :refer [closed?]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [goog.string.format :as format]
   [goog.string :refer [format]]
   [goog.object]
   [cljs.reader :refer [read-string]]

   [Cara-Dune.seed :refer [root op]]
   [Cara-Dune.host]
   [Cara-Dune.raisins]
   [Cara-Dune.peanuts]
   [Cara-Dune.kiwis]
   [Cara-Dune.salt]
   [Cara-Dune.microwaved-potatoes]
   [Cara-Dune.corn]
   [Cara-Dune.beans]))

(defonce os (js/require "os"))
(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))
(defonce express (js/require "express"))
(set! (.-defaultMaxListeners (.-EventEmitter (js/require "events"))) 100)
(set! (.-AbortController js/global) (.-AbortController (js/require "node-abort-controller")))
(defonce OrbitDB (js/require "orbit-db"))
(defonce IPFSHttpClient (js/require "ipfs-http-client"))
(defonce IPFS (js/require "ipfs"))

(defmethod op :ping
  [value]
  (go
    (Wichita.pprint/pprint value)
    (put! (:ui-send| root) {:op :pong
                            :from :program
                            :meatbuster :Jesus})))

(defmethod op :pong
  [value]
  (go
    (Wichita.pprint/pprint value)))

(defmethod op :game
  [value]
  (go))

(defmethod op :leave
  [value]
  (go))

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

(defn -main []
  (go
    (let []

      (println ":_ Mandalorian isn't a race")
      (println ":Mando it's a Creed")
      (println (format "http://localhost:%s" (:port root)))
      (println "i dont want my next job")
      (println "Kuiil has spoken")


      (.ensureDirSync fs (:program-data-dirpath root))

      (remove-watch (:stateA root) :watch-fn)
      (add-watch (:stateA root) :watch-fn
                 (fn [ref wathc-key old-state new-state]

                   (when (not= old-state new-state))))

      (Cara-Dune.host/process
       {:port (:port root)
        :host| (:host| root)
        :ws-send| (:ui-send| root)
        :ws-recv| (:ops| root)})

      (ops-process {})

      (Cara-Dune.beans/process {})

      #_(let [ipfs #_(<p! (.create IPFS (clj->js
                                         {:repo (.join path (.homedir os) ".Cara-Dune" "ipfs")})))
              (.create IPFSHttpClient "http://127.0.0.1:5001")
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


(comment

  (go
    (loop []
      (when-let [{:keys [message from] :as value} (<! sub|)]
        (condp = (:op message)
          :game-state
          (let [{:keys [game-state]} message]
            (swap! gameA merge game-state))
          :player-state
          (let [{:keys [game-state]} message]
            (swap! gameA update-in [:players from] merge message))
          :games
          (let [{:keys [frequency host-peer-id]} message]
            (swap! gamesA update-in [frequency] merge message)))
        (recur))))

  (go
    (loop []
      (<! (timeout 3000))
      (let [expired (into []
                          (comp
                           (keep (fn [[frequency {:keys [timestamp]}]]
                                   #_(println (- (.getTime (java.util.Date.)) timestamp))
                                   (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                     frequency))))
                          @gamesA)]
        (when-not (empty? expired)
          (apply swap! gamesA dissoc expired)))
      (recur)))

  (go
    (loop []
      (<! (timeout 3000))
      (let [expired (into []
                          (comp
                           (keep (fn [[frequency {:keys [timestamp peer-id]}]]
                                   #_(println (- (.getTime (java.util.Date.)) timestamp))
                                   (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                     frequency))))
                          (:players @gameA))]
        (when-not (empty? expired)
          (apply swap! gameA update :players dissoc expired)))
      (recur)))

  (go
    (loop []
      (when-let [value (<! ops|)]
        (condp = (:op value)
          :game
          (let [{:keys [frequency role]} value
                id| (chan 1)
                port (or (System/getenv "Jar_Jar_IPFS_PORT") "5001")
                ipfs-api-url (format "http://127.0.0.1:%s" port)
                games-topic (Cara-Dune.corn/encode-base64url-u "raisins")
                game-topic (Cara-Dune.corn/encode-base64url-u frequency)
                _ (Cara-Dune.corn/subscribe-process
                   {:sub| sub|
                    :cancel| cancel-sub|
                    :frequency frequency
                    :ipfs-api-url ipfs-api-url
                    :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
                    :id| id|})
                host? (= role :host)
                {:keys [peer-id]} (<! id|)]
            #_(println :game value)
            (go
              (loop []
                (alt!
                  cancel-pub|
                  ([_] (do nil))

                  (timeout 2000)
                  ([_]
                   (when host?
                     (Cara-Dune.corn/pubsub-pub
                      ipfs-api-url games-topic (str {:op :games
                                                     :timestamp (.getTime (java.util.Date.))
                                                     :frequency frequency
                                                     :host-peer-id peer-id}))
                     (Cara-Dune.corn/pubsub-pub
                      ipfs-api-url game-topic (str {:op :game-state
                                                    :timestamp (.getTime (java.util.Date.))
                                                    :game-state {:host-peer-id peer-id}})))

                   (Cara-Dune.corn/pubsub-pub
                    ipfs-api-url game-topic (str {:op :player-state
                                                  :timestamp (.getTime (java.util.Date.))
                                                  :peer-id peer-id}))
                   (recur))))))

          :leave
          (let [{:keys [frequency]} value]
            (>! cancel-sub| true)
            (>! cancel-pub| true)
            (reset! gameA {}))

          :discover
          (let [discover-jframe (JFrame. "discover")]
            (Cara-Dune.kiwis/discover-process
             {:jframe discover-jframe
              :root-jframe jframe
              :ops| ops|
              :gamesA gamesA
              :gameA gameA
              :stateA stateA})
            (reset! gameA @gameA))

          :settings
          (let [settings-jframe (JFrame. "settings")]
            (Cara-Dune.kiwis/settings-process
             {:jframe settings-jframe
              :root-jframe jframe
              :ops| ops|
              :settingsA settingsA})
            (reset! settingsA @settingsA))

          :settings-value
          (let []
            (swap! settingsA merge value))

          :host-yes
          (let [{:keys [frequency]} value]
            (println :frequency frequency)))

        (recur))))



  (let [port (or (System/getenv "Jar_Jar_IPFS_PORT") "5001")
        ipfs-api-url (format "http://127.0.0.1:%s" port)
        id| (chan 1)
        raw-stream-connection-pool (Simba.http/connection-pool {:connection-options {:raw-stream? true}})]

    (alter-var-root #'raw-stream-connection-pool (constantly raw-stream-connection-pool))
    (Cara-Dune.corn/subscribe-process
     {:sub| sub|
      :raw-stream-connection-pool raw-stream-connection-pool
      :cancel| (chan (sliding-buffer 1))
      :frequency "raisins"
      :ipfs-api-url ipfs-api-url
      :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
      :id| id|}))

  ;
  )