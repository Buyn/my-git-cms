(ns app.views.nav
  (:require [re-frame.core :as rf]
            [app.state :as s]))

(defn nav []
  (let [user @(rf/subscribe [::s/user])]
    [:nav.nav
     [:a.nav-brand {:href "/"} "⬡ GIT CMS"]
     [:div.nav-links
      [:a {:href "/"} "Home"]
      (if user
        [:<>
         [:span.nav-user (:email user)]
         [:a {:href "/profile"} "Profile"]
         [:button.btn-ghost {:on-click #(rf/dispatch [::s/logout])} "Logout"]]
        [:a.btn-primary {:href "http://localhost:8000/auth/login"} "Login"])]]))
