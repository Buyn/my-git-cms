(ns app.views.editor
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [app.state :as s]
            [app.api :as api]))

(defn editor [m]
  (let [path     (get-in m [:path-params :path])
        content  (r/atom nil)
        preview? (r/atom false)
        message  (r/atom (str "Update " path))]
    (r/create-class
     {:component-did-mount #(rf/dispatch [::api/load-post path])
      :reagent-render
      (fn []
        (let [post @(rf/subscribe [::s/current-post])
              user @(rf/subscribe [::s/user])]
          (when (and post (nil? @content))
            (reset! content (:raw post)))
          (if (not= (:role user) "admin")
            [:div {:class "max-w-3xl mx-auto px-6 py-8"}
             [:p {:class "text-danger"} "Access denied."]]
            [:div {:class "max-w-3xl mx-auto px-6 py-8"}
             ;; Toolbar
             [:div {:class "flex items-center gap-3 mb-4 pb-3 border-b border-DEFAULT"}
              [:span {:class "text-muted flex-1 text-sm"} path]
              [:button {:on-click #(swap! preview? not)
                        :class    "border border-DEFAULT text-muted px-3 py-1 rounded-sm text-sm
                                   hover:border-accent hover:text-accent transition-colors"}
               (if @preview? "Edit" "Preview")]
              [:button {:on-click #(rf/dispatch [::api/save-post path @content @message])
                        :class    "border border-accent text-accent px-3 py-1 rounded-sm text-sm
                                   hover:bg-[var(--accent-dim)] transition-colors"}
               "Save"]]
             (if @preview?
               [:article {:class "prose bg-surface border border-DEFAULT rounded-sm p-8"
                          :dangerouslySetInnerHTML {:__html (:html post)}}]
               [:<>
                [:textarea {:value     (or @content "")
                            :on-change #(reset! content (.. % -target -value))
                            :class     "w-full min-h-[60vh] bg-surface border border-DEFAULT
                                        rounded-sm text-primary font-mono text-sm p-4
                                        resize-y outline-none focus:border-accent
                                        focus:shadow-glow transition-colors"}]
                [:input {:type        "text"
                         :placeholder "Commit message"
                         :value       @message
                         :on-change   #(reset! message (.. % -target -value))
                         :class       "mt-2 w-full bg-surface border border-DEFAULT rounded-sm
                                       text-primary font-mono text-sm px-3 py-2 outline-none
                                       focus:border-accent transition-colors"}]])])))})))
