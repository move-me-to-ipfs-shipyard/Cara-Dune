#_(ns Cara-Dune.orange-seeds
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
   [clojure.walk :as Wichita.walk]
   [clojure.test.check.generators :as Pawny.generators]
   [clj-http.client]
   [cheshire.core]

   [Cara-Dune.seed]
   [Cara-Dune.ipfs-http])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JLabel)
   (javax.swing.border EmptyBorder)
   (java.awt Canvas Graphics Graphics2D Shape Color Polygon Dimension BasicStroke Toolkit Insets BorderLayout)
   (java.awt.event KeyListener KeyEvent MouseListener MouseEvent ActionListener ActionEvent ComponentListener ComponentEvent)
   (java.awt.geom Ellipse2D Ellipse2D$Double Point2D$Double)
   (com.formdev.flatlaf FlatLaf FlatLightLaf)
   (com.formdev.flatlaf.extras FlatUIDefaultsInspector FlatDesktop FlatDesktop$QuitResponse FlatSVGIcon)
   (com.formdev.flatlaf.util SystemInfo UIScale)
   (java.util.function Consumer Function)
   (java.util ServiceLoader)
   (org.kordamp.ikonli Ikon)
   (org.kordamp.ikonli IkonProvider)
   (org.kordamp.ikonli.swing FontIcon)
   (org.kordamp.ikonli.codicons Codicons)
   (net.miginfocom.swing MigLayout)
   (net.miginfocom.layout ConstraintParser LC UnitValue)

   (io.ipfs.api IPFS)
   (java.util.stream Stream)
   (java.util Base64)
   (java.nio.charset StandardCharsets)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defonce stateA (atom nil))
(defonce draw| (chan (sliding-buffer 10)))

(defn clear-canvas
  [{:keys [^Canvas canvas]
    :as opts}]
  (let [^Graphics2D graphics (.getGraphics canvas)]
    (.clearRect graphics 0 0 (.getWidth canvas)  (.getHeight canvas))
    (.setPaint graphics (Color. 255 255 255 255) #_(Color. 237 211 175 200))
    (.fillRect graphics 0 0 (.getWidth canvas) (.getHeight canvas))
    (.setPaint graphics  Color/BLACK)))

(defn create-ui
  [{:keys [resize|
           jframe]
    :as opts}]
  (let [jtab-panel (JPanel.)
        split-pane (JSplitPane.)
        canvas (Canvas.)
        jcanvas-panel (JPanel.)]

    (doto jtab-panel
      (.setLayout (MigLayout. "insets 0"
                              "[grow,shrink,fill]"
                              "[grow,shrink,fill]"))
      #_(.setLayout (BoxLayout. tab-panel BoxLayout/X_AXIS))
      #_(.add (doto split-pane
                (.setResizeWeight 0.5))))

    (doto jcanvas-panel
      (.setLayout (MigLayout. "insets 0"
                              "[grow,shrink,fill]"
                              "[grow,shrink,fill]") #_(BoxLayout. jcanvas-panel BoxLayout/X_AXIS))
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

    (.add jcanvas-panel canvas "width 100%!,height 100%!")

    (.add jtab-panel jcanvas-panel "dock east,width 1:100%:, height 1:100%:")

    (go
      (loop []
        (when-let [value (<! draw|)]
          (Cara-Dune.seed/invoke-later-on-swing-edt
           (fn [_]
             (let []
               #_(println :canvas-draw)
               (clear-canvas {:canvas canvas})
               #_(draw-line)
               #_(draw-word))))
          (recur))))

    (go
      (loop [timeout| nil]
        (let [[value port] (alts! (concat [resize|] (when timeout| [timeout|])))]
          (condp = port

            resize|
            (let []
              (when value
                #_(println :resize)
                (recur (timeout 500))))

            timeout|
            (let []
              #_(println (.getWidth canvas) (.getHeight canvas))
              (>! draw| true)
              (recur nil))))))

    jtab-panel))

(defn process
  [{:keys [resize|
           stop|]
    :as opts}]

  (let []

    (go
      (<! stop|)
      (close! draw|)
      (remove-watch stateA :watch-fn))

    (let []
      (reset! stateA {:correct 0
                      :incorrect 0
                      :grid-rows 16
                      :grid-cols 32}))


    (add-watch stateA :watch-fn
               (fn [ref wathc-key old-state new-state]

                 (when (not= old-state new-state)
                   (put! draw| true))))

    (let [ipfs (IPFS. "/ip4/127.0.0.1/tcp/5001")
          base-url "http://127.0.0.1:5001"
          topic (Cara-Dune.ipfs-http/encode-base64url-u "raisins")
          id (-> ipfs (.id) (.get "ID"))
          sub| (Cara-Dune.ipfs-http/pubsub-sub base-url  topic)]

      (go
        (loop []
          (when-let [value (<! sub|)]
            (when-not (= (:from value) id)
              (println (merge value
                              {:data (-> (:data value) (Cara-Dune.ipfs-http/decode-base64url-u) (read-string))})))
            (recur))))

      (go
        (loop []
          (<! (timeout 2000))
          (Cara-Dune.ipfs-http/pubsub-pub base-url topic (str {:id id
                                                           :rand-int (rand-int 100)}))
          (recur))))))

(comment

  (-> ipfs (.-pubsub) (.sub topic))

  (.map sub (reify Function
              (apply [_ data]
                (println data)
                (println (type data)))
              (andThen [_ after] after)))

  (go
    (loop []
      (<! (timeout 2000))
      (-> ipfs (.-pubsub) (.pub topic (Cara-Dune.ipfs-http/encode-base64url-u (format "%s %s" (rand-int 100) id))))
      (recur)))
  ;
  )

