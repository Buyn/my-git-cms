(ns app.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [reitit.frontend :as rfe]
            [reitit.frontend.easy :as rfee]
            [app.state :as s]
            [app.views.nav :refer [nav]]
            [app.views.home :refer [home]]
            [app.views.post :refer [post]]
            [app.views.editor :refer [editor]]))

(defn profile [_]
  (let [user @(rf/subscribe [::s/user])]
    [:div.page
     [:h1.page-title "Profile"]
     (if user
       [:div
        [:p [:strong "Email: "] (:email user)]
        [:p [:strong "Role: "] (:role user)]]
       [:p "Not logged in."])]))

(def routes
  [["/" {:name ::home :view home}]
   ["/post/:path" {:name ::post :view post}]
   ["/edit/:path" {:name ::editor :view editor}]
   ["/profile" {:name ::profile :view profile}]])

(defonce match (atom nil))

(defn app []
  (let [error @(rf/subscribe [::s/error])]
    [:div.app
     [nav]
     (when error [:div.error-banner error])
     (when-let [m @match]
       [(get-in m [:data :view]) m])]))

(defn init []
  (rf/dispatch-sync [::s/initialize-db])
  (rfee/start!
   (rfe/router routes)
   (fn [m] (reset! match m))
   {:use-fragment false})
  (rdom/render [app] (.getElementById js/document "app")))
