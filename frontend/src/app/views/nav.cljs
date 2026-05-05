(ns app.views.nav
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as rfee]
            [app.state :as s]
            [app.api :as api]))

(defn nav []
  (let [user      @(rf/subscribe [::s/user])
        theme     @(rf/subscribe [::s/theme])
        dark-mode @(rf/subscribe [::s/dark-mode])]
    [:nav.nav
     [:a.nav-brand {:href (rfee/href :app.core/home)} "⬡ GIT CMS"]
     [:div.nav-links
      [:a {:href (rfee/href :app.core/home)} "Home"]
      (if user
        [:<>
         [:span.nav-user (:username user)]
         [:a {:href (rfee/href :app.core/profile)} "Profile"]
         [:button.btn-ghost {:on-click #(rf/dispatch [::api/logout])} "Logout"]]
        [:a.btn-primary {:href (str js/window.location.origin "/auth/login")} "Login"])
      [:div.theme-controls
       [:select.theme-select
        {:value     theme
         :on-change #(rf/dispatch [::s/set-theme (.. % -target -value)])}
        (for [t s/themes]
          ^{:key t}
          [:option {:value t} (subs t 6)])]  ; strip "theme-" prefix
       [:button.theme-toggle
        {:on-click #(rf/dispatch [::s/toggle-dark])
         :title    (if dark-mode "Switch to light" "Switch to dark")}
        (if dark-mode "☀" "☾")]]]]))
