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
        (let [post @(rf/subscribe [::s/current-post])
              user @(rf/subscribe [::s/user])
              loading @(rf/subscribe [::s/loading])]
          [:div.page
           (if loading
             [:div.loading "Loading..."]
             (when post
               [:<>
                (when (= (:role user) "admin")
                  [:a.btn-secondary {:href (str "/edit/" path)} "Edit"])
                [:article.post-content
                 {:dangerouslySetInnerHTML {:__html (:html post)}}]
                [comments-section path]]))]))})))
