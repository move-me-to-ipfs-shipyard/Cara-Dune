#!/bin/bash


install(){
  npm i --no-package-lock
  mkdir -p out/jar/ui/
  mkdir -p out/jar/src/Cara-Dune/
  cp src/Cara_Dune/index.html out/jar/ui/index.html
  cp src/Cara_Dune/style.css out/jar/ui/style.css
  cp package.json out/jar/package.json
}

shadow(){
  clj -A:shadow:main:ui -M -m shadow.cljs.devtools.cli "$@"
}

repl(){
  install
  shadow clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

jar(){

  rm -rf out

  clojure \
    -X:Zazu Zazu.core/process \
    :word '"Cara-Dune"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  install
  cp out/identicon/icon.png out/jar/icon.png
  shadow release :main :ui
  # COMMIT_HASH=$(git rev-parse --short HEAD)
  # COMMIT_COUNT=$(git rev-list --count HEAD)
  # echo Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.zip
  # cd out/jar
  # zip -r ../Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.zip ./ && \
  # cd ../../
}

release(){
  jar
}


tag(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  TAG="$COMMIT_COUNT-$COMMIT_HASH"
  git tag $TAG $COMMIT_HASH
  echo $COMMIT_HASH
  echo $TAG
}

"$@"