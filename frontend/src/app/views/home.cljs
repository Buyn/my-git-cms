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
      (let [posts   @(rf/subscribe [::s/posts])
            loading @(rf/subscribe [::s/loading])]
        [:div {:class "max-w-3xl mx-auto px-6 py-8"}
         [:h1 {:class "text-xl font-bold text-accent shadow-glow mb-6
                        border-b border-DEFAULT pb-2"}
          "Posts"]
         (if loading
           [:p {:class "text-muted text-center py-8"} "Loading..."]
           [:ul {:class "flex flex-col gap-2"}
            (for [p posts]
              ^{:key p}
              [:li {:class "bg-surface border border-DEFAULT rounded-sm px-4 py-3
                            hover:border-accent hover:shadow-glow transition-all"}
               [:a {:href  (rfee/href :app.core/post {:path p})
                    :class "text-primary hover:text-accent"}
                p]])])]))}))
