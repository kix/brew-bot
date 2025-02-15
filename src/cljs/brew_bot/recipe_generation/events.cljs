(ns brew-bot.recipe-generation.events
  (:require [brew-bot.db :as db]
            [brew-bot.recipe-generation.generators :as generators]
            [brew-bot.recipe-generation.weights :as weights]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :update-current-recipe
 (fn [db [_ path val]]
   (-> db
     (update :current-recipe #(assoc-in % path val))
     (assoc-in [:current-recipe :has-recipe-changed?] true))))

(rf/reg-event-db
 :toggle-ingredient-selection
 (fn [db [_ ingredient-type ingredient-key]]
   (let [is-included? (get-in db [:current-recipe ingredient-type :probabilities ingredient-key])]
     (if is-included?
       (update-in db [:current-recipe ingredient-type :probabilities] dissoc ingredient-key)
       (update-in db [:current-recipe ingredient-type :probabilities] assoc ingredient-key 5)))))

(rf/reg-event-fx
 :generate-recipe
 (fn [{db :db} [_ generator-type]]
   (let [source (:current-recipe db)
         recipe (generators/generate-beer-recipe generator-type (:gallons source) (:grains source) (:extracts source) (:hops source) (:yeasts source))]
     {:db (-> db
              (assoc :generated-recipe recipe)
              (assoc :current-recipe db/empty-recipe))
      :dispatch [:update-current-page :recipe-preview]})))
