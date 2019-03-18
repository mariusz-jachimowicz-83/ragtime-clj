(ns ragtime.clj.core
  (:refer-clojure :exclude [load-file])
  (:require
    [clojure.java.jdbc :as jdbc]
    [ragtime.jdbc]
    [ragtime.protocols])
  (:import [java.io File]))

(defn- execute-clj! [db-spec clj-fn]
  (clj-fn db-spec))

(defrecord CljMigration [id up down]
  ragtime.protocols/Migration
  (id [_] id)
  (run-up!   [_ db] (execute-clj! (:db-spec db) up))
  (run-down! [_ db] (execute-clj! (:db-spec db) down)))

(defn clj-migration
  "Create a Ragtime migration from a map with a unique :id, and :up and :down
  keys that map to clojure migration function."
  [migration-map]
  (map->CljMigration migration-map))

(defn- file-extension [file]
  (re-find #"\.[^.]*$" (str file)))

(let [pattern (re-pattern (str "([^\\" File/separator "]*)\\" File/separator "?$"))]
  (defn- basename [file]
    (second (re-find pattern (str file)))))

(defn- remove-extension [file]
  (second (re-matches #"(.*)\.[^.]*" (str file))))

(defn clj-file->ns-name [file-content]
  "Extract ns name from `file-content`"
  (->> file-content
       (re-find #"^\(ns\s+([^\s);]+)")
       second))

(defn- resolve-fn! [namespace-symbol fn-symbol]
  (or (ns-resolve namespace-symbol fn-symbol)
      (throw (Exception. (format "Could not resolve %s/%s on the classpath"
                                 (name namespace-symbol)
                                 (name fn-symbol))))))

(defmethod ragtime.jdbc/load-files ".clj" [files]
  (for [file files]
    (let [ns-sym  (-> file slurp clj-file->ns-name symbol)
          _       (require ns-sym)
          up-fn   (resolve-fn! ns-sym 'up)
          down-fn (resolve-fn! ns-sym 'down)
          id      (-> file basename remove-extension)]
      (clj-migration {:id   id
                      :up   up-fn
                      :down down-fn}))))
