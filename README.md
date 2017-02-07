# pedestal.views

[![CircleCI](https://circleci.com/gh/cognitect-labs/pedestal.views.svg?style=svg)](https://circleci.com/gh/cognitect-labs/pedestal.views)

Server-side rendering interceptors.

## Quickstart

Add the following dependency to project.clj or build.boot:

    [com.cognitect/pedestal.views "0.1.0-SNAPSHOT"]

Require the namespace in your Pedestal service.clj (or wherever else
you define your routes.):

    (:require [com.cognitect.pedestal.views :as views])

Add the view rendering interceptor to the route or routes that need
it:

    (def common-interceptors [(body-params/body-params) http/html-body views/renderer])

    (def routes
      #{["/home"  :get (conj common-interceptors `home)]
        ["/about"   :get (conj common-interceptors `about)]})

Now you can separate view rendering from logic & data assembly.

    (defn about
      [request]
      {:view :sample.views/about
       :text "About"
       :body (clojure-version)
       :url  (route/url-for ::about)})

    (defn home
      [request]
      {:view :sample.views/home
       :text "Rendered by pedestal.views"
       :body "Hello, world!"
       :url  (route/url-for ::home)})

## Details

The interceptor `com.cognitect.pedestal.views/renderer` uses the
`:view` key to look up a rendering function. The value can be a
keyword for ease of use... the interceptor looks for a function with
the "same" name as the keyword. The value of the `:view` key can also
be a symbol or var, in which case lookup proceeds as you would
expect.

The interceptor passes the entire response map to the rendering
function. That means applications can use whatever keys and nesting
structure makes sense for the application.

Rendering functions are supplied by a variety of templating
libraries. We've tried this out with:

- [Enlive](https://github.com/cgrand/enlive)
- [Hiccup](https://github.com/weavejester/hiccup)
- [Stencil](https://github.com/davidsantiago/stencil)

Since they all meet at the level of "make me a function" they all work
nicely.

The interceptor should also work with any of the other packages listed
as [Template Languages](https://www.clojure-toolbox.com).

## License

Copyright 2017 Cognitect, Inc.

Distributed under the Eclipse Public License version 1.0.
