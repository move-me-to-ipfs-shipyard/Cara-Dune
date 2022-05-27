(ns Cara-Dune.host
  (:require
   [clojure.core.async :as a
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io]
   [clojure.string]

   [reitit.ring]
   [reitit.http]
   [reitit.coercion.spec]
   [reitit.http.coercion]
   [reitit.dev.pretty]
   [sieppari.async.core-async]
   [reitit.interceptor.sieppari]
   [reitit.http.interceptors.parameters]
   [reitit.http.interceptors.muuntaja]
   [reitit.http.interceptors.exception]
   [reitit.http.interceptors.multipart]
   #_[reitit.http.interceptors.dev]
   #_[reitit.http.spec]
   #_[spec-tools.spell]
   [aleph.http]
   [muuntaja.core]
   [sieppari.async.manifold]
   [manifold.deferred]
   [manifold.stream]
   [ring.util.response])
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn process
  [{:keys [port host| ws-send| ws-recv|]
    :as opts}]
  (let [router (reitit.http/ring-handler
                (reitit.http/router
                 [#_["/*" (reitit.ring/create-file-handler
                           {:root "out/ui"
                            :index-files ["index.html"]})]

                  #_["/ui/*" (reitit.ring/create-file-handler
                              {:root "out/ui"
                               :index-files ["index.html"]})]

                  #_["/ui/*" (reitit.ring/create-resource-handler)]

                  ["/api"

                   ["/upload"
                    {:post {:parameters {:multipart {:file reitit.http.interceptors.multipart/temp-file-part}}
                            :handler (fn [{{{:keys [file]} :multipart} :parameters}]
                                       {:status 200
                                        :body {:name (:filename file)
                                               :size (:size file)}})}}]

                   ["/download"
                    {:get {:handler (fn [_]
                                      {:status 200
                                       :headers {"Content-Type" "image/png"}
                                       :body (clojure.java.io/input-stream
                                              (clojure.java.io/resource "reitit.png"))})}}]


                   ["/async"
                    {:get {:handler (fn [{{{:keys [seed results]} :query} :parameters}]
                                      (manifold.deferred/chain
                                       (aleph.http/get
                                        "https://randomuser.me/api/"
                                        {:query-params {:seed seed, :results results}})
                                       :body
                                       (partial muuntaja.core/decode "application/json")
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
                  :exception reitit.dev.pretty/exception
                  :data {:coercion reitit.coercion.spec/coercion
                         :muuntaja muuntaja.core/instance
                         :interceptors [(reitit.http.interceptors.parameters/parameters-interceptor)
                                        (reitit.http.interceptors.muuntaja/format-negotiate-interceptor)
                                        (reitit.http.interceptors.muuntaja/format-response-interceptor)
                                        (reitit.http.interceptors.exception/exception-interceptor)
                                        (reitit.http.interceptors.muuntaja/format-request-interceptor)
                                        (reitit.http.coercion/coerce-response-interceptor)
                                        (reitit.http.coercion/coerce-request-interceptor)
                                        (reitit.http.interceptors.multipart/multipart-interceptor)]}})
                (reitit.ring/routes
                 (reitit.ring/create-resource-handler {:path "/"
                                                       :root ""
                                                       :index-files []})
                 (let [socketV (volatile! nil)]
                   (go
                     (loop []
                       (when-let [message (<! ws-send|)]
                         (when-let [^manifold.stream.core.IEventSink socket @socketV]
                           (manifold.stream/put! socket (str message)))
                         (recur))))
                   (fn respond-webscket
                     ([request]
                      (when (clojure.string/starts-with? (:uri request) "/ui")
                        (-> (aleph.http/websocket-connection request)
                            (manifold.deferred/chain
                             (fn [socket]
                               (vreset! socketV socket)
                               socket)
                             (fn [socket]
                               #_(manifold.stream/connect socket socket)
                               (put! ws-send| {:op :ping
                                               :if :you-re-seeing-things-running-through-your-head
                                               :who :ya-gonna-call?})
                               (manifold.deferred/loop
                                []
                                 (->
                                  (manifold.stream/take! socket :none)
                                  (manifold.deferred/chain
                                   (fn [message-string]
                                     (when-not (identical? message-string :none)
                                       (put! ws-recv| (read-string message-string))
                                       #_(let [message (cheshire.core/parse-string message-string true)]
                                           #_(println :message message)
                                           (put! ws| message))
                                       (manifold.deferred/recur))))
                                  (manifold.deferred/catch Exception (fn [ex] (println ex)))))))
                            (manifold.deferred/catch
                             (fn [ex]
                               (println ex))))))
                     ([request respond _]
                      (when (clojure.string/starts-with? (:uri request) "/ui")
                        (respond (respond-webscket request))))))
                 (fn respond-with-index-html
                   ([request]
                    (when-not (clojure.string/starts-with? (:uri request) "/api")
                      (ring.util.response/resource-response "index.html")))
                   ([request respond _]
                    (when-let [response (respond-with-index-html request)]
                      (respond response))))
                 (reitit.ring/create-default-handler))
                {:executor reitit.interceptor.sieppari/executor})
        host (aleph.http/start-server (aleph.http/wrap-ring-async-handler router)
                                      {:port port
                                       :host "0.0.0.0"})]
    (go
      (<! host|)
      (.close ^java.io.Closeable host))

    (println (format "http://localhost:%s" port))))