(ns com.ultreon.craft.clojure.testmod.init.ModItems
  (:require [clojure.test :refer :all])
  (:import (com.ultreon.craft.item Item Item$Properties)
           (com.ultreon.craft.registry DeferRegistry Registries DeferredElement)
           (java.util.function Supplier)))
  (:require [Item]
            [DeferRegistry]
            [Registries]
            [DeferredElement])

(def ^:field ^:static REGISTER (DeferRegistry/of (com.ultreon.craft.clojure.testmod.ClojureTestMod/MOD_ID, Registries/ITEM) DeferRegistry))

(def TEST_ITEM (REGISTER .register ("test_item", Supplier #(Item. (Item$Properties.))) DeferredElement))

(defn register []
  (.register REGISTER))

