(ns Cara-Dune.beans
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]

   [datahike.api :as Deep-Thought.api]))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn process
  [{:keys [path]
    :as opts}]
  (let [config {:store {:backend :file :path path}
                :keep-history? true
                :name "main"}
        _ (when-not (Deep-Thought.api/database-exists? config)
            (Deep-Thought.api/create-database config))
        conn (Deep-Thought.api/connect config)]

    (Deep-Thought.api/transact
     conn
     [{:db/cardinality :db.cardinality/one
       :db/ident :id
       :db/unique :db.unique/identity
       :db/valueType :db.type/uuid}
      {:db/ident :name
       :db/valueType :db.type/string
       :db/cardinality :db.cardinality/one}])

    (Deep-Thought.api/transact
     conn
     [{:id #uuid "3e7c14ce-5f00-4ac2-9822-68f7d5a60952"
       :name  "Deep-Thought"}
      {:id #uuid "f82dc4f3-59c1-492a-8578-6f01986cc4c2"
       :name  "Wichita"}
      {:id #uuid "5358b384-3568-47f9-9a40-a9a306d75b12"
       :name  "Little-Rock"}])

    (->>
     (Deep-Thought.api/q '[:find ?e ?n
                           :where
                           [?e :name ?n]]
                         @conn)
     (println))

    (->>
     (Deep-Thought.api/q '[:find [?ident ...]
                           :where [_ :db/ident ?ident]]
                         @conn)
     (sort)
     (println))))