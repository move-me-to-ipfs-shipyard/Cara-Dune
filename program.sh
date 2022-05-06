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

jar(){

  clojure \
    -X:identicon Zazu.core/process \
    :word '"Cara-Dune"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  rm -rf out/*.jar
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns Cara-Dune.main \
    :filename "\"out/Cara-Dune-$(git rev-parse --short HEAD).jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

"$@"