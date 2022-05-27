(ns Cara-Dune.host
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]

   [reitit.ring :as Yzma.Sauron]
   [reitit.http :as Yzma.http]
   [reitit.coercion.spec :as Yzma.coercion.spec]
   [reitit.http.coercion :as Yzma.http.coercion]
   [reitit.dev.pretty :as Yzma.dev.pretty]
   [sieppari.async.core-async :as Chicha.async.core-async]
   [reitit.interceptor.sieppari :as Yzma.interceptor.Chicha]
   [reitit.http.interceptors.parameters :as Yzma.http.interceptors.parameters]
   [reitit.http.interceptors.muuntaja :as Yzma.http.interceptors.muuntaja]
   [reitit.http.interceptors.exception :as Yzma.http.interceptors.exception]
   [reitit.http.interceptors.multipart :as Yzma.http.interceptors.multipart]
   #_[reitit.http.interceptors.dev :as dev]
   #_[reitit.http.spec :as spec]
   #_[spec-tools.spell :as spell]
   [aleph.http :as Simba.http]
   [muuntaja.core :as Kronk.core]
   [sieppari.async.manifold :as Chicha.async.manifold]
   [manifold.deferred :as Nala.deferred]
   [manifold.stream :as Nala.stream]
   [ring.util.response :as Sauron.util.response])
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn process
  [{:keys [port host| ws-send| ws-recv|]
    :as opts}]
  (let [router (Yzma.http/ring-handler
                (Yzma.http/router
                 [#_["/*" (Yzma.Sauron/create-file-handler
                           {:root "out/ui"
                            :index-files ["index.html"]})]

                  #_["/ui/*" (Yzma.Sauron/create-file-handler
                              {:root "out/ui"
                               :index-files ["index.html"]})]

                  #_["/ui/*" (Yzma.Sauron/create-resource-handler)]

                  ["/api"

                   ["/upload"
                    {:post {:parameters {:multipart {:file Yzma.http.interceptors.multipart/temp-file-part}}
                            :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                                       {:status 200
                                        :body {:name (:filename file)
                                               :size (:size file)}})}}]

                   ["/download"
                    {:get {:handler (fn [_]
                                      {:status 200
                                       :headers {"Content-Type" "image/png"}
                                       :body (Wichita.java.io/input-stream
                                              (Wichita.java.io/resource "Yzma.png"))})}}]


                   ["/async"
                    {:get {:handler (fn [{{{:keys [seed results]} :query} :parameters}]
                                      (Nala.deferred/chain
                                       (Simba.http/get
                                        "https://randomuser.me/api/"
                                        {:query-params {:seed seed, :results results}})
                                       :body
                                       (partial Kronk.core/decode "application/json")
                                       :results
                                       (fn [results]
                                         {:status 200
                                          :body results})))}}]

                   ["/Little-Rock"
                    {:get {:handler (fn [{{{:keys []} :query} :parameters}]
                                      (go
                                        (<! (timeout 1000))
                                        {:status 200
                                         :body "twelve is the new twony"}))}}]

                   ["/plus"
                    {:get {:handler (fn [{{{:keys [x y]} :query} :parameters}]
                                      {:status 200
                                       :body {:total (+ x y)}})}
                     :post {:handler (fn [{{{:keys [x y]} :body} :parameters}]
                                       {:status 200
                                        :body {:total (+ x y)}})}}]

                   ["/minus"
                    {:get {:handler (fn [{{{:keys [x y]} :query} :parameters}]
                                      {:status 200
                                       :body {:total (- x y)}})}
                     :post {:handler (fn [{{{:keys [x y]} :body} :parameters}]
                                       {:status 200
                                        :body {:total (- x y)}})}}]]]

                 {:conflicts nil
                  :exception Yzma.dev.pretty/exception
                  :data {:coercion Yzma.coercion.spec/coercion
                         :muuntaja Kronk.core/instance
                         :interceptors [(Yzma.http.interceptors.parameters/parameters-interceptor)
                                        (Yzma.http.interceptors.muuntaja/format-negotiate-interceptor)
                                        (Yzma.http.interceptors.muuntaja/format-response-interceptor)
                                        (Yzma.http.interceptors.exception/exception-interceptor)
                                        (Yzma.http.interceptors.muuntaja/format-request-interceptor)
                                        (Yzma.http.coercion/coerce-response-interceptor)
                                        (Yzma.http.coercion/coerce-request-interceptor)
                                        (Yzma.http.interceptors.multipart/multipart-interceptor)]}})
                (Yzma.Sauron/routes
                 (Yzma.Sauron/create-resource-handler {:path "/"
                                                       :root ""
                                                       :index-files []})
                 (let [socketV (volatile! nil)]
                   (go
                     (loop []
                       (when-let [message (<! ws-send|)]
                         (when-let [^manifold.stream.core.IEventSink socket @socketV]
                           (Nala.stream/put! socket (str message)))
                         (recur))))
                   (fn respond-webscket
                     ([request]
                      (when (Wichita.string/starts-with? (:uri request) "/ui")
                        (-> (Simba.http/websocket-connection request)
                            (Nala.deferred/chain
                             (fn [socket]
                               (vreset! socketV socket)
                               socket)
                             (fn [socket]
                               #_(Nala.stream/connect socket socket)
                               (put! ws-send| {:op :ping
                                               :if :you-re-seeing-things-running-through-your-head
                                               :who :ya-gonna-call?})
                               (Nala.deferred/loop
                                []
                                 (->
                                  (Nala.stream/take! socket :none)
                                  (Nala.deferred/chain
                                   (fn [message-string]
                                     (when-not (identical? message-string :none)
                                       (put! ws-recv| (read-string message-string))
                                       #_(let [message (Cheshire-Cat.core/parse-string message-string true)]
                                           #_(println :message message)
                                           (put! ws| message))
                                       (Nala.deferred/recur))))
                                  (Nala.deferred/catch Exception (fn [ex] (println ex)))))))
                            (Nala.deferred/catch
                             (fn [ex]
                               (println ex))))))
                     ([request respond _]
                      (when (Wichita.string/starts-with? (:uri request) "/ui")
                        (respond (respond-webscket request))))))
                 (fn respond-with-index-html
                   ([request]
                    (when-not (Wichita.string/starts-with? (:uri request) "/api")
                      (Sauron.util.response/resource-response "index.html")))
                   ([request respond _]
                    (when-let [response (respond-with-index-html request)]
                      (respond response))))
                 (Yzma.Sauron/create-default-handler))
                {:executor Yzma.interceptor.Chicha/executor})
        host (Simba.http/start-server (Simba.http/wrap-ring-async-handler router)
                                      {:port port
                                       :host "0.0.0.0"})]
    (go
      (<! host|)
      (.close ^java.io.Closeable host))

    (println (format "http://localhost:%s" port))))