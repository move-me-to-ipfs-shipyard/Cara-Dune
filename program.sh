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

tag(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  TAG="$COMMIT_COUNT-$COMMIT_HASH"
  git tag $TAG $COMMIT_HASH
  echo $COMMIT_HASH
  echo $TAG
}

jar(){

  rm -rf out/*.jar
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  clojure \
    -X:Genie Genie.core/process \
    :main-ns Cara-Dune.main \
    :filename "\"out/Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.jar\"" \
    :paths '["src" "out/ui" "data"]'
}

shadow(){
  clj -A:shadow:ui -M -m shadow.cljs.devtools.cli "$@"
}

ui_install(){
  npm i --no-package-lock
  mkdir -p out/ui/
  cp src/Cara_Dune/index.html out/ui/index.html
  cp src/Cara_Dune/style.css out/ui/style.css
}

ui_repl(){
  ui_install
  shadow clj-repl
  # (shadow/watch :ui)
  # (shadow/repl :ui)
  # :repl/quit
}

ui_release(){
  ui_install
  shadow release :ui
}

release(){
  rm -rf out
  ui_release
  jar
}

"$@"