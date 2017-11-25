(ns re-frisk-shell.frisk
  (:require [reagent.core :as reagent]
            [clojure.string :as str]
            [clojure.set :as set]
            [cljs.reader :as reader]))
;;original idea Odin Hole Standal https://github.com/Odinodin/data-frisk-reagent
(declare DataFrisk)

(defn ExpandButton [{:keys [expanded? path emit-fn]}]
  [:button {:style {:border 0
                    :backgroundColor "transparent" :width "20px" :height "20px"}
            :onClick #(emit-fn (if expanded? :contract :expand) path)}
   [:svg {:viewBox "0 0 100 100"
          :width "100%" :height "100%"
          :style {:transition "all 0.2s ease"
                  :transform (when expanded? "rotate(90deg)")}}
    [:polygon {:points "0,0 0,100 100,50" :stroke "gray" :color "gray"}]]])

(def styles
  {:shell {:backgroundColor "#FAFAFA"
           :fontFamily "Consolas,Monaco,Courier New,monospace"
           :fontSize "12px"
           :z-index 9999}
   :strings {:color "#4Ebb4E"}
   :keywords {:color "purple"}
   :numbers {:color "blue"}
   :nil {:color "red"}
   :shell-visible-button {:backgroundColor "#4EE24E"}})

(defn ExpandAllButton [emit-fn data]
  [:button {:onClick #(emit-fn :expand-all data)
            :style {:padding "0px"
                    :borderTopLeftRadius "2px"
                    :borderBottomLeftRadius "2px"
                    :cursor "pointer"
                    :border "1px solid darkgray"
                    :backgroundColor "white"}}
   "Expand all"])

(defn CollapseAllButton [emit-fn data]
  [:button {:onClick #(emit-fn :collapse-all)
            :style {:padding "0px"
                    :cursor "pointer"
                    :borderTopRightRadius "2px"
                    :borderBottomRightRadius "2px"
                    :borderTop "1px solid darkgray"
                    :borderBottom "1px solid darkgray"
                    :borderRight "1px solid darkgray"
                    :borderLeft "0"
                    :backgroundColor "white"}}
   "Collapse all"])

(defn FilterEditBox [emit-fn filter]
  [:input {:type "text"
             :style {:flex 1 :margin-left 5}
             :value filter
             :placeholder "[:a :b ...]"
             :on-change #(emit-fn :filter-change (.. % -target -value))}])

(defn FilterReset [emit-fn]
  [:button {:style {:margin-right 5 :width 25}
            :onClick #(emit-fn :filter-reset)} "X"])

(defn node-clicked [{:keys [event emit-fn path] :as all}]
  (.stopPropagation event)
  (if (.-shiftKey event)
    (emit-fn :copy path)
    (emit-fn :filter-change (str path))))

(defn NilText []
  [:span {:style (:nil styles)} (pr-str nil)])

(defn StringText [data]
  [:span {:style (:strings styles)} (pr-str data)])

(defn KeywordText [data]
  [:span {:style (:keywords styles)} (str data)])

(defn NumberText [data]
  [:span {:style (:numbers styles)} data])

(defn is-prefix [needle haystack]
  (and (< (count needle) (count haystack))
       (= needle (subvec haystack 0 (count needle)))))

(defn Node [{:keys [data path emit-fn swappable node matching-paths] :as val}]
  [:span {:style {:padding-top "5px"}}
   (when node
     [:span {:style {:padding-left "20px"}}
      [Node node]])
   [:span
    {:onClick #(node-clicked {:event %, :emit-fn emit-fn :path path})
     :style (merge (when node {:padding-left "10px"})
                   (when (get matching-paths path)
                     {:background-color "#fff9db"}))}
    (cond
     (nil? data)
     [NilText]

     (string? data)
     (if swappable
       [:input {:type "text"
                :default-value (str data)
                :on-change
                (fn string-changed [e]
                  (emit-fn :changed path (.. e -target -value)))}]
       [StringText data])

     (keyword? data)
     (if swappable
       [:input {:type "text"
                :default-value (name data)
                :on-change
                (fn keyword-changed [e]
                  (emit-fn :changed path (keyword (.. e -target -value))))}]
       [KeywordText data])

     (object? data)
     "Object"

     (number? data)
     (if swappable
       [:input {:type "number"
                :default-value data
                :on-change
                (fn number-changed [e]
                  (emit-fn :changed path (js/Number (.. e -target -value))))}]
       [NumberText data])
     :else
     (str data))]])

;; A path is expanded if it is explicitly expanded or if it is a part of
;; current selection
(defn is-expanded [expanded-paths expanded-matching-paths path]
  (or (get expanded-paths path)
      (get expanded-matching-paths path)))

(defn KeyValNode [{[k v] :data :keys [path expanded-paths matching-paths expanded-matching-paths emit-fn swappable]}]
  [:div {:style {:display "flex"}}
   [DataFrisk {:node {:data k
                      :emit-fn emit-fn
                      :path (conj path k)
                      :matching-paths matching-paths}
               :data v
               :swappable swappable
               :path (conj path k)
               :expanded-paths expanded-paths
               :matching-paths matching-paths
               :expanded-matching-paths expanded-matching-paths
               :emit-fn emit-fn}]])

(defn MapNode [{:keys [data path expanded-paths matching-paths expanded-matching-paths emit-fn node] :as all}]
  (let [expanded? (is-expanded expanded-paths expanded-matching-paths path)]
    [:div {:style {:display "flex" :padding-top "3px"}}
     [:div {:style {:flex "0 1 auto"}}
      (if (empty? data)
        [:div {:style {:width "20px"}}]
        [ExpandButton {:expanded? expanded?
                       :path path
                       :emit-fn emit-fn}])]
     [:div {:style {:flex 1}}
      (when node
        [Node node])
      [:span " {"]
      [:span (str (count (keys data)) " keys")]
      [:span "}"]
      (when expanded?
        (map-indexed (fn [i x] ^{:key i}
                       [:div {:style {:flex 1}}
                        [KeyValNode (assoc all :data x)]])
                     data))]]))

(defn ListVecNode [{:keys [data path expanded-paths matching-paths expanded-matching-paths emit-fn swappable node]}]
  (let [expanded? (is-expanded expanded-paths expanded-matching-paths path)]
    [:div {:style {:display "flex" :padding-top "3px"}}
     [:div {:style {:flex "0 1 auto"}}
      (if (empty? data)
        [:div {:style {:width "20px"}}]
        [ExpandButton {:expanded? expanded?
                       :path path
                       :emit-fn emit-fn}])]
     [:div {:style {:flex 1}}
      (when node
        [Node node])
      [:span (if (vector? data) " [" " (")
        (str (count data) " items")]
      [:span (if (vector? data) "]" ")")]
      (when expanded?
        (map-indexed (fn [i x] ^{:key i} [:div {:style {:flex 1}}
                                          [DataFrisk {:data x
                                                      :swappable swappable
                                                      :path (conj path i)
                                                      :expanded-paths expanded-paths
                                                      :matching-paths matching-paths
                                                      :expanded-matching-paths expanded-matching-paths
                                                      :emit-fn emit-fn}]]) data))]]))

(defn SetNode [{:keys [data path expanded-paths matching-paths expanded-matching-paths emit-fn swappable node]}]
  (let [expanded? (is-expanded expanded-paths expanded-matching-paths path)]
    [:div {:style {:display "flex" :padding-top "3px"}}
     [:div {:style {:flex "0 1 auto"}}
      (if (empty? data)
        [:div {:style {:width "20px"}}]
        [ExpandButton {:expanded? expanded?
                       :path path
                       :emit-fn emit-fn}])]
     [:div {:style {:flex 1}}
      (when node
        [Node node])
      [:span " #{"
        (str (count data) " items")]
      [:span "}"]
      (when expanded?
        (map-indexed (fn [i x] ^{:key i} [:div {:style {:flex 1}}
                                          [DataFrisk {:data x
                                                      :swappable swappable
                                                      :path (conj path x)
                                                      :expanded-paths expanded-paths
                                                      :matching-paths matching-paths
                                                      :expanded-matching-paths expanded-matching-paths
                                                      :emit-fn emit-fn}]]) data))]]))

(defn DataFrisk [{:keys [data] :as all}]
  (cond (map? data) [MapNode all]
        (set? data) [SetNode all]
        (or (seq? data) (vector? data)) [ListVecNode all]
        (satisfies? IDeref data) [DataFrisk (assoc all :data @data)]
        :else [Node all]))

(defn conj-to-set [coll x]
  (conj (or coll #{}) x))

(defn expand-all-paths [root-value]
  (loop [remaining [{:path [] :node root-value}]
         expanded-paths #{}]
    (if (seq remaining)
      (let [[current & rest] remaining
            current-node (if (satisfies? IDeref (:node current)) @(:node current) (:node current))]
        (cond (map? current-node)
              (recur
                (concat rest (map (fn [[k v]] {:path (conj (:path current) k)
                                               :node v})
                                  current-node))
                (conj expanded-paths (:path current)))
              (or (seq? current-node) (vector? current-node))
              (recur
                (concat rest (map-indexed (fn [i node] {:path (conj (:path current) i)
                                                        :node node})
                                          current-node))
                (conj expanded-paths (:path current)))
              :else
              (recur
                rest
                (if (coll? current-node)
                  (conj expanded-paths (:path current))
                  expanded-paths))))
      expanded-paths)))

; Taken from prbroadfoot/data-frisk-reagent, MIT-licensed
(defn copy-to-clipboard [data]
  (let [textArea (.createElement js/document "textarea")]
    (doto textArea
      ;; Put in top left corner of screen
      (aset "style" "position" "fixed")
      (aset "style" "top" 0)
      (aset "style" "left" 0)
      ;; Make it small
      (aset "style" "width" "2em")
      (aset "style" "height" "2em")
      (aset "style" "padding" 0)
      (aset "style" "border" "none")
      (aset "style" "outline" "none")
      (aset "style" "boxShadow" "none")
      ;; Avoid flash of white box
      (aset "style" "background" "transparent")
      (aset "value" data))

    (.appendChild (.-body js/document) textArea)
    (.select textArea)

    (.execCommand js/document "copy")
    (.removeChild (.-body js/document) textArea)))

(defn parenthesize [s]
  (str "[" (-> s
               (str/replace #"^[\[]" "")
               (str/replace "[]]$" "")) "]"))

(defn parse-filter [filter]
  (let [filter (parenthesize filter)]
    (try
      (reader/read-string filter)
      (catch js/Error e []))))

(defn update-filter [state id raw-filter]
  (let [filter (parse-filter raw-filter)]
    (-> state
        (assoc-in [:data-frisk id :raw-filter] raw-filter)
        (assoc-in [:data-frisk id :filter] filter))))

(defn emit-fn-factory [state-atom id swappable]
  (fn [event & args]
    (case event
      :expand (swap! state-atom update-in [:data-frisk id :expanded-paths] conj-to-set (first args))
      :expand-all (swap! state-atom assoc-in [:data-frisk id :expanded-paths] (expand-all-paths (first args)))
      :contract (swap! state-atom update-in [:data-frisk id :expanded-paths] disj (first args))
      :collapse-all (swap! state-atom assoc-in [:data-frisk id :expanded-paths] #{})
      :copy (copy-to-clipboard (first args))
      :filter-change (swap! state-atom update-filter id (first args))
      :filter-reset (swap! state-atom update-filter id "")
      :changed (let [[path value] args]
                 (if (seq path)
                   (swap! swappable assoc-in path value)
                   (reset! swappable value))))))

(defn match-value [value filter-part]
  (str/includes? (str/lower-case (str value)) (str/lower-case (str filter-part))))

(defn match-path [filter path]
  (cond (empty? filter) nil
        (empty? path) nil
        ;; [foo] matches [... *foo*]
        (= (count filter) 1) (match-value (last path) (first filter))
        ;; [foo bar baz] matches [*foo* ... *bar* ... *baz*]
        ;; and [...*foo* ... *bar* ... *baz*]
        :else (if (match-value (first path) (first filter))
                (match-path (rest filter) (rest path))
                (match-path filter (rest path)))))

(defn walk-paths
  ([data]
   (walk-paths [] data))
  ([prefix data]
   (conj
    (cond (map? data)
          (apply set/union
                 (map (fn [[k v]] (walk-paths (conj prefix k) v)) data))
          (set? data)
          (apply set/union
                 (map (fn [v] (walk-paths (conj prefix v) v)) data))
          (or (seq? data) (vector? data))
          (apply set/union
                 (map-indexed
                  (fn [i v] (walk-paths (conj prefix i) v)) data))
          (satisfies? IDeref data) (walk-paths prefix @data)
          :else #{})
    prefix)))

(defn matching-paths [data filter']
  (set (filter #(match-path filter' %) (walk-paths data))))

(defn prefixes [path]
  (set (reductions conj [] path)))

;; Any node which is a prefix of a matched path needs to be expnaded
(defn expanded-matching-paths [paths]
  (apply set/union (map prefixes paths)))

(defn Root [data id state-atom]
  (let [data-frisk (:data-frisk @state-atom)
        swappable (when (satisfies? IAtom data)
                    data)
        filter (or (get-in data-frisk [id :filter]) [])
        matching (matching-paths data filter)
        expanded-matching (expanded-matching-paths matching)
        emit-fn (emit-fn-factory state-atom id swappable)]
    [:div {:style {:color "#444444"}}
     [:div {:style {:padding "4px 2px" :display "flex"}}
      [ExpandAllButton emit-fn data]
      [CollapseAllButton emit-fn]
      [FilterEditBox emit-fn (get-in data-frisk [id :raw-filter])]
      [FilterReset emit-fn]]
     [DataFrisk {:data data
                 :swappable swappable
                 :path []
                 :expanded-paths (get-in data-frisk [id :expanded-paths])
                 :matching-paths matching
                 :expanded-matching-paths expanded-matching
                 :filter (or (get-in data-frisk [id :filter]) [])
                 :emit-fn emit-fn}]]))

(def expand-by-default (reduce #(assoc-in %1 [:data-frisk %2 :expanded-paths] #{[]}) {} (range 1)))

(defn main-frisk [re-frame-data checkbox-sorted-val]
  (let [state-atom (reagent/atom expand-by-default)]
    (fn [_]
      (let [db @(:app-db @re-frame-data)
            db' (if (and @checkbox-sorted-val (map? db))
                  (into (sorted-map) db)
                  db)]
        [Root db' 0 state-atom]))))

(defn handler-frisk [re-frame-data]
  (let [state-atom (reagent/atom expand-by-default)]
    (fn [_]
      [Root @(:id-handler @re-frame-data) 0 state-atom])))

(defn event-frisk [deb-data]
  (let [state-atom (reagent/atom expand-by-default)]
    (fn [_]
      [Root (get-in @deb-data [:event-data :event]) 0 state-atom])))
