(ns app.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdomc]
            [re-frame.core :as rf]
            [reitit.frontend :as rfe]
            [reitit.frontend.easy :as rfee]
            [app.state :as s]
            [app.api :as api]
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
        [:p [:strong "Username: "] (:username user)]
        [:p [:strong "Email: "]    (:email user)]
        [:p [:strong "Role: "]     (:role user)]]
       [:p "Not logged in."])]))

(def routes
  [["/"           {:name ::home    :view home}]
   ["/post/:path" {:name ::post    :view post}]
   ["/edit/:path" {:name ::editor  :view editor}]
   ["/profile"    {:name ::profile :view profile}]])

(defonce match (r/atom nil))

(defn app []
  (let [error @(rf/subscribe [::s/error])]
    [:div.app
     [nav]
     (when error [:div.error-banner error])
     (when-let [m @match]
       [(get-in m [:data :view]) m])]))

(defonce root (atom nil))

(defn init []
  (rf/dispatch-sync [::s/initialize-db])
  (rf/dispatch [::api/load-user])
  (rfee/start!
   (rfe/router routes)
   (fn [m] (reset! match m))
   {:use-fragment false})
  (reset! root (rdomc/create-root (.getElementById js/document "app")))
  (rdomc/render @root [app]))
