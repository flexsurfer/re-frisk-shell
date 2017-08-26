(ns re-frisk-shell.re-com.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [re-frisk-shell.re-com.ui :refer [small-button]]
            [re-frisk-shell.re-com.vendors :refer [star]]
            [re-com.core :refer [v-box h-box gap box label
                                 h-split v-split
                                 input-text input-textarea p
                                 checkbox scroller] :refer-macros [handler-fn]]
            [clojure.string :as str]
            [re-frisk-shell.frisk :as frisk]))

(defn event-list-item []
  (fn [item selected-tab-id checkbox-val]
    (let [event (str (first (if (:event item) (:event item) item)))
          ;selected? (= @selected-tab-id (:id tab))
          namespace (str/split (str/replace event #":" "") #"/")
          splited-label (str/split (first namespace) #"\.")]
      [:a
       {:href  "#"
        :class (str "list-group-item")                      ;(when selected? " active"))
        :style {:padding "5" :white-space :pre}}
       ;:on-click (handler-fn (reset! selected-tab-id (:id tab)))}
       [:span (if (and @checkbox-val (> (count namespace) 1))
                (str ":" (str/join "." (mapv first splited-label))
                     "/" (last namespace))
                event)]])))

(defn filter-event [text]
  (fn [item]
    (let [name (str/lower-case (name (first (if (:event item) (:event item) item))))
          text (str/lower-case text)]
      (not= (str/index-of name text) nil))))

(defn events-list []
  (fn [filtered-events selected-tab-id checkbox-val]
    [v-box
     :class "list-group"
     :children (for [item @filtered-events]
                 [event-list-item item selected-tab-id checkbox-val])]))

(defn events-view [re-frame-events]
  (let [checkbox-val (reagent/atom nil)
        selected-tab-id (reagent/atom nil)
        text-val (reagent/atom "")
        filtered-events (reaction (if (= @text-val "")
                                    @re-frame-events
                                    (filter (filter-event @text-val) @re-frame-events)))]
    (fn []
      [v-box
       :size "1"
       :children [;events filter
                  [h-box
                   :children [[box
                               :size "1"
                               :child
                               [input-text
                                :style {:height :auto :padding "0"}
                                :width "100%"
                                :model text-val
                                :change-on-blur? false
                                :placeholder "Filter events"
                                :on-change #(reset! text-val %)]]
                              [small-button "X"]]]
                  ;truncate checkbox
                  [checkbox
                   :model checkbox-val
                   :on-change #(reset! checkbox-val %)
                   :label "truncate"]
                  ;events
                  [scroller
                   :size "1"
                   :v-scroll :auto
                   :h-scroll :auto
                   :child [events-list filtered-events selected-tab-id checkbox-val]]
                  ;bottom buttons
                  [h-box
                   :align :center
                   :gap "5px"
                   :style {:padding-top "3"}
                   :children [[small-button "import"]
                              [small-button "export"]
                              [gap :size "1"]
                              [small-button "clear"]]]]])))

(def expand-by-default (reduce #(assoc-in %1 [:data-frisk %2 :expanded-paths] #{[]}) {} (range 1)))

(defn main-frisk []
  (let [state-atom (reagent/atom expand-by-default)]
    (fn [app-db checkbox-sorted-val]
      (let [db @app-db
            db' (if (and checkbox-sorted-val (map? db))
                  (into (sorted-map) db)
                  db)]
        [frisk/Root db' 0 state-atom]))))

(defn handler-frisk []
  (let [state-atom (reagent/atom expand-by-default)]
    (fn [handlers]
      [frisk/Root @handlers 0 state-atom])))

(defn main-view [_]
  (let [checkbox-sorted-val (reagent/atom true)]
    (fn [re-frame-data]
      [v-box
       :size "1"
       :style {:padding "0"}
       :children [[v-split
                   :size "1"
                   :style {:padding "0"
                           :margin  "0"}
                   :initial-split "0"
                   :panel-1 [scroller
                             :size "1"
                             :v-scroll :auto
                             :h-scroll :auto
                             :style {:background-color "#f3f3f3"}
                             :child [box
                                     :child [handler-frisk (:id-handler @re-frame-data)]]]
                   :panel-2 [v-box
                             :size "1"
                             :children
                             [[v-split
                               :size "1"
                               :style {:padding "0"
                                       :margin  "0"}
                               :initial-split "100"
                               ;MAIN FRISK
                               :panel-1 [v-box
                                         :size "1"
                                         :style {:background-color "#4e5d6c"}
                                         :children
                                         [[checkbox
                                           :model checkbox-sorted-val
                                           :on-change (fn [val]
                                                        (reset! checkbox-sorted-val val)
                                                        (swap! (:app-db @re-frame-data) assoc :sorted true)
                                                        (js/setTimeout #(swap! (:app-db @re-frame-data) dissoc :sorted) 200))
                                           :label "sorted"]
                                          [scroller
                                           :size "1"
                                           :v-scroll :auto
                                           :h-scroll :auto
                                           :style {:background-color "#f3f3f3"}
                                           :child
                                           [box
                                            :child [main-frisk (:app-db @re-frame-data) @checkbox-sorted-val]]]]]
                               ;event frisk
                               :panel-2 [box
                                         :size "1"
                                         :style {:background-color "#4e5d6c"}
                                         :child [p "Content 3"]]]
                              [h-box
                               :style {:padding "0"}
                               :children [[gap :size "1"]
                                          [star]]]]]]]])))

(defn main [re-frame-data re-frame-events]
  [v-box
   :height "100%"
   :children [[h-split
               :size "1"
               :initial-split "20"
               :panel-1 [events-view re-frame-events]
               :panel-2 [main-view re-frame-data]]]])
