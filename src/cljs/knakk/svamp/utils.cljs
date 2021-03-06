(ns knakk.svamp.utils
  (:require [cljs.reader :as reader]
            [goog.events :as events])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events.EventType]))

(def ^:private meths
  {:get "GET"
   :put "PUT"
   :post "POST"
   :delete "DELETE"})

(defn edn-xhr
  ;; TODO handle on-error as well, check om-sync
  [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
      (fn [e]
        (on-complete (reader/read-string (.getResponseText xhr)))))
    (. xhr
       (send url (meths method) (when data (pr-str data))
             #js {"Content-Type" "application/edn"}))))

(defn display [show]
  "Helper: hide a dom element when show is true."
  (if show
    #js {}
    #js {:display "none"}))

#_(defn spinner []
  (om/component
    (dom/div #js {:className "spinner"}
      (dom/div #js {:className "bounce1"})
      (dom/div #js {:className "bounce2"})
      (dom/div #js {:className "bounce3"}))))
