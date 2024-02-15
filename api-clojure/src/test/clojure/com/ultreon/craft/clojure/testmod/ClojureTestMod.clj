(ns com.ultreon.craft.clojure.testmod.ClojureTestMod
  (:import (com.ultreon.craft ModInit)))

(defn- on-initialize []
  (println (format "Hello from Clojure! Mod ID: %s" MOD_ID)))

(deftype ^:public ClojureTestMod []
  ModInit
  (onInitialize [_] (on-initialize)))

(def ^:public ^:static MOD_ID "clojure_testmod" ^:field)
