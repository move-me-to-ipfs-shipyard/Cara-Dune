#!/bin/bash

repl(){
  npm i --no-package-lock
  mkdir -p out/jar/ui/
  cp src/Cara_Dune/index.html out/jar/ui/index.html
  cp package.json out/jar/package.json
  clj -A:Moana:main:ui -M -m shadow.cljs.devtools.cli clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

shadow(){
  # watch release
  npm i --no-package-lock
  mkdir -p out/jar/ui/
  cp src/Cara_Dune/index.html out/jar/ui/index.html
  cp package.json out/jar/package.json
  clj -A:Moana:main:ui -M -m shadow.cljs.devtools.cli $1 ui main
}

jar(){
  rm -rf out
  shadow release
  COMMIT_HASH=$(git rev-parse --short HEAD)
  cd out/jar
  zip -r ../Cara-Dune-$COMMIT_HASH.zip ./ && \
  cd ../../
}

release(){
  jar
}


"$@"