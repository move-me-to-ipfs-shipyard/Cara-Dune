(ns Cara-Dune.kiwis
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

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn draw-grid
  [{:keys [^Canvas canvas
           ^Graphics2D graphics
           grid-rows
           grid-cols]
    :or {}}]
  (let [row-height (/ (.getHeight canvas) grid-rows)
        col-width (/ (.getWidth canvas) grid-cols)]
    (doseq [row-i (range grid-rows)]
      (let [y (* row-i row-height)]
        (.drawLine graphics 0 y (.getWidth canvas) y)))
    (doseq [col-i (range grid-cols)]
      (let [x (* col-i col-width)]
        (.drawLine graphics x 0 x (.getHeight canvas))))))

(defn canvas-process
  [{:keys [^JPanel jcanvas-panel
           ^Canvas canvas]
    :or {}
    :as opts}]
  (let []

    (doto jcanvas-panel
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

    (.add jcanvas-panel canvas "width 100%!,height 100%!")))