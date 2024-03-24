(ns com.ultreon.craft.clojure.ClojureApiMod
  (:import (com.ultreon.craft ModInit)))

(def mod-id "clojure_testmod")

(defn- on-initialize []
  (println (format "Hello from Clojure! Mod ID: %s" mod-id)))

(deftype ^:public JavaApiMod []
  ModInit
  (onInitialize [_] (on-initialize)))
