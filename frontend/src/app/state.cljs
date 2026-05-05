(ns app.state
  (:require [re-frame.core :as rf]))

(def themes ["theme-starfleet" "theme-cyberpunk" "theme-minimal"])

(defn- stored [k default]
  (or (js/localStorage.getItem k) default))

(def default-db
  {:user         nil
   :posts        []
   :current-post nil
   :comments     []
   :loading      false
   :error        nil
   :theme        (stored "theme" "theme-starfleet")
   :dark-mode    (= (stored "dark-mode" "false") "true")})

;; ── DB init ──────────────────────────────────────────────────

(rf/reg-event-db ::initialize-db
  (fn [_ _] default-db))

;; ── Generic ──────────────────────────────────────────────────

(rf/reg-event-db ::set-loading
  (fn [db [_ v]] (assoc db :loading v)))

(rf/reg-event-db ::set-error
  (fn [db [_ e]] (assoc db :error e)))

;; ── User ─────────────────────────────────────────────────────

(rf/reg-event-db ::set-user
  (fn [db [_ user]] (assoc db :user user)))

(rf/reg-event-db ::logout
  (fn [db _] (assoc db :user nil)))

;; ── Content ──────────────────────────────────────────────────

(rf/reg-event-db ::set-posts
  (fn [db [_ posts]] (assoc db :posts posts)))

(rf/reg-event-db ::set-current-post
  (fn [db [_ post]] (assoc db :current-post post)))

;; ── Comments ─────────────────────────────────────────────────

(rf/reg-event-db ::set-comments
  (fn [db [_ comments]] (assoc db :comments comments)))

;; ── Theme ────────────────────────────────────────────────────

(defn- apply-theme! [theme dark?]
  (set! (.-className js/document.documentElement)
        (str theme (when dark? " dark")))
  (js/localStorage.setItem "theme" theme)
  (js/localStorage.setItem "dark-mode" (str dark?)))

(rf/reg-event-db ::set-theme
  (fn [db [_ theme]]
    (apply-theme! theme (:dark-mode db))
    (assoc db :theme theme)))

(rf/reg-event-db ::toggle-dark
  (fn [db _]
    (let [dark? (not (:dark-mode db))]
      (apply-theme! (:theme db) dark?)
      (assoc db :dark-mode dark?))))

;; ── Subscriptions ────────────────────────────────────────────

(rf/reg-sub ::user         (fn [db _] (:user db)))
(rf/reg-sub ::posts        (fn [db _] (:posts db)))
(rf/reg-sub ::current-post (fn [db _] (:current-post db)))
(rf/reg-sub ::comments     (fn [db _] (:comments db)))
(rf/reg-sub ::loading      (fn [db _] (:loading db)))
(rf/reg-sub ::error        (fn [db _] (:error db)))
(rf/reg-sub ::theme        (fn [db _] (:theme db)))
(rf/reg-sub ::dark-mode    (fn [db _] (:dark-mode db)))
