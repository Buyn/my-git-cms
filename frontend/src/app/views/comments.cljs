(ns app.views.comments
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [app.state :as s]
            [app.api :as api]))

(defn comment-form [page-path]
  (let [text (r/atom "")
        email (r/atom "")]
    (fn []
      [:form.comment-form
       {:on-submit (fn [e]
                     (.preventDefault e)
                     (when (seq @text)
                       (rf/dispatch [::api/post-comment page-path @text @email])
                       (reset! text "")
                       (reset! email "")))}
       [:input {:type "email" :placeholder "Email (optional)"
                :value @email :on-change #(reset! email (.. % -target -value))}]
       [:textarea {:placeholder "Write a comment..."
                   :value @text :on-change #(reset! text (.. % -target -value))}]
       [:button.btn-primary {:type "submit"} "Post"]])))

(defn reaction-bar [comment-id]
  [:div.reactions
   (for [emoji ["👍" "❤️" "🚀" "👀"]]
     ^{:key emoji}
     [:button.reaction {:on-click #(rf/dispatch [::api/add-reaction comment-id emoji])}
      emoji])])

(defn comments-section [page-path]
  (r/create-class
   {:component-did-mount #(rf/dispatch [::api/load-comments page-path])
    :reagent-render
    (fn [page-path]
      (let [comments @(rf/subscribe [::s/comments])
            user @(rf/subscribe [::s/user])]
        [:section.comments
         [:h3 "Comments"]
         [comment-form page-path]
         [:div.comment-list
          (for [c comments]
            ^{:key (:id c)}
            [:div.comment-card
             [:div.comment-meta
              [:span.comment-author (:email c)]
              [:span.comment-date (:created_at c)]
              (when (or (= (:role user) "admin") (= (:email user) (:email c)))
                [:button.btn-danger {:on-click #(rf/dispatch [::api/delete-comment (:id c) page-path])} "×"])]
             [:p (:text c)]
             [reaction-bar (:id c)]])]]))}))
