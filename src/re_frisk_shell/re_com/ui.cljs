(ns re-frisk-shell.re-com.ui)

(defn small-button
  ([label] (small-button {} label))
  ([{} label]
   [:a {:href "#" :class "btn btn-primary btn-xs"} label]))

(defn big-button [style label]
  [:a {:href "#" :class "btn btn-primary btn-lg" :style style} label])
