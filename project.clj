(defproject re-frisk-shell "0.5.1"
  :description "re-frisk ui shell"
  :url "https://github.com/flexsurfer/re-frisk"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}

  :dependencies [[reagent "0.7.0"]
                 [re-com "2.1.0"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.13"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/re-frisk/js/compiled" "target"]

  :figwheel {:http-server-root "re-frisk"
             :server-port 5309}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "dev"]
                :figwheel {:on-jsload "re-frisk-shell.demo/on-js-reload"}

                :compiler {:main re-frisk-shell.demo
                           :asset-path "js/compiled/out/re-frisk"
                           :output-to "resources/re-frisk/js/compiled/re_frisk.js"
                           :output-dir "resources/re-frisk/js/compiled/out/re-frisk"
                           :source-map-timestamp true}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.7.2"]
                                  [com.taoensso/sente "1.11.0"]
                                  [com.cognitect/transit-cljs "0.8.239"]
                                  [figwheel-sidecar "0.5.4-7"]
                                  [com.cemerick/piggieback "0.2.1"]]}})