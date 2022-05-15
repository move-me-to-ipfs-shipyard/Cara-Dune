(ns Cara-Dune.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.core.async.impl.protocols :refer [closed?]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]
   [clojure.pprint :as Wichita.pprint]
   [clojure.repl :as Wichita.repl]
   [cljfmt.core :as Joker.core]
   
   [aleph.http :as Simba.http]

   [Cara-Dune.drawing]
   [Cara-Dune.seed]
   [Cara-Dune.raisins]
   [Cara-Dune.peanuts]
   [Cara-Dune.kiwis]
   [Cara-Dune.microwaved-potatoes]
   [Cara-Dune.corn]
   [Cara-Dune.beans])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JTextPane)
   (javax.swing.border EmptyBorder)
   (java.awt Canvas Graphics Graphics2D Shape Color Polygon Dimension BasicStroke Toolkit Insets BorderLayout)
   (java.awt.event KeyListener KeyEvent MouseListener MouseEvent ActionListener ActionEvent ComponentListener ComponentEvent)
   (java.awt.geom Ellipse2D Ellipse2D$Double Point2D$Double)
   (com.formdev.flatlaf FlatLaf FlatLightLaf)
   (com.formdev.flatlaf.extras FlatUIDefaultsInspector FlatDesktop FlatDesktop$QuitResponse FlatSVGIcon)
   (com.formdev.flatlaf.util SystemInfo UIScale)
   (java.util.function Consumer)
   (java.util ServiceLoader)
   (org.kordamp.ikonli Ikon)
   (org.kordamp.ikonli IkonProvider)
   (org.kordamp.ikonli.swing FontIcon)
   (org.kordamp.ikonli.codicons Codicons)
   (net.miginfocom.swing MigLayout)
   (net.miginfocom.layout ConstraintParser LC UnitValue)
   (java.io File)
   (java.lang Runnable))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))
(defonce gamesA (atom nil))
(defonce gameA (atom nil))
(defonce settingsA (atom nil))

(defonce resize| (chan (sliding-buffer 1)))
(defonce eval| (chan 10))
(defonce cancel-sub| (chan 1))
(defonce cancel-pub| (chan 1))
(defonce canvas-draw| (chan (sliding-buffer 1)))
(defonce ops| (chan 10))
(defonce table| (chan (sliding-buffer 10)))
(defonce sub| (chan (sliding-buffer 10)))
(def ^:dynamic ^JFrame jframe nil)
(def ^:dynamic ^Canvas canvas nil)
(def ^:dynamic ^JTextArea jrepl nil)
(def ^:dynamic ^JTextArea joutput nil)
(def ^:dynamic ^JEditorPane jeditor nil)
(def ^:dynamic ^JScrollPane joutput-scroll nil)
(def ^:dynamic ^Graphics2D graphics nil)
(def ^:dynamic ^JPanel jcode-panel nil)
(def ^:dynamic ^JPanel jroot-panel nil)
(def ^:dynamic raw-stream-connection-pool nil)

#_(defonce *ns (find-ns 'Cara-Dune.main))

(def ^:const energy-per-move 100)
(def ^:const canvas-width 1600)
(def ^:const canvas-height 1600)
(def ^:const tile-size 32)
(def ^:const jframe-title "one X-Wing? great - we're saved")
(def ^:const grid-rows 16)
(def ^:const grid-cols 16)

(defn draw-word
  "draw Word"
  []
  (.drawString graphics "Word" (* 0.5 (.getWidth canvas)) (* 0.5 (.getHeight canvas))))

(defn draw-line
  "draw line"
  []
  (.drawLine graphics  (* 0.3 (.getWidth canvas)) (* 0.3 (.getHeight canvas)) (* 0.7 (.getWidth canvas)) (* 0.7 (.getHeight canvas))))

(defn draw-grid
  []
  (Cara-Dune.peanuts/draw-grid
   {:canvas canvas
    :graphics graphics
    :grid-rows grid-rows
    :grid-cols grid-cols}))

(defn draw-Cara
  [{:keys [row col]}]
  (SwingUtilities/invokeLater
   (reify Runnable
     (run [_]
       (let [row-height (/ (.getHeight canvas) grid-rows)
             col-width (/ (.getWidth canvas) grid-cols)
             x (* col col-width)
             y (* row row-height)
             shape (Ellipse2D$Double. x y (* 0.7 col-width) (* 0.7 row-height))]
         (.setPaint graphics Color/CYAN)
         (.fill graphics shape)
         (.setPaint graphics Color/BLACK))))))

(defn clear-canvas
  []
  (.clearRect graphics 0 0 (.getWidth canvas)  (.getHeight canvas))
  (.setPaint graphics (Color. 255 255 255 255) #_(Color. 237 211 175 200))
  (.fillRect graphics 0 0 (.getWidth canvas) (.getHeight canvas))
  (.setPaint graphics  Color/BLACK))

(defn force-resize
  []
  (let [width (.getWidth jframe)
        height (.getHeight jframe)]
    (.setSize jframe (Dimension. (+ 1 width) height))
    (.setSize jframe (Dimension. width height))))

(defn clear
  []
  (.setText joutput ""))

(defn transmit
  "evaluate code in spe-editor-bike"
  []
  (let [string (-> (.getText jeditor) (clojure.string/trim) (clojure.string/trim-newline))
        form (read-string string)]
    (put! eval| {:form form})))

(defn reload
  []
  (require
   '[Cara-Dune.seed]
   '[Cara-Dune.raisins]
   '[Cara-Dune.peanuts]
   '[Cara-Dune.kiwis]
   '[Cara-Dune.salt]
   '[Cara-Dune.microwaved-potatoes]
   '[Cara-Dune.corn]
   '[Cara-Dune.beans]
   '[Cara-Dune.main]
   :reload))

(defn -main
  [& args]
  (println "i dont want my next job")

  #_(alter-var-root #'*ns* (constantly (find-ns 'Cara-Dune.main)))

  (when SystemInfo/isMacOS
    (System/setProperty "apple.laf.useScreenMenuBar" "true")
    (System/setProperty "apple.awt.application.name" jframe-title)
    (System/setProperty "apple.awt.application.appearance" "system"))

  (when SystemInfo/isLinux
    (JFrame/setDefaultLookAndFeelDecorated true)
    (JDialog/setDefaultLookAndFeelDecorated true))

  (when (and
         (not SystemInfo/isJava_9_orLater)
         (= (System/getProperty "flatlaf.uiScale") nil))
    (System/setProperty "flatlaf.uiScale" "2x"))

  (FlatLightLaf/setup)

  (FlatDesktop/setQuitHandler (reify Consumer
                                (accept [_ response]
                                  (.performQuit ^FlatDesktop$QuitResponse response))
                                (andThen [_ after] after)))

  (let [screenshotsMode? (Boolean/parseBoolean (System/getProperty "flatlaf.demo.screenshotsMode"))

        jframe (JFrame. jframe-title)
        jmenubar (JMenuBar.)
        jtoolbar (JToolBar.)
        jroot-panel (JPanel.)

        jcode-panel (JPanel.)
        jrepl (JTextArea. 1 80)
        joutput (JTextArea. 14 80)
        joutput-scroll (JScrollPane.)
        jeditor (JEditorPane.)
        jeditor-scroll (JScrollPane.)

        canvas (Canvas.)
        jcanvas-panel (JPanel.)]

    (let [data-dir-path (or
                         (some-> (System/getenv "CARA_DUNE_PATH")
                                 (.replaceFirst "^~" (System/getProperty "user.home")))
                         (.getCanonicalPath ^File (Wichita.java.io/file (System/getProperty "user.home") "Cara-Dune")))
          state-file-path (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path "Cara-Dune.edn"))]
      (Wichita.java.io/make-parents data-dir-path)
      (reset! stateA {})
      (reset! gamesA {})
      (reset! gameA {})
      (reset! settingsA {:editor? true}))

    (SwingUtilities/invokeLater
     (reify Runnable
       (run [_]

            (doto jframe
              (.add jroot-panel)
              (.addComponentListener (let []
                                       (reify ComponentListener
                                         (componentHidden [_ event])
                                         (componentMoved [_ event])
                                         (componentResized [_ event] (put! resize| (.getTime (java.util.Date.))))
                                         (componentShown [_ event])))))

            (doto jroot-panel
              #_(.setLayout (BoxLayout. jroot-panel BoxLayout/Y_AXIS))
              (.setLayout (MigLayout. "insets 10"
                                      "[grow,shrink,fill]"
                                      "[grow,shrink,fill]")))

            (when-let [url (Wichita.java.io/resource "icon.png")]
              (.setIconImage jframe (.getImage (ImageIcon. url))))

            (Cara-Dune.kiwis/menubar-process
             {:jmenubar jmenubar
              :jframe jframe
              :menubar| ops|})
            (.setJMenuBar jframe jmenubar)

            #_(Cara-Dune.kiwis/toolbar-process
               {:jtoolbar jtoolbar})
            #_(.add jroot-panel jtoolbar "dock north")

            #_(Cara-Dune.beans/editor-process
               {:ns-sym 'Cara-Dune.main
                :eval| eval|
                :jcode-panel jcode-panel
                :jrepl jrepl
                :joutput joutput
                :joutput-scroll joutput-scroll
                :jeditor jeditor
                :jeditor-scroll jeditor-scroll})
            #_(.add jroot-panel jcode-panel "dock west")

            (Cara-Dune.peanuts/canvas-process
             {:jcanvas-panel jcanvas-panel
              :canvas canvas})
            (.add jroot-panel jcanvas-panel "dock east,width 50%:100%:100%, height 1:100%:")

            (.setPreferredSize jframe
                               (let [size (-> (Toolkit/getDefaultToolkit) (.getScreenSize))]
                                 (Dimension. (UIScale/scale 1024) (UIScale/scale 576)))
                               #_(if SystemInfo/isJava_9_orLater
                                   (Dimension. 830 440)
                                   (Dimension. 1660 880)))

            #_(doto jframe
                (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
                (.setSize 2400 1600)
                (.setLocation 1300 200)
                #_(.add panel)
                (.setVisible true))

            #_(println :before (.getGraphics canvas))
            (doto jframe
              (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
              (.pack)
              (.setLocationRelativeTo nil)
              (.setVisible true))
            #_(println :after (.getGraphics canvas))

            (alter-var-root #'Cara-Dune.main/jframe (constantly jframe))
            (alter-var-root #'Cara-Dune.main/joutput-scroll (constantly joutput-scroll))
            (alter-var-root #'Cara-Dune.main/jrepl (constantly jrepl))
            (alter-var-root #'Cara-Dune.main/joutput (constantly joutput))
            (alter-var-root #'Cara-Dune.main/jeditor (constantly jeditor))
            (alter-var-root #'Cara-Dune.main/canvas (constantly canvas))
            (alter-var-root #'Cara-Dune.main/graphics (constantly (.getGraphics canvas)))
            (alter-var-root #'Cara-Dune.main/jroot-panel (constantly jroot-panel))
            (alter-var-root #'Cara-Dune.main/jcode-panel (constantly jcode-panel))

            (remove-watch stateA :watch-fn)
            (add-watch stateA :watch-fn
                       (fn [ref wathc-key old-state new-state]

                         (when (not= old-state new-state)
                           (put! canvas-draw| true))))

            (remove-watch settingsA :main)
            (add-watch settingsA :main
                       (fn [ref wathc-key old-state new-state]
                         (SwingUtilities/invokeLater
                          (reify Runnable
                            (run [_]
                              (if (:editor? @settingsA)
                                (.add jroot-panel jcode-panel "dock west")
                                (.remove jroot-panel jcode-panel))
                              (force-resize))))))
            (reset! settingsA @settingsA)

            (do
              (->>
               '(let [locations| (->
                                  [{:row 1 :col 1}
                                   {:row 3 :col 3}
                                   {:row (rand-int 10) :col (rand-int 10)}
                                   {:row (rand-int 10) :col (rand-int 10)}
                                   {:row (rand-int 10) :col (rand-int 10)}
                                   {:row (rand-int 10) :col (rand-int 10)}]
                                  (to-chan!))]
                  (go
                    (loop []
                      (when-let [value (<! locations|)]
                        (<! (timeout 1000))
                        (clear-canvas)
                        (draw-grid)
                        (draw-line)
                        (draw-word)
                        (draw-Cara value)
                        (recur)))))
               (Wichita.pprint/pprint)
               (with-out-str)
               #_(Joker.core/reformat-string)
               (.setText jeditor))

              (.setText jrepl "(transmit)")

              (force-resize)

              #_(go
                  (<! (timeout 50))
                  (Cara-Dune.beans/print-ns-fns-docs 'Cara-Dune.main eval|))))))


    (go
      (loop [timeout| nil]
        (let [[value port] (alts! (concat [resize|] (when timeout| [timeout|])))]
          (condp = port

            resize|
            (let []
              #_(println :resize)
              (recur (timeout 500)))

            timeout|
            (let []
              (>! canvas-draw| true)
              (recur nil))))))

    (go
      (loop []
        (let [[value port] (alts! [canvas-draw|])]
          (condp = port
            canvas-draw|
            (let []
              (SwingUtilities/invokeLater
               (reify Runnable
                 (run [_]
                   (clear-canvas)
                   (draw-grid)
                   (draw-line)
                   (draw-word))))
              (recur))))))

    (go
      (loop []
        (when-let [{:keys [message from] :as value} (<! sub|)]
          (condp = (:op message)
            :game-state
            (let [{:keys [game-state]} message]
              (swap! gameA merge game-state))
            :player-state
            (let [{:keys [game-state]} message]
              (swap! gameA update-in [:players from] merge message))
            :games
            (let [{:keys [frequency host-peer-id]} message]
              (swap! gamesA update-in [frequency] merge message)))
          (recur))))

    (go
      (loop []
        (<! (timeout 3000))
        (let [expired (into []
                            (comp
                             (keep (fn [[frequency {:keys [timestamp]}]]
                                     #_(println (- (.getTime (java.util.Date.)) timestamp))
                                     (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                       frequency))))
                            @gamesA)]
          (when-not (empty? expired)
            (apply swap! gamesA dissoc expired)))
        (recur)))

    (go
      (loop []
        (<! (timeout 3000))
        (let [expired (into []
                            (comp
                             (keep (fn [[frequency {:keys [timestamp peer-id]}]]
                                     #_(println (- (.getTime (java.util.Date.)) timestamp))
                                     (when-not (< (- (.getTime (java.util.Date.)) timestamp) 4000)
                                       frequency))))
                            (:players @gameA))]
          (when-not (empty? expired)
            (apply swap! gameA update :players dissoc expired)))
        (recur)))

    (go
      (loop []
        (when-let [value (<! ops|)]
          (condp = (:op value)
            :game
            (let [{:keys [frequency role]} value

                  games-topic "raisins"
                  game-topic frequency
                  _ (Cara-Dune.corn/subscribe-process
                     {:sub| sub|
                      :cancel| cancel-sub|
                      :frequency frequency})
                  host? (= role :host)]
              #_(println :game value)
              (go
                (loop []
                  (alt!
                    cancel-pub|
                    ([_] (do nil))

                    (timeout 2000)
                    ([_]
                     (when host?)

                     (recur))))))

            :leave
            (let [{:keys [frequency]} value]
              (>! cancel-sub| true)
              (>! cancel-pub| true)
              (reset! gameA {}))

            :discover
            (let [discover-jframe (JFrame. "discover")]
              (Cara-Dune.kiwis/discover-process
               {:jframe discover-jframe
                :root-jframe jframe
                :ops| ops|
                :gamesA gamesA
                :gameA gameA
                :stateA stateA})
              (reset! gameA @gameA))

            :settings
            (let [settings-jframe (JFrame. "settings")]
              (Cara-Dune.kiwis/settings-process
               {:jframe settings-jframe
                :root-jframe jframe
                :ops| ops|
                :settingsA settingsA}))

            :settings-value
            (let []
              (swap! settingsA merge value))

            :host-yes
            (let [{:keys [frequency]} value]
              (println :frequency frequency)))

          (recur))))

    (let [raw-stream-connection-pool (Simba.http/connection-pool {:connection-options {:raw-stream? true}})]
      
      (alter-var-root #'raw-stream-connection-pool (constantly raw-stream-connection-pool))
      (Cara-Dune.corn/subscribe-process
       {:sub| sub|
        :raw-stream-connection-pool raw-stream-connection-pool
        :cancel| (chan (sliding-buffer 1))
        :frequency "raisins"}))

    (let [port (or (try (Integer/parseInt (System/getenv "PORT"))
                        (catch Exception e nil))
                   3344)]
      (Cara-Dune.microwaved-potatoes/process
       {:port port})))
  (println "Kuiil has spoken"))