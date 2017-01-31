# Pedestal.views sample app

This sample illustrates two styles of using pedestal.views.

First we have the ``pure function'' style. In this style the renderer
just expects to locate a function with the same name as the view
key. Look in `src/sample/service.clj` at the route for `/enlive`. It
maps to the "handler" `home-page-enlive`. That function attaches a
view key of `:sample.views/home-page-with-enlive`.

There is a function `sample.views/home-page-with-enlive` that will
then be used to render the view.

It just happens that Enlive's deftemplate macro creates a
function. Most Clojure templating languages ultimately just define
functions.

If you're using Stencil, a Mustache-style templating language, then
you can skip the extra functions and use a renderer that looks for
template files.

The route for `/stencil` illustrates that approach. Here the view key
is resolved to a '.mustache' file on the classpath. Try

    curl http://localhost:8080/stencil

This will render the view with the "normal.mustache" template.

    curl http://localhost:8080/stencil?error=very-yes

will render the view using the "abby-normal.mustache" template.
