(ns app.views.comments
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [app.state :as s]
            [app.api :as api]))

(def input-cls
  "bg-surface border border-DEFAULT rounded-sm text-primary font-mono text-sm
   px-3 py-2 outline-none focus:border-accent transition-colors")

(defn comment-form [page-path]
  (let [text  (r/atom "")
        email (r/atom "")]
    (fn []
      [:form {:class    "flex flex-col gap-2 mb-6"
              :on-submit (fn [e]
                           (.preventDefault e)
                           (when (seq @text)
                             (rf/dispatch [::api/post-comment page-path @text @email])
                             (reset! text "")
                             (reset! email "")))}
       [:input {:type        "email"
                :placeholder "Email (anonymous)"
                :value       @email
                :on-change   #(reset! email (.. % -target -value))
                :class       input-cls}]
       [:textarea {:placeholder "Write a comment..."
                   :value       @text
                   :on-change   #(reset! text (.. % -target -value))
                   :class       (str input-cls " min-h-[80px] resize-y")}]
       [:button {:type  "submit"
                 :class "self-start border border-accent text-accent px-4 py-1 rounded-sm
                         text-sm hover:bg-[var(--accent-dim)] transition-colors"}
        "Post"]])))

(defn reaction-bar [comment-id]
  [:div {:class "flex gap-1 mt-2"}
   (for [emoji ["👍" "❤️" "🚀" "👀"]]
     ^{:key emoji}
     [:button {:on-click #(rf/dispatch [::api/add-reaction comment-id emoji])
               :class    "bg-raised border border-DEFAULT rounded-sm px-2 py-0.5 text-sm
                          hover:border-accent transition-colors cursor-pointer"}
      emoji])])

(defn comments-section [page-path]
  (r/create-class
   {:component-did-mount #(rf/dispatch [::api/load-comments page-path])
    :reagent-render
    (fn [page-path]
      (let [comments @(rf/subscribe [::s/comments])
            user     @(rf/subscribe [::s/user])]
        [:section {:class "mt-8"}
         [:h3 {:class "text-accent text-sm font-bold uppercase tracking-widest mb-4"}
          "Comments"]
         [comment-form page-path]
         [:div {:class "flex flex-col gap-3"}
          (for [c comments]
            ^{:key (:id c)}
            [:div {:class "bg-surface border border-DEFAULT rounded-sm px-4 py-3"}
             [:div {:class "flex items-center gap-3 mb-1 text-xs"}
              [:span {:class "text-accent"}
               (or (:username c) (:anon_email c) "anonymous")]
              [:span {:class "text-muted flex-1"} (:created_at c)]
              (when (or (= (:role user) "admin")
                        (= (:id user) (:user_id c)))
                [:button {:on-click #(rf/dispatch [::api/delete-comment (:id c) page-path])
                          :class    "border border-danger text-danger px-2 py-0.5 rounded-sm
                                     hover:bg-[rgba(255,51,102,0.1)] transition-colors"}
                 "×"])]
             [:p {:class "text-primary text-sm"} (:content c)]
             [reaction-bar (:id c)]])]]))}))
