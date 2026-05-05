(ns app.views.nav
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as rfee]
            [app.state :as s]
            [app.api :as api]))

(defn nav []
  (let [user      @(rf/subscribe [::s/user])
        theme     @(rf/subscribe [::s/theme])
        dark-mode @(rf/subscribe [::s/dark-mode])]
    [:nav {:class "sticky top-0 z-50 flex items-center justify-between
                   px-8 py-3 bg-surface border-b border-DEFAULT font-mono text-sm"}
     [:a {:href  (rfee/href :app.core/home)
          :class "font-bold tracking-widest text-accent shadow-glow"}
      "⬡ GIT CMS"]
     [:div {:class "flex items-center gap-5"}
      [:a {:href (rfee/href :app.core/home) :class "text-primary hover:text-accent"} "Home"]
      (if user
        [:<>
         [:span {:class "text-muted text-xs"} (:username user)]
         [:a {:href  (rfee/href :app.core/profile)
              :class "text-primary hover:text-accent"} "Profile"]
         [:button {:on-click #(rf/dispatch [::api/logout])
                   :class    "border border-DEFAULT text-muted px-3 py-1 rounded-sm
                              hover:border-accent hover:text-accent transition-colors"}
          "Logout"]]
        [:a {:href  (str js/window.location.origin "/auth/login")
             :class "border border-accent text-accent px-3 py-1 rounded-sm
                    hover:bg-[var(--accent-dim)] transition-colors"}
         "Login"])
      ;; Theme controls
      [:select {:value     theme
                :on-change #(rf/dispatch [::s/set-theme (.. % -target -value)])
                :class     "bg-raised border border-DEFAULT text-primary text-xs
                            px-2 py-1 rounded-sm cursor-pointer outline-none"}
       (for [t s/themes]
         ^{:key t}
         [:option {:value t} (subs t 6)])]
      [:button {:on-click #(rf/dispatch [::s/toggle-dark])
                :title    (if dark-mode "Light mode" "Dark mode")
                :class    "border border-DEFAULT text-muted px-2 py-1 rounded-sm
                           hover:border-accent hover:text-accent transition-colors"}
       (if dark-mode "☀" "☾")]]]))
