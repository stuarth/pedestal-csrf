(ns pedestal-csrf.service
  (:require [io.pedestal.http :as bootstrap]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [io.pedestal.http.csrf :as csrf] 
            [ring.util.response :as ring-resp]
            [hiccup.core :as hiccup]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn csrf-form
  [request]
  (ring-resp/response
   (hiccup/html
    [:body
     [:form {:action "/" :method "POST"}
      [:input {:name "__anti-forgery-token"
               :value (::csrf/anti-forgery-token request)
               :type "hidden"}]
      [:button {:type "submit"} "Submit"]]])))

(defn csrf-action
  [request]
  (ring-resp/response
   (hiccup/html
    [:body
     [:h1 "Form successfully submitted!"]])))

(defroutes routes
  [[["/"
     ^:interceptors [(body-params/body-params)
                     bootstrap/html-body
                     (csrf/anti-forgery)]
     {:get csrf-form
      :post csrf-action}]]])

;; Consumed by pedestal-csrf.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes
              ::bootstrap/enable-session {} 

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::bootstrap/type :jetty
              ;;::bootstrap/host "localhost"
              ::bootstrap/port 8020})

