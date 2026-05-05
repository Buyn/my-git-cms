(ns app.views.home
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [reitit.frontend.easy :as rfee]
            [app.state :as s]
            [app.api :as api]))

(defn home [_]
  (r/create-class
   {:component-did-mount #(rf/dispatch [::api/load-posts])
    :reagent-render
    (fn []
      (let [posts @(rf/subscribe [::s/posts])
            loading @(rf/subscribe [::s/loading])]
        [:div.page
         [:h1.page-title "Posts"]
         (if loading
           [:div.loading "Loading..."]
           [:ul.post-list
            (for [p posts]
              ^{:key p}
              [:li.post-item
               [:a {:href (rfee/href :app.core/post {:path p})} p]])])]))}))
