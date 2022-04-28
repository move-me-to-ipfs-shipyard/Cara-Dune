(ns Cara-Dune.microwaved-potatoes
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]

   [Cara-Dune.seed])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JLabel JTextPane)
   (javax.swing.border EmptyBorder)
   (javax.swing.event DocumentListener DocumentEvent)
   (javax.swing.text SimpleAttributeSet StyleConstants JTextComponent)
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
   (java.io ByteArrayOutputStream)
   (java.lang Runnable)

   (java.awt.image BufferedImage)
   (java.awt Image Graphics2D Color)
   (javax.imageio ImageIO)
   (java.security MessageDigest)))

(defn menubar-process
  [{:keys [jmenubar
           jframe]
    :as opts}]
  (let [on-menubar-item (fn [f]
                          (reify ActionListener
                            (actionPerformed [_ event]
                              (SwingUtilities/invokeLater
                               (reify Runnable
                                 (run [_]
                                   (f _ event)))))))

        on-menu-item-show-dialog (on-menubar-item (fn [_ event] (JOptionPane/showMessageDialog jframe (.getActionCommand ^ActionEvent event) "menu bar item" JOptionPane/PLAIN_MESSAGE)))]
    (doto jmenubar
      (.add (doto (JMenu.)
              (.setText "program")
              (.setMnemonic \F)
              (.add (doto (JMenuItem.)
                      (.setText "host")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_N (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \U)
                      (.addActionListener on-menu-item-show-dialog)))
              (.add (doto (JMenuItem.)
                      (.setText "join")
                      #_(.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_N (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      #_(.setMnemonic \U)
                      (.addActionListener on-menu-item-show-dialog)))
              (.add (doto (JMenuItem.)
                      (.setText "observe")
                      #_(.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_N (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      #_(.setMnemonic \U)
                      (.addActionListener on-menu-item-show-dialog)))
              (.add (doto (JMenuItem.)
                      (.setText "discover")
                      #_(.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_N (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      #_(.setMnemonic \U)
                      (.addActionListener on-menu-item-show-dialog)))
              (.add (doto (JMenuItem.)
                      (.setText "exit")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Q (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \X)
                      (.addActionListener (on-menubar-item (fn [_ event]
                                                             (.dispose jframe))))))))

      #_(.add (doto (JMenu.)
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
                        (.addActionListener on-menu-item-show-dialog)))))))
  nil)

(defn toolbar-process
  [{:keys [jtoolbar]
    :as opts}]
  (let []
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
      #_(.addSeparator)))
  nil)