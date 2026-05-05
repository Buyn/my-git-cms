(ns app.api
  (:require [re-frame.core :as rf]
            [app.state :as s]))

(def base-url "http://localhost:8000")

(defn fetch! [method path opts]
  (let [{:keys [body on-success on-error]} opts]
    (-> (js/fetch (str base-url path)
                  (clj->js (cond-> {:method      method
                                    :credentials "include"
                                    :headers     {"Content-Type" "application/json"}}
                             body (assoc :body (js/JSON.stringify (clj->js body))))))
        (.then (fn [r]
                 (if (.-ok r)
                   (.json r)
                   (throw (js/Error. (.-status r))))))
        (.then #(when on-success (on-success (js->clj % :keywordize-keys true))))
        (.catch #(when on-error (on-error (.-message %)))))))

(rf/reg-fx :effect/fetch
  (fn [{:keys [method path body on-success on-error]}]
    (fetch! method path {:body body :on-success on-success :on-error on-error})))

;; ── Auth ─────────────────────────────────────────────────────

(rf/reg-event-fx ::load-user
  (fn [_ _]
    {:fx [[:effect/fetch {:method     "GET"
                          :path       "/auth/me"
                          :on-success #(rf/dispatch [::s/set-user %])
                          :on-error   (fn [_])}]]}))

(rf/reg-event-fx ::logout
  (fn [_ _]
    {:fx [[:effect/fetch {:method     "POST"
                          :path       "/auth/logout"
                          :on-success #(rf/dispatch [::s/logout])
                          :on-error   #(rf/dispatch [::s/logout])}]]}))

;; ── Content ──────────────────────────────────────────────────

(rf/reg-event-fx ::load-posts
  (fn [_ _]
    {:fx [[:effect/fetch {:method     "GET"
                          :path       "/content/list"
                          :on-success #(rf/dispatch [::s/set-posts %])
                          :on-error   #(rf/dispatch [::s/set-error %])}]]}))

(rf/reg-event-fx ::load-post
  (fn [_ [_ path]]
    {:fx [[:effect/fetch {:method     "GET"
                          :path       (str "/content/" path)
                          :on-success #(rf/dispatch [::s/set-current-post
                                                     {:path path
                                                      :html (:html %)
                                                      :raw  (:raw %)}])
                          :on-error   #(rf/dispatch [::s/set-error %])}]]}))

(rf/reg-event-fx ::save-post
  (fn [_ [_ path content message]]
    {:fx [[:effect/fetch {:method     "PUT"
                          :path       (str "/content/" path)
                          :body       {:content content
                                       :message (or message (str "Update " path))}
                          :on-success #(rf/dispatch [::load-post path])
                          :on-error   #(rf/dispatch [::s/set-error %])}]]}))

;; ── Comments ─────────────────────────────────────────────────

(rf/reg-event-fx ::load-comments
  (fn [_ [_ page-path]]
    {:fx [[:effect/fetch {:method     "GET"
                          :path       (str "/comments/" (js/encodeURIComponent page-path))
                          :on-success #(rf/dispatch [::s/set-comments %])
                          :on-error   #(rf/dispatch [::s/set-error %])}]]}))

(rf/reg-event-fx ::post-comment
  (fn [_ [_ page-path text email]]
    {:fx [[:effect/fetch {:method     "POST"
                          :path       "/comments"
                          :body       {:page_path page-path :content text :anon_email email}
                          :on-success #(rf/dispatch [::load-comments page-path])
                          :on-error   #(rf/dispatch [::s/set-error %])}]]}))

(rf/reg-event-fx ::delete-comment
  (fn [_ [_ id page-path]]
    {:fx [[:effect/fetch {:method     "DELETE"
                          :path       (str "/comments/" id)
                          :on-success #(rf/dispatch [::load-comments page-path])
                          :on-error   #(rf/dispatch [::s/set-error %])}]]}))

(rf/reg-event-fx ::add-reaction
  (fn [_ [_ comment-id type]]
    {:fx [[:effect/fetch {:method   "POST"
                          :path     (str "/comments/" comment-id "/reaction")
                          :body     {:type type}
                          :on-error #(rf/dispatch [::s/set-error %])}]]}))
