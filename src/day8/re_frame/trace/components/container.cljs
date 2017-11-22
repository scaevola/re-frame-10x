(ns day8.re-frame.trace.components.container
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [re-frame.db :as db]
            [day8.re-frame.trace.panels.app-db :as app-db]
            [day8.re-frame.trace.panels.subvis :as subvis]
            [day8.re-frame.trace.panels.traces :as traces]
            [day8.re-frame.trace.panels.subs :as subs]
            [re-frame.trace]
            [reagent.core :as r]))

(defn tab-button [panel-id title]
  (let [selected-tab @(rf/subscribe [:settings/selected-tab])]
    [:button {:class    (str "tab button " (when (= selected-tab panel-id) "active"))
              :on-click #(rf/dispatch [:settings/selected-tab panel-id])} title]))

(defn devtools-inner [traces opts]
  (let [selected-tab     (rf/subscribe [:settings/selected-tab])
        panel-type       (:panel-type opts)
        external-window? (= panel-type :popup)
        unloading?       (rf/subscribe [:global/unloading?])]
    [:div.panel-content
     {:style {:width "100%" :display "flex" :flex-direction "column"}}
     (when (and external-window? @unloading?)
       [:h1.host-closed "Host window has closed. Reopen external window to continue tracing."])
     (when-not (re-frame.trace/is-trace-enabled?)
       [:h1.host-closed {:style {:word-wrap "break-word"}} "Tracing is not enabled. Please set " [:pre "{\"re_frame.trace.trace_enabled_QMARK_\" true}"] " in " [:pre ":closure-defines"]])
     [:div.panel-content-top
      [:div.nav
       (tab-button :traces "Traces")
       (tab-button :app-db "App DB")
       (tab-button :subs "Subs")
       #_ (tab-button :subvis "SubVis")
       (when-not external-window?
         [:img.popout-icon
          {:src      (str "data:image/svg+xml;utf8,"
                          "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 8 8\" x=\"0px\" y=\"0px\">\n    <path fill=\"#444444\" d=\"M0 0v8h8v-2h-1v1h-6v-6h1v-1h-2zm4 0l1.5 1.5-2.5 2.5 1 1 2.5-2.5 1.5 1.5v-4h-4z\"/>\n</svg>\n")
           :on-click #(rf/dispatch-sync [:global/launch-external])}])]]
     (case @selected-tab
       :traces [traces/render-trace-panel traces]
       :app-db [app-db/render-state db/app-db]
       :subvis [subvis/render-subvis traces]
       :subs [subs/subs-panel]
       [app-db/render-state db/app-db])]))
