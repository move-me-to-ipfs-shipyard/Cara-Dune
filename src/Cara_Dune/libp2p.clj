(ns Cara-Dune.libp2p
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string])

  (:import
   (java.net InetAddress InetSocketAddress)
   (io.netty.bootstrap Bootstrap)
   (io.netty.channel ChannelPipeline)
   (io.libp2p.core Connection Host PeerId)
   (io.libp2p.core.dsl HostBuilder)
   (io.libp2p.core.multiformats Multiaddr MultiaddrDns Protocol)
   (io.libp2p.core Libp2pException Stream P2PChannelHandler)
   (io.libp2p.core.multistream  ProtocolBinding StrictProtocolBinding)
   (io.libp2p.protocol Ping PingController ProtocolHandler ProtobufProtocolHandler
                       ProtocolMessageHandler #_ProtocolMessageHandler$DefaultImpls)
   (io.libp2p.security.noise NoiseXXSecureChannel)
   (io.libp2p.core.crypto PrivKey)

   (io.libp2p.pubsub.gossip Gossip GossipRouter GossipParams GossipScoreParams GossipPeerScoreParams)
   (io.libp2p.pubsub.gossip.builders GossipParamsBuilder GossipScoreParamsBuilder GossipPeerScoreParamsBuilder)
   (io.libp2p.pubsub PubsubApiImpl)
   (io.libp2p.etc.encode Base58)
   (io.libp2p.etc.util P2PService$PeerHandler)
   (io.libp2p.core.pubsub Topic MessageApi)
   (io.libp2p.discovery MDnsDiscovery)

   (java.util.function Function Consumer)
   (io.netty.buffer ByteBuf ByteBufUtil Unpooled)
   (java.util.concurrent CompletableFuture TimeUnit)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defmulti to-byte-array type)

(defmethod to-byte-array ByteBuf ^bytes
  [^ByteBuf bytebuf]
  (let [byte-arr (byte-array (.readableBytes bytebuf))]
    (->   (.slice bytebuf)
          (.readBytes byte-arr))
    byte-arr))

(comment

  (do
    (def ping (Ping.))
    (def host (->
               (HostBuilder.)
               (.protocol (into-array ProtocolBinding [ping]))
               (.secureChannel
                (into-array Function [(reify Function
                                        (apply
                                          [_ priv-key]
                                          (NoiseXXSecureChannel. ^PrivKey priv-key)))]))
               (.listen (into-array String ["/ip4/127.0.0.1/tcp/0"]))
               (.build)))
    (-> host (.start) (.get))
    (println (format "host listening on \n %s" (.listenAddresses host))))

  (do
    (def address (Multiaddr/fromString "/ip4/104.131.131.82/tcp/4001/ipfs/QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ"))
    (def pinger (-> ping (.dial host address) (.getController) (.get 5 TimeUnit/SECONDS)))
    (dotimes [i 5]
      (let [latency (-> pinger (.ping) (.get 5 TimeUnit/SECONDS))]
        (println latency))))

  (.stop host)



  ;
  )


(comment

  (do
    (defn start-host
      [gossip]
      (let [host (->
                  (HostBuilder.)
                  (.protocol (into-array ProtocolBinding [(Ping.) gossip]))
                  (.secureChannel
                   (into-array Function [(reify Function
                                           (apply
                                             [_ priv-key]
                                             (NoiseXXSecureChannel. ^PrivKey priv-key)))]))
                  (.listen (into-array String ["/ip4/127.0.0.1/tcp/0"]))
                  (.build))]
        (-> host (.start) (.get))
        (println (format "host listening on \n %s" (.listenAddresses host)))
        host))

    (defn connect
      ([host multiaddr]
       (connect host (.getFirst (.toPeerIdAndAddr multiaddr)) [(.getSecond (.toPeerIdAndAddr multiaddr))]))
      ([host peer-id multiaddrs]
       (->
        host
        (.getNetwork)
        (.connect ^PeerId peer-id (into-array Multiaddr multiaddrs))
        (.thenAccept (reify Consumer
                       (accept [_ connection]
                         (println ::connected connection)))))))

    (defn ping
      [host address]
      (let [pinger (-> (Ping.) (.dial host address) (.getController) (.get 5 TimeUnit/SECONDS))]
        (dotimes [i 5]
          (let [latency (-> pinger (.ping) (.get 5 TimeUnit/SECONDS))]
            (println latency)))))

    (defn subsribe
      [gossip key topic]
      (.subscribe gossip
                  (reify Consumer
                    (accept [_ msg]
                      (println ::gossip-recv-msg key (-> ^MessageApi msg
                                                         (.getData)
                                                         (to-byte-array)
                                                         (String. "UTF-8")))))
                  (into-array Topic [(Topic. topic)])))

    (defn publish
      [publisher topic string]
      (.publish publisher (Unpooled/wrappedBuffer (.getBytes string "UTF-8")) (into-array Topic [(Topic. topic)])))

    (defn linst-methods
      [v]
      (->> v
           clojure.reflect/reflect
           :members
           (filter #(contains? (:flags %) :public))
           (filter #(or (instance? clojure.reflect.Method %)
                        (instance? clojure.reflect.Constructor %)))
           (sort-by :name)
           (map #(select-keys % [:name :return-type :parameter-types]))
           clojure.pprint/print-table)))


  (do
    (def address1 (Multiaddr/fromString "/ip4/104.131.131.82/tcp/4001/ipfs/QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ"))
    (def address2 (Multiaddr/fromString "/dnsaddr/bootstrap.libp2p.io/ipfs/QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN"))
    (def address21 (Multiaddr/fromString "/ip4/147.75.109.213/tcp/4001/ipfs/QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN"))
    (def address3 (Multiaddr/fromString "/ip4/104.131.131.82/tcp/4001/ipfs/QmaCpDMGvV2BGHeYERUEnRQAwe3N8SzbUtfsmvsqQLuvuJ")))

  (do
    (def gossip1 (Gossip.))
    (def host1 (start-host gossip1))

    (def gossip2 (Gossip.))
    (def host2 (start-host gossip2))

    (def gossip3 (Gossip.))
    (def host3 (start-host gossip3)))

  (.getPeerId host1)
  (.listenAddresses host1)

  (do
    (def publisher1 (.createPublisher gossip1 (.getPrivKey host1) 1234))
    (def publisher2 (.createPublisher gossip2 (.getPrivKey host2) 1234))
    (def publisher3 (.createPublisher gossip3 (.getPrivKey host3) 1234)))

  (connect host1 (.getPeerId host2) (.listenAddresses host2))

  (do
    (connect host1 (.getPeerId host3) (.listenAddresses host3))
    (connect host2 (.getPeerId host3) (.listenAddresses host3)))

  (subsribe gossip1  :gossip1  "topic1")
  (subsribe gossip2  :gossip2  "topic1")


  (publish publisher1 "topic1" "from publisher1")
  (publish publisher2 "topic1" "from publisher2")


  (do
    (.stop host1) (.stop host2) (.stop host3))

  (connect host1 address3)
  (connect host2 address3)

  (.toString (.getSecond (.toPeerIdAndAddr address2)))

  (MultiaddrDns/resolve address2)

  (-> MultiaddrDns (clojure.reflect/reflect) (clojure.pprint/pprint))
  (-> io.libp2p.core.multiformats.MultiaddrDns$Companion (clojure.reflect/reflect) (clojure.pprint/pprint))

  (->>
   (InetAddress/getAllByName "libp2p.io")
   (filter (fn [inet-addr] (instance? java.net.Inet4Address inet-addr)))
   (map (fn [inet-addr] (.getHostAddress ^InetAddress inet-addr))))

  host -t TXT _dnsaddr.bootstrap.libp2p.io
  nslookup sjc-2.bootstrap.libp2p.io
  ; ewr-1.bootstrap.libp2p.io 147.75.77.187
  ; ams-rust.bootstrap.libp2p.io 145.40.68.179
  ; ams-2.bootstrap.libp2p.io 147.75.83.83
  ; nrt-1.bootstrap.libp2p.io 147.75.94.115
  ; sjc-2.bootstrap.libp2p.io 147.75.109.29
  ; sjc-1.bootstrap.libp2p.io 147.75.109.213


  (ping host1 address3)
  (ping host1 address21)


  ;
  )