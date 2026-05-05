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
    [:div {:class "max-w-3xl mx-auto px-6 py-8"}
     [:h1 {:class "text-xl font-bold text-accent mb-6 border-b border-DEFAULT pb-2"}
      "Profile"]
     (if user
       [:div {:class "flex flex-col gap-2 text-sm"}
        [:p [:span {:class "text-muted"} "Username: "] (:username user)]
        [:p [:span {:class "text-muted"} "Email: "]    (:email user)]
        [:p [:span {:class "text-muted"} "Role: "]     (:role user)]]
       [:p {:class "text-muted"} "Not logged in."])]))

(def routes
  [["/"           {:name ::home    :view home}]
   ["/post/:path" {:name ::post    :view post}]
   ["/edit/:path" {:name ::editor  :view editor}]
   ["/profile"    {:name ::profile :view profile}]])

(defonce match (r/atom nil))

(defn app []
  (let [error @(rf/subscribe [::s/error])]
    [:div {:class "min-h-screen bg-base text-primary font-mono"}
     [nav]
     (when error
       [:div {:class "bg-[rgba(255,51,102,0.08)] border-b border-danger
                      text-danger text-sm text-center px-8 py-2"}
        error])
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
