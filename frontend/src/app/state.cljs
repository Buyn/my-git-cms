(ns app.state
  (:require [re-frame.core :as rf]))

(def default-db
  {:user nil
   :posts []
   :current-post nil
   :comments []
   :loading false
   :error nil})

(rf/reg-event-db ::initialize-db
  (fn [_ _] default-db))

(rf/reg-event-db ::set-loading
  (fn [db [_ v]] (assoc db :loading v)))

(rf/reg-event-db ::set-error
  (fn [db [_ e]] (assoc db :error e)))

(rf/reg-event-db ::set-user
  (fn [db [_ user]] (assoc db :user user)))

(rf/reg-event-db ::logout
  (fn [db _] (assoc db :user nil)))

(rf/reg-event-db ::set-posts
  (fn [db [_ posts]] (assoc db :posts posts)))

(rf/reg-event-db ::set-current-post
  (fn [db [_ post]] (assoc db :current-post post)))

(rf/reg-event-db ::set-comments
  (fn [db [_ comments]] (assoc db :comments comments)))

(rf/reg-sub ::user (fn [db _] (:user db)))
(rf/reg-sub ::posts (fn [db _] (:posts db)))
(rf/reg-sub ::current-post (fn [db _] (:current-post db)))
(rf/reg-sub ::comments (fn [db _] (:comments db)))
(rf/reg-sub ::loading (fn [db _] (:loading db)))
(rf/reg-sub ::error (fn [db _] (:error db)))
