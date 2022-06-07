(ns Cara-Dune.host
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
   [cljs.reader :refer [read-string]]

   [Cara-Dune.seed :refer [root op]]))

(defonce os (js/require "os"))
(defonce fs (js/require "fs-extra"))
(defonce path (js/require "path"))
(defonce http (js/require "http"))
(defonce express (js/require "express"))
(defonce WebSocketServer (.-WebSocketServer (js/require "ws")))

(defonce host (express))
(defonce api (express.Router.))

(.get api "/a" (fn [request response]
                           (go
                             (<! (timeout 1000))
                             (.send response (str {})))))

(.use host (.static express "ui"))
(.use host "/api" api)

#_(.get host "*" (fn [request response]
                   (.sendFile response (.join path js/__dirname  "ui" "index.html"))))

(defonce host-ws (WebSocketServer. (clj->js {:noServer true
                                             :path "/ws"})))
(defonce http-host (.createServer http host))

(.on http-host "upgrade" (fn [request socket head]
                           (.handleUpgrade host-ws request socket head
                                           (fn [socket]
                                             (.emit host-ws "connection" socket request)))))

(defonce socketV (volatile! nil))

(defn process
  [{:keys [ws-send| ws-recv|]
    :as opts}]
  (.on host-ws "connection"
       (fn [socket]
         (vreset! socketV socket)
         (put! ws-send| {:op :ping
                         :if :you-re-seeing-things-running-through-your-head
                         :who :ya-gonna-call?})
         (.on socket "message" (fn [message-string]
                                 (put! ws-recv| (-> message-string (.toString) (read-string)))))))
  (go
    (loop []
      (when-let [message (<! ws-send|)]
        (when-let [socket @socketV]
          (.send socket (str message)))
        (recur))))
  (let [done| (chan 1)]
    (go
      (.listen http-host (:port root) (fn [] (put! done| true)))
      (<! done|)
      (println (format "http://localhost:%s" (:port root)))

      (go
        (<! (:host| root))
        (.close host)))))


