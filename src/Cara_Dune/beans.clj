(ns Cara-Dune.beans
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
   (java.io ByteArrayOutputStream PrintStream)
   (java.lang Runnable)

   (java.awt.image BufferedImage)
   (java.awt Image Graphics2D Color)
   (javax.imageio ImageIO)
   (java.security MessageDigest)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

#_(defn print-ns-fns-docs
    [ns-sym]
    (go
      (let [fn-names (keys (ns-publics ns-sym))]
        (doseq [fn-name fn-names]
          (print (eval-form `(with-out-str (Wichita.repl/doc ~fn-name)) {}))))))

(defn print-ns-fns-docs
  [ns-sym]
  (let [fn-names (keys (ns-publics ns-sym))]
    (doseq [fn-name fn-names]
      (binding [*ns* (find-ns ns-sym)]
        (eval `(Wichita.repl/doc ~fn-name))))))

(defn console-output-stream
  [{:keys [^Color text-color
           ^PrintStream print-stream
           append?
           ^JTextComponent jtext-component]
    :as opts}]
  (let [eol (System/getProperty "line.separator")
        document (.getDocument jtext-component)
        baos (ByteArrayOutputStream.)
        attributes (SimpleAttributeSet.)
        buffer (StringBuffer. 80)
        first-line?V (volatile! append?)
        clear-buffer (fn []
                       (when (and @first-line?V
                                  (not= (.getLength document) 0))
                         (.insert buffer 0 "\n"))

                       (vreset! first-line?V false)
                       (let [line (.toString buffer)]
                         (if append?
                           (let [offset (.getLength document)]
                             (.insertString document offset line attributes)
                             (.setCaretPosition jtext-component (.getLength document)))
                           (let []
                             (.insertString document 0 line attributes)
                             (.setCaretPosition jtext-component 0)))
                         (when print-stream
                           (.print print-stream line))
                         (.setLength buffer 0)))]
    #_(when text-color
        (StyleConstants/setForeground attributes text-color))
    (proxy [ByteArrayOutputStream] []
      (flush []
        (println :flush)
        (try
          (let [message (.toString ^ByteArrayOutputStream this)]
            (when (not= (.length message) 0)

              (if append?
                (let []
                  (when (= (.getLength document) 0)
                    (.setLength buffer 0))
                  (if (.equals eol message)
                    (.append buffer message)
                    (do
                      (.append buffer message)
                      (clear-buffer))))
                (let []
                  (.append buffer message)
                  (when (.equals eol message)
                    (clear-buffer))))

              (.reset ^ByteArrayOutputStream this)))
          (catch Exception ex (println ex)))))))

(defn console-process
  [{:keys [^JTextComponent jtext-component
           remove-from-start?
           ^int max-lines]
    :or {remove-from-start? true
         max-lines 100}
    :as opts}]
  (let [document (.getDocument jtext-component)]

    (.addDocumentListener
     document
     (reify DocumentListener
       (changedUpdate [_ event])
       (insertUpdate [_ event]
         (SwingUtilities/invokeLater
          (reify Runnable
            (run [_]
              (let [root (.getDefaultRootElement document)
                    excess (- (.getElementCount root) max-lines)]
                (when (> excess 0)
                  (if remove-from-start?
                    (let [line (.getElement root (- excess 1))
                          end (.getEndOffset line)]
                      (.remove document 0 end))
                    (let [line (.getElement root max-lines)
                          start (.getStartOffset line)
                          end (.getEndOffset root)]
                      (.remove document (- start 1) (- end start))))))))))
       (removeUpdate [_ event])))

    (.setEditable jtext-component false)

    (System/setOut (PrintStream.
                    ^ByteArrayOutputStream
                    (console-output-stream {:text-color Color/BLACK
                                            :print-stream System/out
                                            :append? remove-from-start?
                                            :jtext-component jtext-component})
                    true))
    (System/setErr (PrintStream.
                    ^ByteArrayOutputStream
                    (console-output-stream {:text-color Color/BLACK
                                            :print-stream System/err
                                            :append? remove-from-start?
                                            :jtext-component jtext-component})
                    true)))
  nil)

(defn editor-process
  [{:keys [*ns
           ^JPanel jcode-panel
           ^JTextArea jrepl
           ^JTextArea joutput
           ^JScrollPane joutput-scroll
           ^JEditorPane jeditor
           ^JScrollPane jeditor-scroll]
    :as opts}]
  (let [eval| (chan 10)]

    (doto jeditor
      #_(.setBorder (EmptyBorder. #_top 0 #_left 0 #_bottom 0 #_right 0)))

    (doto jeditor-scroll
      (.setViewportView jeditor)
      (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER)
      #_(.setPreferredSize (Dimension. 800 1300)))

    (console-process
     {:jtext-component joutput
      :remove-from-start? true
      :max-lines 100})

    (doto joutput-scroll
      (.setViewportView joutput)
      (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

    (doto jrepl
      (.addKeyListener (reify KeyListener
                         (keyPressed
                           [_ event]
                           (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
                             (.consume ^KeyEvent event)))
                         (keyReleased
                           [_ event]
                           (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
                             (let [string (-> (.getText jrepl) (clojure.string/trim) (clojure.string/trim-newline))]
                               (when (not (empty? string))
                                 (let [form (read-string string)
                                       result (binding [*ns* *ns]
                                                (eval form))]
                                   (put! eval| {:form form
                                                :result result}))
                                 (SwingUtilities/invokeLater
                                  (reify Runnable
                                    (run [_]
                                      (.setText jrepl ""))))))))
                         (keyTyped
                           [_ event]))))

    (doto jcode-panel
      (.setLayout (MigLayout. "insets 0"
                              "[grow,shrink,fill]"
                              "[grow,shrink,fill]"))
      (.add jeditor-scroll "wrap,height 70%")
      (.add joutput-scroll "wrap,height 30%")
      (.add jrepl "wrap"))

    #_(.add tab-panel code-panel "dock west")
    #_(.setLeftComponent split-pane code-panel)

    (go
      (loop []
        (when-let [{:keys [form result]} (<! eval|)]
          (SwingUtilities/invokeLater
           (reify Runnable
             (run [_]
               (doto joutput
                 (.append "=> "))
               (doto joutput
                 (.append (str form))
                 (.append "\n"))
               (doto joutput
                 (.append (if (string? result) result (pr-str result)))
                 (.append "\n")))))
          #_(go
              (<! (timeout 10))
              (SwingUtilities/invokeLater
               (reify Runnable
                 (run [_]
                   (let [scrollbar (.getVerticalScrollBar joutput-scroll)]
                     (.setValue scrollbar (.getMaximum scrollbar)))))))
          (recur))))

    nil))



