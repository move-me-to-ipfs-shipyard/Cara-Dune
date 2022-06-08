#!/bin/bash


install(){
  npm i --no-package-lock
}

copy(){
  mkdir -p out/jar/ui/
  mkdir -p out/jar/src/Cara-Dune/
  cp src/Cara_Dune/index.html out/jar/ui/index.html
  cp src/Cara_Dune/style.css out/jar/ui/style.css
  cp package.json out/jar/package.json

  convert out/identicon/icon.png -define icon:auto-resize=256,64,48,32,16 out/identicon/icon.ico
  png2icns out/identicon/icon.icns out/identicon/icon.png
  cp out/identicon/icon.png out/jar/icon.png
  cp out/identicon/icon.icns out/jar/icon.icns
  cp out/identicon/icon.ico out/jar/icon.ico
}

shadow(){
  clj -A:shadow:main:ui -M -m shadow.cljs.devtools.cli "$@"
}

repl(){
  install
  copy
  shadow clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

identicon(){
  clojure \
    -X:Zazu Zazu.core/process \
    :word '"Cara-Dune"' \
    :filename '"out/identicon/icon.png"' \
    :size 256
}

compile(){
  shadow release :ui :main
}

jar(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  echo Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.zip
  cd out/jar
  zip -r ../Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH.zip ./ && \
  cd ../../
}

release(){
  rm -rf out
  install
  identicon
  copy
  compile
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