(defproject proj0 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [ring-server "0.5.0"]
                 [reagent "1.1.0"]
                 [reagent-utils "0.3.3"]
                 [re-frame "1.2.0"]
                 [day8.re-frame/http-fx "0.2.3"]
                 [ring "1.8.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [org.clojure/data.json "2.4.0"]
                 [compojure "1.6.2"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]
                 [hiccup "1.0.5"]
                 [yogthos/config "1.1.7"]
                 [org.clojure/clojurescript "1.10.866"
                  :scope "provided"]
                 [metosin/reitit "0.5.12"]
                 [metosin/ring-http-response "0.9.0"]
                 [pez/clerk "1.0.0"]
                 [venantius/accountant "0.2.5"
                  :exclusions [org.clojure/tools.reader]]
                 [org.clojure/core.async "0.4.474"]
                 [binaryage/devtools "1.0.3"]
                 ]

  :jvm-opts ["-Xmx1G"]
  
  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [lein-asset-minifier "0.4.6"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler proj0.handler/app
         :uberwar-name "proj0.war"}

  :min-lein-version "2.5.0"
  :uberjar-name "proj0.jar"
  :main proj0.server
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  [[:css {:source "resources/public/css/site.css"
          :target "resources/public/css/site.min.css"}]]

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to        "target/cljsbuild/public/js/app.js"
              :output-dir       "target/cljsbuild/public/js"
              :source-map       "target/cljsbuild/public/js/app.js.map"
              :optimizations :advanced
              :infer-externs true
              :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "proj0.core/mount-root"}
             :compiler
             {:main "proj0.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true}}



            }
   }

  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl
                      ]
   :css-dirs ["resources/public/css"]
   :ring-handler proj0.handler/app}



  :profiles {:dev {:repl-options {:init-ns proj0.repl}
                   :dependencies [[cider/piggieback "0.5.2"]
                                  [binaryage/devtools "1.0.2"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.9.1"]
                                  [prone "2020-01-17"]
                                  [figwheel-sidecar "0.5.20"]
                                  [nrepl "0.8.3"]
                                  [thheller/shadow-cljs "2.14.3"]
                                  [pjstadig/humane-test-output "0.10.0"]
                                  
 ]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.20"]
]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :shadow-cljs {:dependencies [[com.google.javascript/closure-compiler-unshaded "v20210505"]]}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
