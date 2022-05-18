#!/bin/bash


install(){
  npm i --no-package-lock
  mkdir -p out/jar/ui/
  cp src/Cara_Dune/index.html out/jar/ui/index.html
  cp src/Cara_Dune/style.css out/jar/ui/style.css
  cp package.json out/jar/package.json
}

Moana(){
  clj -A:Moana:main:ui -M -m shadow.cljs.devtools.cli "$@"
}

repl(){
  install
  Moana clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

jar(){
  rm -rf out
  install
  Moana release :main :ui
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  echo Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.zip
  cd out/jar
  zip -r ../Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.zip ./ && \
  cd ../../
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