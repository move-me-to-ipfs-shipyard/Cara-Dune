(ns Cara-Dune.main
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]
   [clojure.repl :as Wichita.repl]

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
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane)
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
   (net.miginfocom.layout ConstraintParser LC UnitValue))
  (:gen-class))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))
(defonce resize| (chan (sliding-buffer 1)))
(defonce exit||A (atom #{}))
(defonce canvas-draw| (chan (sliding-buffer 1)))
(def ^:dynamic ^JFrame jframe nil)
(def ^:dynamic ^Canvas canvas nil)
(def ^:dynamic ^JTextArea repl nil)
(def ^:dynamic ^JTextArea output nil)
(def ^:dynamic ^JEditorPane editor nil)
(def ^:dynamic ^JScrollPane output-scroll nil)
(def ^:dynamic ^Graphics2D graphics nil)
(defonce ns* (find-ns 'Cara-Dune.main))

(def ^:const energy-per-move 100)
(def ^:const canvas-width 1600)
(def ^:const canvas-height 1600)
(def ^:const tile-size 32)
(def ^:const jframe-title "one X-Wing? great - we're saved")

(defn eval-form
  ([form]
   (eval-form form {}))
  ([form {:keys [:print-form?] :or {print-form? true} :as opts}]
   (let [string-writer (java.io.StringWriter.)
         result (binding [*ns* ns*
                          *out* string-writer]
                  (eval form))]
     (doto output
       (.append "=> "))
     (when print-form?
       (doto output
         (.append (str form))
         (.append "\n")))
     (doto output
       (.append (str string-writer))
       (.append (if (string? result) result (pr-str result)))
       (.append "\n"))

     (go
       (<! (timeout 10))
       (let [scrollbar (.getVerticalScrollBar output-scroll)]
         (.setValue scrollbar (.getMaximum scrollbar)))))))

(defn draw-word
  "draw word"
  []
  (.drawString graphics "word" (* 0.5 (.getWidth canvas)) (* 0.5 (.getHeight canvas))))

(defn draw-line
  "draw line"
  []
  (.drawLine graphics  (* 0.3 (.getWidth canvas)) (* 0.3 (.getHeight canvas)) (* 0.7 (.getWidth canvas)) (* 0.7 (.getHeight canvas))))

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
  (.setText output ""))

(defn transmit
  "evaluate code in spe-editor-bike"
  []
  (-> (.getText editor) (clojure.string/trim) (clojure.string/trim-newline) (read-string) (eval-form)))

(defn print-fns
  []
  (go
    (let [fn-names (keys (ns-publics 'Cara-Dune.main))]
      (doseq [fn-name fn-names]
        (print (eval-form `(with-out-str (Wichita.repl/doc ~fn-name)) {:print-form? false}))))))

(defn create-jframe
  []
  (let [jframe (JFrame. jframe-title)
        root-panel (JPanel.)
        screenshotsMode? (Boolean/parseBoolean (System/getProperty "flatlaf.demo.screenshotsMode"))

        on-menubar-item (fn [f]
                          (reify ActionListener
                            (actionPerformed [_ event]
                              (SwingUtilities/invokeLater
                               (reify Runnable
                                 (run [_]
                                   (f _ event)))))))

        on-menu-item-show-dialog (on-menubar-item (fn [_ event] (JOptionPane/showMessageDialog jframe (.getActionCommand ^ActionEvent event) "menu bar item" JOptionPane/PLAIN_MESSAGE)))]

    (alter-var-root #'Cara-Dune.main/jframe (constantly jframe))

    (doto root-panel
      #_(.setLayout (BoxLayout. root-panel BoxLayout/Y_AXIS))
      (.setLayout (MigLayout. "insets 10"
                              "[grow,shrink,fill]"
                              "[grow,shrink,fill]")))

    (doto jframe
      (.add root-panel)
      (.addComponentListener (let []
                               (reify ComponentListener
                                 (componentHidden [_ event])
                                 (componentMoved [_ event])
                                 (componentResized [_ event] (put! resize| (.getTime (java.util.Date.))))
                                 (componentShown [_ event])))))


    (let [jmenubar (JMenuBar.)]
      (doto jmenubar
        (.add (doto (JMenu.)
                (.setText "file")
                (.setMnemonic \F)
                (.add (doto (JMenuItem.)
                        (.setText "new")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_N (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \U)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "exit")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Q (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \X)
                        (.addActionListener (on-menubar-item (fn [_ event]
                                                               (.dispose jframe))))))))

        (.add (doto (JMenu.)
                (.setText "edit")
                (.setMnemonic \E)
                (.add (doto (JMenuItem.)
                        (.setText "undo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Z (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \U)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "redo")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Y (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \R)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "cut")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_X (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \C)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "copy")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_C (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \O)
                        (.addActionListener on-menu-item-show-dialog)))
                (.add (doto (JMenuItem.)
                        (.setText "paste")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_V (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \P)
                        (.addActionListener on-menu-item-show-dialog)))
                (.addSeparator)
                (.add (doto (JMenuItem.)
                        (.setText "delete")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_DELETE 0))
                        (.setMnemonic \D)
                        (.addActionListener on-menu-item-show-dialog))))))

      (.setJMenuBar jframe jmenubar))

    (FlatDesktop/setQuitHandler (reify Consumer
                                  (accept [_ response]
                                    (.performQuit ^FlatDesktop$QuitResponse response))
                                  (andThen [_ after] after)))

    (let [jtoolbar (JToolBar.)]
      (doto jtoolbar
        #_(.setMargin (Insets. 3 3 3 3))
        (.add (doto (JButton.)
                (.setToolTipText "new file")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/NEW_FILE (UIScale/scale 16) Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "open file")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/FOLDER_OPENED (UIScale/scale 16) Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "save")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/SAVE (UIScale/scale 16) Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "undo")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/DISCARD (UIScale/scale 16) Color/BLACK))))
        (.add (doto (JButton.)
                (.setToolTipText "redo")
                (.setIcon (FontIcon/of org.kordamp.ikonli.codicons.Codicons/REDO (UIScale/scale 16) Color/BLACK))))
        #_(.addSeparator))

      (.add root-panel jtoolbar "dock north"))

    (let [content-panel (JPanel.)
          split-pane (JSplitPane.)]
      (doto content-panel
        (.setLayout (BoxLayout. content-panel BoxLayout/X_AXIS))
        #_(.add (doto split-pane
                  (.setResizeWeight 0.5))))

      (let [code-panel (JPanel.)
            code-layout (BoxLayout. code-panel BoxLayout/Y_AXIS)
            repl (JTextArea. 1 80)
            output (JTextArea. 14 80)
            output-scroll (JScrollPane.)
            editor (JEditorPane.)
            editor-scroll (JScrollPane.)]

        (doto editor
          (.setBorder (EmptyBorder. #_top 0 #_left 0 #_bottom 0 #_right 0)))

        (doto editor-scroll
          (.setViewportView editor)
          (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER)
          #_(.setPreferredSize (Dimension. 800 1300)))

        (doto output
          (.setEditable false))

        (doto output-scroll
          (.setViewportView output)
          (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

        (doto repl
          (.addKeyListener (reify KeyListener
                             (keyPressed
                               [_ event]
                               (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
                                 (.consume ^KeyEvent event)))
                             (keyReleased
                               [_ event]
                               (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
                                 (-> (.getText repl) (clojure.string/trim) (clojure.string/trim-newline) (read-string) (eval-form))
                                 (.setText repl "")))
                             (keyTyped
                               [_ event]))))

        (doto code-panel
          (.setLayout (MigLayout. "insets 0"
                                  "[grow,shrink,fill]"
                                  "[grow,shrink,fill]"))
          (.add editor-scroll "wrap,height 70%")
          (.add output-scroll "wrap,height 30%")
          (.add repl "wrap"))

        (.add root-panel code-panel "dock west")
        #_(.setLeftComponent split-pane code-panel)

        (alter-var-root #'Cara-Dune.main/output-scroll (constantly output-scroll))
        (alter-var-root #'Cara-Dune.main/repl (constantly repl))
        (alter-var-root #'Cara-Dune.main/output (constantly output))
        (alter-var-root #'Cara-Dune.main/editor (constantly editor)))

      (let [canvas (Canvas.)
            canvas-panel (JPanel.)]

        (doto canvas-panel
          (.setLayout (MigLayout. "insets 0"
                                  "[grow,shrink,fill]"
                                  "[grow,shrink,fill]") #_(BoxLayout. canvas-panel BoxLayout/X_AXIS))
          #_(.setBorder (EmptyBorder. #_top 0 #_left 0 #_bottom 50 #_right 50)))

        (doto canvas
          #_(.setPreferredSize (Dimension. canvas-width canvas-height))
          (.addMouseListener (reify MouseListener
                               (mouseClicked
                                 [_ event]
                                 (println :coordinate [(.getX ^MouseEvent event) (.getY ^MouseEvent event)]))
                               (mouseEntered [_ event])
                               (mouseExited [_ event])
                               (mousePressed [_ event])
                               (mouseReleased [_ event]))))

        #_(.setRightComponent split-pane canvas)

        (.add canvas-panel canvas "width 100%!,height 100%!")

        (.add root-panel canvas-panel "dock east,width 50%!, height 1:100%:")
        (go
          (<! (timeout 50))
          (alter-var-root #'Cara-Dune.main/canvas (constantly canvas))
          (alter-var-root #'Cara-Dune.main/graphics (constantly (.getGraphics canvas)))))

      (.add root-panel content-panel))



    (when-let [url (Wichita.java.io/resource "icon.png")]
      (.setIconImage jframe (.getImage (ImageIcon. url))))




    nil))

(defn window
  []
  (let []

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

    (doseq [exit| @exit||A]
      (close! exit|))

    (let [exit| (chan 1)]
      (swap! exit||A conj exit|)
      (go
        (loop [timeout| nil]
          (let [[value port] (alts! (concat [resize| exit|] (when timeout| [timeout|])))]
            (condp = port

              resize|
              (let []
                #_(println :resize)
                (recur (timeout 500)))

              timeout|
              (let []
                (>! canvas-draw| true)
                (recur nil))

              exit|
              (do
                (swap! exit||A disj exit|)
                nil))))))

    (let [exit| (chan 1)]
      (go
        (loop []
          (let [[value port] (alts! [canvas-draw| exit|])]
            (condp = port
              canvas-draw|
              (let []
                #_(println :canvas-draw)
                (clear-canvas)
                (draw-line)
                (draw-word)
                (recur))

              exit|
              (do
                (swap! exit||A disj exit|)
                nil))))))

    (SwingUtilities/invokeLater
     (reify Runnable
       (run [_]

         (FlatLightLaf/setup)

         (create-jframe)

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
         (doto jframe
           (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
           (.pack)
           (.setLocationRelativeTo nil)
           (.setVisible true))


         (remove-watch stateA :watch-fn)
         (add-watch stateA :watch-fn
                    (fn [ref wathc-key old-state new-state]

                      (when (not= old-state new-state)
                        (put! canvas-draw| true))))

         (do
           (eval-form `(print-fns))
           (force-resize)

           (.setText editor
                     "(go 
    (loop [n 3]
    (when (> n 0)
      (clear-canvas)
      (<! (timeout 1000))
      (draw-word)
      (<! (timeout 1000))
      (draw-line)
      (<! (timeout 1000))
      (recur (dec n)))))
 (.setText repl \"(transmit)\"))")

           (.setText repl "(transmit)")))))))

(defn reload
  []
  (require '[Cara-Dune.main] :reload))

(defn process
  []
  (go
    (<! (timeout 1000))
    (println "Kuiil has spoken")))

(defn -main
  [& args]
  (println "i dont want my next job")
  (reset! stateA {})
  (window)
  (println "Kuiil has spoken"))