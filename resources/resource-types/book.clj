{
 :rdf-type "http://data.deichman.no/format/Book"
 :index-type "book"
 :label "Book"
 :desc "a physical or digital manifestation of a literary work."
 :groups [
          {:title "Identificators and relations"
           :elements [
                      {:id :isbn
                       :label "ISBN"
                       :required false
                       :repeatable false
                       :value-template {:value "" :predicate "bibo:isbn" :type :string}}
                      {:id :sameas
                       :label "Same as"
                       :desc "URI describing the same book."
                       :required false
                       :repeatable true
                       :value-template {:value "" :type :string :predicate "owl:sameAs"}}
                      {:id :work
                       :label "Work"
                       :desc "The work which this book is a manifestation of."
                       :required true
                       :repeatable false
                       :value-template {:value "" :type :uri :predicate "fabio:isManifestationOf"}
                       :create-new {:label "Work" :template "work.edn"}}]}
          {:title "Publication data"
           :elements [
                      {:id :title
                       :label "Title"
                       :required true
                       :repeatable false
                       :value-template {:value "" :type :string :predicate "dc:title"}}
                      {:id :sub-title
                       :label "Subtitle"
                       :required false
                       :repeatable false
                       :value-template {:value "" :type :string :predicate "svamp:subtitle"}}
                      {:id :publisher
                       :label "Publisher"
                       :required false
                       :repeatable false
                       :value-template {:value "" :type :uri :predicate "dc:publisher"}}
                      {:id :pub-year
                       :label "Publication year"
                       :required false
                       :repeatable false
                       :value-template {:value "" :type :integer :predicate "dc:date"}}]}
          {:title "Creator(s)"
           :elements [
                      {:id :creator
                       :label "Creator"
                       :desc "The author and other people who contributed to the book."
                       :required false
                       :repeatable true
                       :predicates [{:label "author"
                                     :prediacte "dc:creator"}
                                    {:label "translator"
                                     :predicate "dc:translator"}
                                    {:label "illustrator"
                                     :predicate "dc:illustrator"}
                                    {:label "editor"
                                     :predicate "dc:editor"}
                                    {:label "contributor"
                                     :predicate "dc:contributor"}]
                       :value-template {:value "" :type :multi-uri :predicate "dc:creator"}}]}
          {:title "Classification"
           :elements [
                      {:id :dewey
                       :label "Dewey"
                       :desc "Call number in the Dewey decimal system."
                       :required false
                       :repeatable true
                       :value-template {:value "" :type :uri :predicate "deich:dewey"}}
                      {:id :subject
                       :label "Subject"
                       :required false
                       :repeatable true
                       :value-template {:value "" :type :uri :predicate "dc:subject"}}]}
          {:title "Content"
           :elements [
                      {:id :desc
                       :label "Description"
                       :required false
                       :repeatable true
                       :value-template {:value "" :type :string :predicate "dc:description"}}
                      {:id :abstract
                       :label "Abstract"
                       :desc "Short summary of the contents."
                       :required false
                       :repeatable true
                       :value-template {:value "" :type :string :predicate "dc:abstract"}}
                      {:id :quote
                       :label "Quotation"
                       :required false
                       :repeatable true
                       :value-template {:value "" :type :string :predicate "svamp:quote"}}]}
          ]
 }
