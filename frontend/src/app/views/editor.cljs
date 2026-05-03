(ns app.views.editor
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [app.state :as s]
            [app.api :as api]))

(defn editor [m]
  (let [path (get-in m [:path-params :path])
        content (r/atom nil)
        preview? (r/atom false)]
    (r/create-class
     {:component-did-mount #(rf/dispatch [::api/load-post path])
      :reagent-render
      (fn []
        (let [post @(rf/subscribe [::s/current-post])
              user @(rf/subscribe [::s/user])]
          (when (and post (nil? @content))
            (reset! content (:raw post)))
          (if (not= (:role user) "admin")
            [:div.page [:p.error "Access denied."]]
            [:div.page
             [:div.editor-toolbar
              [:span.editor-path path]
              [:button.btn-ghost {:on-click #(swap! preview? not)}
               (if @preview? "Edit" "Preview")]
              [:button.btn-primary
               {:on-click #(rf/dispatch [::api/save-post path @content])}
               "Save"]]
             (if @preview?
               [:div.post-content
                {:dangerouslySetInnerHTML {:__html (:html post)}}]
               [:textarea.editor-area
                {:value (or @content "")
                 :on-change #(reset! content (.. % -target -value))}])])))})))
