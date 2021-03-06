(ns knakk.svamp.index
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [immutant.messaging :as msg]
            [knakk.svamp.settings :refer [settings]]
            [knakk.svamp.resources :as resources]
            [knakk.svamp.sparql :as sparql]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.index :as esi]
            [clojurewerkz.elastisch.native.document :as esd]))


;; Data  ======================================================================

(def queue "/queue/indexing")

(def indexes ["drafts" "public"])

(defn index-settings []
  (->> "indexing.edn" io/resource slurp edn/read-string))

(def common-mappings
  {"<svamp://internal/resource/created>" {"created" {:type "date" :index "not_analyzed"}}
   "<svamp://internal/resource/published>" {"published" {:type "date" :index "not_analyzed"}}
   "<svamp://internal/resource/updated>" {"updated" {:type "date" :index "not_analyzed"}}
   "<svamp://internal/resource/searchLabel>" {"searchLabel" {:type "string"}}
   "<svamp://internal/resource/displayLabel>" {"displayLabel" {:type "string" :index "not_analyzed"}}})


;; Private functions ==========================================================


(defn- mapping-from-file [file]
  (let [r (->> (str "resource-types/" file) io/resource slurp read-string)]
    {(:index-type r) {:properties
     (into {}
       (for [[_ prop] (merge common-mappings (:index-mappings r))]
         prop))}}))

(defn create-mappings
  ;; TODO move this elsewhere and remove import knakk.svamp.resources
  "Automatically create mappings for Elasticsearch from the rdf type definition files."
  []
  (into {}
   (map
     #(mapping-from-file (:file %))
     (resources/all-types))))

;; Public API =================================================================

(defn create!
  "Create all given indexes using the settings from indexing.edn. The :mappings
  are automatically inferred from the rdf type definitions.

  Returns {:error [<errormessages>], :result [<successmessages>]}"
  [indexes]
  (let [res (atom {:results [] :errors []})]
    (doseq [idx indexes]
      (try
        (esi/create idx :settings (index-settings) :mappings (create-mappings))
        (swap! res assoc :results (conj (:results @res) (str "Index added: " idx)))
        (catch Exception e
          (swap! res assoc :errors (conj (:errors @res) (.toString e))))))
    @res))

(defn delete!
  "Delete all given indexes.

  Returns {:error [<errormessages>], :result [<successmessages>]}"
  [indexes]
  (let [res (atom {:results [] :errors []})]
    (doseq [idx indexes]
      (try
        (esi/delete idx)
        (swap! res assoc :results (conj (:results @res) (str "Index deleted: " idx)))
        (catch Exception e
          (swap! res assoc :errors (conj (:errors @res) (.toString e))))))
    @res))

(defn reset-all!
  "Deletes, then re-creates given indexes.

  Returns {:errosr [<errormessages>], :results [<successmessages>]"
  [indexes]
  (let [deleted (delete! indexes)
        created (create! indexes)]
    (merge-with into deleted created)))

(defn index-all!
  "Enqueues all resources of given type to be indexed."
  [resource-file]
  (let [uris (resources/all-by-type resource-file)]
    (msg/with-connection {}
      (doseq [u uris]
        (msg/publish queue u)))
    {:errors [] :results [(str "put " (count uris) " of type " resource-file " to indexing queue")]}))

(defn update-mapping!
  "Updates the mapping for a given ressource-type.

  Returns {:errors [<errormessages>], :results [<successmessages>]"
  [resource-file]
  ;; TODO error handling; missing file or syntax errors in file, use some->> ?
  (let [res (atom {:results [] :errors []})
        resource-type (->> (str "resource-types/" resource-file) io/resource slurp read-string :index-type)]
    (doseq [idx indexes]
      (try
        (esi/update-mapping idx resource-type :mapping (mapping-from-file resource-file))
        (swap! res assoc :results (conj (:results @res) (str "Updated mapping for index: " idx ", type: " resource-type)))
        (catch Exception e
          (swap! res assoc :errors (conj (:errors @res) (.toString e))))))
    @res))

(defn update-mapping-and-reindex!
  "Updates mapping and queues all resources of type to be indexed.

  Returns {:errors [<errormessages>], :results [<successmessages>]"
  [resource-file]
  (let [u (update-mapping! resource-file)
        i (index-all! resource-file)]
    (merge-with into u i)))

(defn uri [s] (str "<" s ">"))

(defn indexable-resource ; WIP
  "Takes a resource URI and returns a map reay to be indexed.

  Returns an empty map if resource is missing or invalid." ;; TODO should it? now throws
  [resource]
  (let [values (->>
             (sparql/select-resource resource)
             :results
             sparql/solutions
             (map #(vector (:p %) (:o %)))
             (reduce
              (fn [m [k v]]
                (assoc m k (conj (m k []) v)))
              {}))
        template (first (values "svamp://internal/resource/template")) ;; TODO what if nil?
        template-file (->> (str "resource-types/" template) io/resource slurp load-string)
        pred-id (->> template-file :index-mappings (into common-mappings))
        ]
    (into {:type (:index-type template-file)}
           (for [[k v] values
             :when (get pred-id (uri k))
             :let [id (first (first (get pred-id (uri k))))]]
           [id (if (> (count v) 1) v (first v))]))))


(defn index-resource! [resource]
  (let [doc (indexable-resource resource)
        t (:type doc)
        published? (get doc "published")
        index (if published? "public" "drafts")]
    (try
      (esd/put index t resource (dissoc doc :type))
      (catch Exception e
        (println (.toString e)))))) ;; TODO logg or somthing?

(comment
  (indexable-resource "http://dewey.info/class/700")
  (index-resource! "http://dewey.info/class/700")
  (some->> (str "resource-types/" "dewey.clj<") io/resource slurp read-string :index-type)

  (update-mapping! "dewey.clj")

  (es/connect! [["127.0.0.1" 9300]] {"cluster.name" "svamp"})
  (reset-all! ["drafts" "public"])

  (delete ["draft" "fukks" "fisk"])
  (create ["public" "drafts"])
)

