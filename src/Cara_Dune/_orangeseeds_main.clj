#_(ns Cara-Dune.main
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
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JLabel JTabbedPane)
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
   (java.lang Runnable)
   
   (java.awt.image BufferedImage)
   (java.awt Image Graphics2D Color)
   (javax.imageio ImageIO)
   (java.security MessageDigest))
  (:gen-class))

#_(println (System/getProperty "clojure.core.async.pool-size"))
(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))

(defn create-jframe
  [{:keys [^String jframe-title
           resize|]
    :as opts}]
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

    (doto root-panel
      #_(.setLayout (BoxLayout. root-panel BoxLayout/Y_AXIS))
      (.setLayout (MigLayout. "wrap,insets 10"
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
                                                               (.dispose jframe)))))))))

      (.setJMenuBar jframe jmenubar))

    (.add root-panel ^JPanel (Cara-Dune.corn/create-ui {:jframe jframe
                                                        :resize| resize|}))

    (when-let [url (Wichita.java.io/resource "icon.png")]
      (.setIconImage jframe (.getImage (ImageIcon. url))))

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

    (let [width (.getWidth jframe)
          height (.getHeight jframe)]
      (.setSize jframe (Dimension. (+ 1 width) height))
      (.setSize jframe (Dimension. width height)))

    nil))

(defn reload
  []
  (require '[Cara-Dune.main] :reload))

(defn -main
  [& args]
  (println "i dont want my next job")
  (let [jframe-title "one X-Wing? great - we're saved"
        resize| (chan (sliding-buffer 1))
        resize|m (mult resize|)
        stop| (chan)]

    (reset! stateA {})
    
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

    (FlatDesktop/setQuitHandler (reify Consumer
                                  (accept [_ response]
                                    (do
                                      (close! stop|)
                                      (close! resize|))
                                    (.performQuit ^FlatDesktop$QuitResponse response))
                                  (andThen [_ after] after)))

    (Cara-Dune.seed/invoke-later-on-swing-edt
     (fn [_]
       (FlatLightLaf/setup)

       (let [{:keys []} (create-jframe {:jframe-title jframe-title
                                        :resize| resize|})]
         (Cara-Dune.orange-seeds/process
          {:stop| stop|
           :resize| (tap resize|m (chan (sliding-buffer 1)))
           :tab-title (str 'raisins)}))

       (do
         #_(eval-form `(print-fns))))))
  (println "Kuiil has spoken"))