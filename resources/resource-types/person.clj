{
 :rdf-type "http://data.deichman.no/format/Person"
 :index-type "person"
 :label "Person"
 :desc "a human being, living or dead."
 :groups [
          {:title "Personalia"
           :elements [
                      {:id :name
                       :label "Real name"
                       :required true
                       :repeatable false
                       :value-template {:value "" :predicate "foaf:name" :type :string}}
                      {:id :nickname
                       :label "Nickname"
                       :desc "Also known as..."
                       :required false
                       :repeatable true
                       :value-template {:value "" :predicate "svamp:nick" :type :string}}
                      {:id :birth-place
                       :label "Birthplace"
                       :required false
                       :repeatable false
                       :value-template {:value "" :predicate "svamp:place" :type :uri}
                       :create-new {:label "Place" :template "place.edn"}}
                      {:id :birth-year
                       :label "Year of birth"
                       :required false
                       :repeatable false
                       :value-template {:value "" :predicate "svamp:birthYear" :type :integer}}
                      {:id :death-year
                       :label "Year of Death"
                       :required false
                       :repeatable false
                       :value-template {:value "" :predicate "svamp:birthYear" :type :integer}}]}]
 }
