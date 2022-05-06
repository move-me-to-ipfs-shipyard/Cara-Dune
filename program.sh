#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:Ripley Ripley.core/process \
    :main-ns Cara-Dune.main
}


main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -M -m Cara-Dune.main
}

jar(){

  clojure \
    -X:Zazu Zazu.core/process \
    :word '"Cara-Dune"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  rm -rf out/*.jar
  clojure \
    -X:Genie Genie.core/process \
    :main-ns Cara-Dune.main \
    :filename "\"out/Cara-Dune-$(git rev-parse --short HEAD).jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

ui(){
  # watch release
  npm i --no-package-lock
  mkdir -p out/resources/ui/
  cp src/Cara_Dune/index.html out/resources/ui/index.html
  clj -A:Moana:main:ui -M -m shadow.cljs.devtools.cli $1 ui main
}

ui-repl(){
  npm i --no-package-lock
  mkdir -p out/resources/ui/
  cp src/Cara_Dune/index.html out/resources/ui/index.html
  clj -A:Moana:main:ui -M -m shadow.cljs.devtools.cli clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

"$@"