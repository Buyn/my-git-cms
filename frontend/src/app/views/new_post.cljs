(ns app.views.new-post
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [app.state :as s]
            [app.api :as api]))

(defn new-post [_]
  (let [slug    (r/atom "")
        content (r/atom "* Title\n\n")
        message (r/atom "Add new post")]
    (fn []
      (let [user @(rf/subscribe [::s/user])]
        (if (not= (:role user) "admin")
          [:div {:class "max-w-3xl mx-auto px-6 py-8"}
           [:p {:class "text-danger"} "Access denied."]]
          [:div {:class "max-w-3xl mx-auto px-6 py-8"}
           [:h1 {:class "text-xl font-bold text-accent mb-6 border-b border-DEFAULT pb-2"}
            "New Post"]
           [:div {:class "flex flex-col gap-3"}
            [:input {:type        "text"
                     :placeholder "filename.org"
                     :value       @slug
                     :on-change   #(reset! slug (.. % -target -value))
                     :class       "bg-surface border border-DEFAULT rounded-sm text-primary
                                   font-mono text-sm px-3 py-2 outline-none
                                   focus:border-accent transition-colors"}]
            [:textarea {:value     @content
                        :on-change #(reset! content (.. % -target -value))
                        :class     "w-full min-h-[50vh] bg-surface border border-DEFAULT
                                    rounded-sm text-primary font-mono text-sm p-4
                                    resize-y outline-none focus:border-accent transition-colors"}]
            [:input {:type        "text"
                     :placeholder "Commit message"
                     :value       @message
                     :on-change   #(reset! message (.. % -target -value))
                     :class       "bg-surface border border-DEFAULT rounded-sm text-primary
                                   font-mono text-sm px-3 py-2 outline-none
                                   focus:border-accent transition-colors"}]
            [:button {:on-click #(when (seq @slug)
                                   (rf/dispatch [::api/save-post @slug @content @message]))
                      :class    "self-start border border-accent text-accent px-4 py-1
                                 rounded-sm text-sm hover:bg-[var(--accent-dim)] transition-colors"}
             "Create"]]])))))
