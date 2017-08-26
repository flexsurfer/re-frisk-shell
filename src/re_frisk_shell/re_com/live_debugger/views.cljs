(ns re-frisk-shell.re-com.live-debugger.views
  (:require [reagent.core :as reagent]
            [re-com.core :refer [v-box h-box gap box label
                                 h-split v-split
                                 input-text input-textarea p
                                 checkbox scroller]]))

(defn live-debugger []
  (let [text-area-value (reagent/atom "")]
    (fn []
      [v-box
       :size "1"
       :children
       [[h-box
         :height "50px"
         :children [[box :size "1"
                     :child
                     [input-textarea
                      :style {:padding "0"}
                      :width "100%"
                      :height "50px"
                      :model text-area-value
                      :on-change #(reset! text-area-value %)]]
                    [big-button
                     {:height "50px"} "Run"]]]
        [box
         :size "1"
         :style {:background-color "#4e5d6c"}
         :child
         [p "Content"]]]])))
