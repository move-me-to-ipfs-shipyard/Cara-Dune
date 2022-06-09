(ns Cara-Dune.seed
  (:require
   [clojure.core.async
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io]
   [clojure.string]
   [clojure.repl])

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

   (java.awt.image BufferedImage)
   (java.awt Image Graphics2D Color)
   (javax.imageio ImageIO)
   (java.security MessageDigest)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))


(defn invoke-later-on-swing-edt
  [f]
  (SwingUtilities/invokeLater
   (reify Runnable
     (run [_]
       (f _)))))

(defn color-for-word ^Color
  [^String word]
  (let [digest (->
                (doto (MessageDigest/getInstance "SHA-256")
                  (.update (.getBytes word)))
                (.digest))
        size (alength digest)
        step 3
        n (- size (mod size 3))
        positions (range 0 n step)
        positions-size (count positions)
        colors (->>
                (reduce
                 (fn [result i]
                   (-> result
                       (update :red + (bit-and (aget digest i) 0xff))
                       (update :green + (bit-and (aget digest (+ i 1)) 0xff))
                       (update :blue + (bit-and (aget digest (+ i 2)) 0xff))))
                 {:red 0 :green 0 :blue 0}
                 positions)
                (map (fn [[k value]]
                       [k (-> value (/ positions-size))]))
                (into {}))]
    (Color. (int (:red colors)) (int (:green colors)) (int (:blue colors)))))