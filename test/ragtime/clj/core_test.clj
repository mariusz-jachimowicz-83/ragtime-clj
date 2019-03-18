(ns ragtime.clj.core-test
  (:require [clojure.test :refer :all]
            [ragtime.jdbc      :as ragtime-jdbc]
            [ragtime.core      :as ragtime]
            [ragtime.protocols :as ragtime-protocols]
            [clojure.java.jdbc :as jdbc]
            [ragtime.clj.core :refer [clj-file->ns-name]]))

(def db-spec "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")

(use-fixtures :each (fn reset-db [f]
                      (jdbc/execute! db-spec "DROP ALL OBJECTS")
                      (f)))

(defn table-names [db]
  (set (jdbc/query (:db-spec db) ["SHOW TABLES"] {:row-fn :table_name})))

(deftest test-load-directory
  (let [db  (ragtime-jdbc/sql-database db-spec)
        ms  (ragtime-jdbc/load-directory "test/migrations")
        idx (ragtime/into-index ms)]
    (ragtime/migrate-all db idx ms)
    (is (= #{"RAGTIME_MIGRATIONS" "FOO" "BAR" "BAZ" "QUZA" "QUZB" "QUXA" "QUXB" "LAST_TABLE" "CLJT_1" "CLJT_2"}
           (table-names db)))
    (is (= ["001-test" "002-bar" "003-test" "004-test" "005-test" "006-test" "007_test" "008_test"]
           (ragtime-protocols/applied-migration-ids db)))
    (ragtime/rollback-last db idx (count ms))
    (is (= #{"RAGTIME_MIGRATIONS"} (table-names db)))
    (is (empty? (ragtime-protocols/applied-migration-ids db)))))

(deftest test-load-resources
  (let [db  (ragtime-jdbc/sql-database db-spec)
        ms  (ragtime-jdbc/load-resources "migrations")
        idx (ragtime/into-index ms)]
    (ragtime/migrate-all db idx ms)
    (is (= #{"RAGTIME_MIGRATIONS" "FOO" "BAR" "BAZ" "QUZA" "QUZB" "QUXA" "QUXB" "LAST_TABLE" "CLJT_1" "CLJT_2"}
           (table-names db)))
    (is (= ["001-test" "002-bar" "003-test" "004-test" "005-test" "006-test" "007_test" "008_test"]
           (ragtime-protocols/applied-migration-ids db)))
    (ragtime/rollback-last db idx (count ms))
    (is (= #{"RAGTIME_MIGRATIONS"} (table-names db)))
    (is (empty? (ragtime-protocols/applied-migration-ids db)))))

(deftest ns-name-regex
  (is (=
       "asd.asdads.asdads"
       (clj-file->ns-name "(ns asd.asdads.asdads
  (:require [some.dep :refer [omg]]))")))

  (is (=
       "my.db.migrations.20190314091619-some_ident"
       (clj-file->ns-name "(ns my.db.migrations.20190314091619-some_ident
  \"Database migration to pear-shaped-obecjts table\"
  (:require
   [taoensso.timbre :as t]
   [clojure.java.jdbc :as j]
   [honeysql.core :as sql]
   [honeysql.helpers :as h]
   [honeysql-postgres.helpers :as hp]))
")))

  (is (=
       "my.db.migrations.20190314091619-some_ident"
       (clj-file->ns-name "(ns my.db.migrations.20190314091619-some_ident (:require
   [taoensso.timbre :as t]
)) "))))
