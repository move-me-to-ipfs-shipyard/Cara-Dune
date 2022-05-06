#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:repl Ripley.core/process \
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
  git tag "$COMMIT_COUNT-$COMMIT_HASH" $COMMIT_HASH 
}

jar(){

  clojure \
    -X:identicon Zazu.core/process \
    :word '"Cara-Dune"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  rm -rf out/*.jar
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns Cara-Dune.main \
    :filename "\"out/Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

"$@"