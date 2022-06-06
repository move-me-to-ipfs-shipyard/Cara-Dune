#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=8 \
    -X:Ripley Ripley.core/process \
    :main-ns Cara-Dune.main
}


main(){
  clojure \
    -J-Dclojure.core.async.pool-size=8 \
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

identicon(){
  clojure \
    -X:Zazu Zazu.core/process \
    :word '"Cara-Dune"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  convert out/identicon/icon.png -define icon:auto-resize=256,64,48,32,16 out/identicon/icon.ico
  png2icns out/identicon/icon.icns out/identicon/icon.png
}

jar(){
  OPERATING_SYSTEM=$1
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  clojure \
    -J-Dcljfx.skip-javafx-initialization=true \
    -X:Genie Genie.core/process \
    :main-ns Cara-Dune.main \
    :filename "\"out/Cara-Dune-$COMMIT_COUNT-$COMMIT_HASH-$OPERATING_SYSTEM.jar\"" \
    :paths '["src" "out/ui" "out/identicon"]' \
    :create-basis-opts "{:aliases [:$OPERATING_SYSTEM]}"
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
  identicon
  rm -rf out/*.jar
  for os in "linux" "windows" "macos"; do
    echo "i am assembling jar for $os"
    jar $os || break;
  done
}

"$@"