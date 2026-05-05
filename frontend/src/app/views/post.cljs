(ns app.views.post
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [app.state :as s]
            [app.api :as api]
            [app.views.comments :refer [comments-section]]))

(defn post [m]
  (let [path (get-in m [:path-params :path])]
    (r/create-class
     {:component-did-mount #(rf/dispatch [::api/load-post path])
      :reagent-render
      (fn []
        (let [post    @(rf/subscribe [::s/current-post])
              user    @(rf/subscribe [::s/user])
              loading @(rf/subscribe [::s/loading])]
          [:div {:class "max-w-3xl mx-auto px-6 py-8"}
           (if loading
             [:p {:class "text-muted text-center py-8"} "Loading..."]
             (when post
               [:<>
                (when (= (:role user) "admin")
                  [:a {:href  (str "/edit/" path)
                       :class "inline-block mb-4 border border-muted text-muted px-3 py-1
                                rounded-sm hover:border-accent hover:text-accent transition-colors text-sm"}
                   "Edit"])
                [:article {:class "prose bg-surface border border-DEFAULT rounded-sm p-8 mb-8"
                           :dangerouslySetInnerHTML {:__html (:html post)}}]
                [comments-section path]]))]))})))
