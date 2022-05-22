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
   [cheshire.core :as Cheshire-Cat.core]

   [aleph.http :as Simba.http]
   [manifold.deferred :as Nala.deferred]
   [manifold.stream :as Nala.stream]
   [byte-streams :as Rafiki]

   [datahike.api :as Deep-Thought.api]

   [Cara-Dune.seed]
   [Cara-Dune.raisins]
   [Cara-Dune.peanuts]
   [Cara-Dune.kiwis]
   [Cara-Dune.salt]
   [Cara-Dune.microwaved-potatoes]
   [Cara-Dune.corn]
   [Cara-Dune.beans])
  (:import
   (javax.swing JFrame WindowConstants ImageIcon JPanel JScrollPane JTextArea BoxLayout JEditorPane ScrollPaneConstants SwingUtilities JDialog)
   (javax.swing JMenu JMenuItem JMenuBar KeyStroke JOptionPane JToolBar JButton JToggleButton JSplitPane JLabel JTextPane JTextField JTable)
   (javax.swing DefaultListSelectionModel JCheckBox)
   (javax.swing.border EmptyBorder)
   (javax.swing.table DefaultTableModel)
   (javax.swing.event DocumentListener DocumentEvent ListSelectionListener ListSelectionEvent)
   (javax.swing.text SimpleAttributeSet StyleConstants JTextComponent)
   (java.awt Canvas Graphics Graphics2D Shape Color Polygon Dimension BasicStroke Toolkit Insets BorderLayout)
   (java.awt.event KeyListener KeyEvent MouseListener MouseEvent ActionListener ActionEvent ComponentListener ComponentEvent)
   (java.awt.event  WindowListener WindowAdapter WindowEvent)
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
   (java.io File ByteArrayOutputStream PrintStream OutputStreamWriter PrintWriter)
   (java.lang Runnable)

   (io.ipfs.api IPFS)
   (java.util.stream Stream)
   (java.util Base64)
   (java.io BufferedReader)
   (java.nio.charset StandardCharsets))
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
(defonce host| (chan 1))
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


(defn encode-base64url-u
  [^String string]
  (-> (Base64/getUrlEncoder) (.withoutPadding)
      (.encodeToString (.getBytes string StandardCharsets/UTF_8)) (->> (str "u"))))

(defn decode-base64url-u
  [^String string]
  (-> (Base64/getUrlDecoder)
      (.decode (subs string 1))
      (String. StandardCharsets/UTF_8)))

(defn pubsub-sub
  [base-url topic message| cancel| raw-stream-connection-pool]
  (let [streamV (volatile! nil)]
    (->
     (Nala.deferred/chain
      (Simba.http/post (str base-url "/api/v0/pubsub/sub")
                       {:query-params {:arg topic}
                        :pool raw-stream-connection-pool})
      :body
      (fn [stream]
        (vreset! streamV stream)
        stream)
      #(Nala.stream/map Rafiki/to-string %)
      (fn [stream]
        (Nala.deferred/loop
         []
          (->
           (Nala.stream/take! stream :none)
           (Nala.deferred/chain
            (fn [message-string]
              (when-not (identical? message-string :none)
                (let [message (Cheshire-Cat.core/parse-string message-string true)]
                  #_(println :message message)
                  (put! message| message))
                (Nala.deferred/recur))))
           (Nala.deferred/catch Exception (fn [ex] (println ex)))))))
     (Nala.deferred/catch Exception (fn [ex] (println ex))))

    (go
      (<! cancel|)
      (Nala.stream/close! @streamV))
    nil))

(defn pubsub-pub
  [base-url topic message]
  (let []

    (->
     (Nala.deferred/chain
      (Simba.http/post (str base-url "/api/v0/pubsub/pub")
                       {:query-params {:arg topic}
                        :multipart [{:name "file" :content message}]})
      :body
      Rafiki/to-string
      (fn [response-string] #_(println :repsponse reresponse-stringsponse)))
     (Nala.deferred/catch
      Exception
      (fn [ex] (println ex))))

    nil))

(defn subscribe-process
  [{:keys [^String ipfs-api-multiaddress
           ^String ipfs-api-url
           frequency
           raw-stream-connection-pool
           sub|
           cancel|
           id|]
    :as opts}]
  (let [ipfs (IPFS. ipfs-api-multiaddress)
        base-url ipfs-api-url
        topic (encode-base64url-u frequency)
        id (-> ipfs (.id) (.get "ID"))
        message| (chan (sliding-buffer 10))]
    (put! id| {:peer-id id})
    (pubsub-sub base-url  topic message| cancel| raw-stream-connection-pool)

    (go
      (loop []
        (when-let [value (<! message|)]
          (put! sub| (merge value
                            {:message (-> (:data value) (decode-base64url-u) (read-string))}))
          #_(println (merge value
                            {:message (-> (:data value) (decode-base64url-u) (read-string))}))
          #_(when-not (= (:from value) id)

              #_(println (merge value
                                {:message (-> (:data value) (decode-base64url-u) (read-string))})))
          (recur))))

    #_(go
        (loop []
          (<! (timeout 2000))
          (pubsub-pub base-url topic (str {:id id
                                           :rand-int (rand-int 100)}))
          (recur)))))

(defn menubar-process
  [{:keys [^JMenuBar jmenubar
           ^JFrame jframe
           menubar|]
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
                      (.setText "game")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_H (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \H)
                      (.addActionListener
                       (on-menubar-item (fn [_ event]
                                          (put! menubar| {:op :game}))))))
              #_(.add (doto (JMenuItem.)
                        (.setText "join")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_J (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \J)
                        (.addActionListener on-menu-item-show-dialog)))
              #_(.add (doto (JMenuItem.)
                        (.setText "observe")
                        (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_O (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                        (.setMnemonic \O)
                        (.addActionListener on-menu-item-show-dialog)))
              (.add (doto (JMenuItem.)
                      (.setText "discover")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_D (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \D)
                      (.addActionListener
                       (on-menubar-item (fn [_ event]
                                          (put! menubar| {:op :discover}))))))
              (.add (doto (JMenuItem.)
                      (.setText "settings")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_S (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \S)
                      (.addActionListener
                       (on-menubar-item (fn [_ event]
                                          (put! menubar| {:op :settings}))))))
              (.add (doto (JMenuItem.)
                      (.setText "exit")
                      (.setAccelerator (KeyStroke/getKeyStroke KeyEvent/VK_Q (-> (Toolkit/getDefaultToolkit) (.getMenuShortcutKeyMask))))
                      (.setMnemonic \Q)
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
  [{:keys [^JToolBar jtoolbar]
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

(defn host-game-process
  [{:keys [^JFrame root-jframe
           ^JFrame jframe
           ^String frequency
           yes|]
    :or {frequency (str (java.util.UUID/randomUUID))}
    :as opts}]
  (let [root-panel (JPanel.)
        jfrequency-text-field (JTextField. frequency 40)
        jbutton-yes (JButton. "yes")]

    (doto jframe
      (.add root-panel))

    (doto jbutton-yes
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! yes| {:op :host-yes
                       :frequency (.getText jfrequency-text-field)})
           (.dispose jframe)))))

    (doto root-panel
      (.setLayout (MigLayout. "insets 10"))
      (.add (JLabel. "frequency") "cell 0 0")
      (.add jfrequency-text-field "cell 1 0")
      (.add jbutton-yes "cell 0 1"))

    (.setPreferredSize jframe (Dimension. (* 0.8 (.getWidth root-jframe))
                                          (* 0.8 (.getHeight root-jframe))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
      (.pack)
      (.setLocationRelativeTo root-jframe)
      (.setVisible true)))
  nil)

(defn discover-process
  [{:keys [^JFrame root-jframe
           ^JFrame jframe
           ops|
           gamesA
           gameA
           stateA]
    :or {}
    :as opts}]
  (let [root-panel (JPanel.)
        jtable (JTable.)
        jscroll-pane (JScrollPane.)

        jbutton-host (JButton. "host")
        jbutton-join (JButton. "join")
        jbutton-observe (JButton. "observe")
        jbutton-leave (JButton. "leave")
        jbutton-open (JButton. "open")

        jtext-field-frequency (JTextField. (str (java.util.UUID/randomUUID)) 40)

        column-names (into-array ^Object ["frequency" "host"])
        table-model (DefaultTableModel.) #_(DefaultTableModel.
                                            ^"[[Ljava.lang.Object;"
                                            (to-array-2d
                                             [[(str (java.util.UUID/randomUUID)) 10]
                                              [(str (java.util.UUID/randomUUID)) 10]])
                                            ^"[Ljava.lang.Object;"
                                            (into-array ^Object ["frequency" "guests"])
                                            #_(object-array
                                               [(object-array)
                                                (object-array
                                                 [(str (java.util.UUID/randomUUID)) 10])]))]

    (doto jframe
      (.add root-panel))

    (doto jtable
      (.setModel table-model)
      (.setRowSelectionAllowed true)
      (.setSelectionModel (doto (DefaultListSelectionModel.)
                            (.addListSelectionListener
                             (reify ListSelectionListener
                               (valueChanged [_ event]
                                 (when (not= -1 (.getSelectedRow jtable))
                                   (SwingUtilities/invokeLater
                                    (reify Runnable
                                      (run [_]
                                        (.setText jtext-field-frequency (.getValueAt jtable (.getSelectedRow jtable) 0)))))))))))
      #_(.setAutoCreateRowSorter true))

    (doto jscroll-pane
      (.setViewportView jtable)
      (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

    (doto jbutton-host
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! ops| {:op :game
                       :role :host
                       :frequency (.getText jtext-field-frequency)})
           #_(.dispose jframe)))))

    (doto jbutton-join
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! ops| {:op :game
                       :role :player
                       :frequency (.getText jtext-field-frequency)})
           #_(.dispose jframe)))))

    (doto jbutton-leave
      (.addActionListener
       (reify ActionListener
         (actionPerformed [_ event]
           (put! ops| {:op :leave})
           #_(.dispose jframe)))))

    (doto root-panel
      (.setLayout (MigLayout. "insets 10"))
      (.add jscroll-pane "cell 0 0 3 1, width 100%")
      (.add jtext-field-frequency "cell 0 1")
      (.add jbutton-host "cell 0 2")
      (.add jbutton-join "cell 0 2")
      (.add jbutton-open "cell 0 2")
      (.add jbutton-leave "cell 0 2"))

    (.setPreferredSize jframe (Dimension. (* 0.8 (.getWidth root-jframe))
                                          (* 0.8 (.getHeight root-jframe))))

    (remove-watch gameA :discover-process)
    (add-watch gameA :discover-process
               (fn [ref wathc-key old-state new-state]
                 #_(println  :gameA old-state new-state)
                 #_(when (not= old-state new-state))
                 (SwingUtilities/invokeLater
                  (reify Runnable
                    (run [_]
                      (let [we-host? (= (:host-id new-state) (:peer-id @stateA))
                            in-game? (not (empty? new-state))]
                        (.setEnabled jbutton-open in-game?)
                        (.setEnabled jbutton-leave in-game?)
                        (.setEnabled jbutton-host (not in-game?))
                        (.setEnabled jbutton-join (not in-game?))))))))

    (remove-watch gamesA :discover-process)
    (add-watch gamesA :discover-process
               (fn [ref wathc-key old-state new-state]
                 (when (not= old-state new-state)
                   #_(println new-state)
                   (SwingUtilities/invokeLater
                    (reify Runnable
                      (run [_]
                        (let [selected-frequency (when (not= -1 (.getSelectedRow jtable))
                                                   (.getValueAt jtable (.getSelectedRow jtable) 0))
                              data (map (fn [[frequency {:keys [frequency host-peer-id]}]]
                                          [frequency host-peer-id]) new-state)]
                          (.setDataVector table-model
                                          ^"[[Ljava.lang.Object;"
                                          #_(to-array-2d
                                             [[(str (java.util.UUID/randomUUID)) 10]
                                              [(str (java.util.UUID/randomUUID)) 10]])
                                          (to-array-2d data)
                                          ^"[Ljava.lang.Object;"
                                          column-names)
                          (when selected-frequency
                            (let [^int new-index (->>
                                                  data
                                                  (into []
                                                        (comp
                                                         (map first)
                                                         (map-indexed vector)
                                                         (keep (fn [[index frequency]] (when (= frequency selected-frequency) index)))))
                                                  (first))]
                              (when new-index
                                (.setRowSelectionInterval jtable new-index new-index)))))))))))

    #_(go
        (loop []
          (when-let [value (<! table|)]
            (SwingUtilities/invokeLater
             (reify Runnable
               (run [_]
                 (.setDataVector table-model
                                 ^"[[Ljava.lang.Object;"
                                 (to-array-2d
                                  value)
                                 ^"[Ljava.lang.Object;"
                                 column-names))))
            (recur))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
      (.pack)
      (.setLocationRelativeTo root-jframe)
      (.setVisible true)))
  nil)

(defn settings-process
  [{:keys [^JFrame root-jframe
           ^JFrame jframe
           ops|
           settingsA]
    :or {}
    :as opts}]
  (let [root-panel (JPanel.)
        jscroll-pane (JScrollPane.)

        jcheckbox-apricotseed (JCheckBox.)]

    (doto jscroll-pane
      (.setViewportView root-panel)
      (.setHorizontalScrollBarPolicy ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER))

    (doto jframe
      (.add root-panel))

    (doto root-panel
      (.setLayout (MigLayout. "insets 10"))
      (.add (JLabel. ":apricotseed?") "cell 0 0")
      #_(.add jcheckbox-apricotseed "cell 0 0"))

    (.setPreferredSize jframe (Dimension. (* 0.8 (.getWidth root-jframe))
                                          (* 0.8 (.getHeight root-jframe))))

    (.addActionListener jcheckbox-apricotseed
                        (reify ActionListener
                          (actionPerformed [_ event]
                            (SwingUtilities/invokeLater
                             (reify Runnable
                               (run [_]
                                 (put! ops| {:op :settings-value
                                             :_ (.isSelected jcheckbox-apricotseed)})))))))

    (remove-watch settingsA :settings-process)
    (add-watch settingsA :settings-process
               (fn [ref wathc-key old-state new-state]
                 (SwingUtilities/invokeLater
                  (reify Runnable
                    (run [_]
                      (.setSelected jcheckbox-apricotseed (:apricotseed? new-state)))))))

    (doto jframe
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE #_WindowConstants/EXIT_ON_CLOSE)
      (.pack)
      (.setLocationRelativeTo root-jframe)
      (.setVisible true)))
  nil)

(defn draw-grid
  []
  (let [{:keys [^Canvas canvas
                ^Graphics2D graphics
                grid-rows
                grid-cols]
         :or {}} {:canvas canvas
                  :graphics graphics
                  :grid-rows grid-rows
                  :grid-cols grid-cols}

        row-height (/ (.getHeight canvas) grid-rows)
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


#_(defn print-ns-fns-docs
    [ns-sym]
    (go
      (let [fn-names (keys (ns-publics ns-sym))]
        (doseq [fn-name fn-names]
          (print (eval-form `(with-out-str (Wichita.repl/doc ~fn-name)) {}))))))

(defn print-ns-fns-docs
  [ns-sym eval|]
  (let [fn-names (keys (ns-publics ns-sym))]
    (doseq [fn-name fn-names]
      (put! eval| {:form `(Wichita.repl/doc ~fn-name)})
      #_(binding [*ns* (find-ns ns-sym)]
          (eval `(Wichita.repl/doc ~fn-name))))))

(defn console-output-stream ^ByteArrayOutputStream
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

    (let [out (PrintStream.
               (console-output-stream {:text-color Color/BLACK
                                       :print-stream System/out
                                       :append? remove-from-start?
                                       :jtext-component jtext-component})
               true)
          err (PrintStream.
               (console-output-stream {:text-color Color/BLACK
                                       :print-stream System/err
                                       :append? remove-from-start?
                                       :jtext-component jtext-component})
               true)]
      (System/setOut out)
      (System/setErr err)
      (alter-var-root #'*out* (constantly (OutputStreamWriter. out)))
      (alter-var-root #'*err* (constantly (PrintWriter.
                                           (OutputStreamWriter. err) true)))))
  nil)

(defn editor-process
  [{:keys [eval|
           ns-sym
           ^JPanel jcode-panel
           ^JTextArea jrepl
           ^JTextArea joutput
           ^JScrollPane joutput-scroll
           ^JEditorPane jeditor
           ^JScrollPane jeditor-scroll]
    :as opts}]
  (let []

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
      (.addKeyListener
       (reify KeyListener
         (keyPressed
           [_ event]
           (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
             (.consume ^KeyEvent event)))
         (keyReleased
           [_ event]
           (when (= (.getKeyCode ^KeyEvent event) KeyEvent/VK_ENTER)
             (let [string (-> (.getText jrepl) (clojure.string/trim) (clojure.string/trim-newline))]
               (when (not (empty? string))
                 (put! eval| {:string string})))))
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
      (binding [*ns* (find-ns ns-sym)]
        (loop []
          (when-let [{:keys [form string] :as value} (<! eval|)]
            (try
              (let [form (or form (read-string string))
                    ns-name (.getName *ns*)
                    done| (chan 1)]
                (SwingUtilities/invokeLater
                 (reify Runnable
                   (run [_]
                     (doto joutput
                       (.append (str ns-name "=> ")))
                     (doto joutput
                       (.append (str form))
                       (.append "\n"))
                     (put! done| true))))

                (let [_ (<! done|)
                      result (eval form)]
                  (SwingUtilities/invokeLater
                   (reify Runnable
                     (run [_]
                       (doto joutput
                         (.append (if (string? result) result (pr-str result)))
                         (.append "\n"))
                       (put! done| true)))))

                (<! done|)
                (SwingUtilities/invokeLater
                 (reify Runnable
                   (run [_]
                     (let [scrollbar (.getVerticalScrollBar joutput-scroll)]
                       (.setValue scrollbar (.getMaximum scrollbar))
                       (put! done| true)))))
                (<! done|))
              (catch Exception ex (println ex))
              (finally
                (SwingUtilities/invokeLater
                 (reify Runnable
                   (run [_]
                     (.setText jrepl ""))))))

            (recur)))))

    nil))

(defn -main
  [& args]
  (println ":_ Mandalorian isn't a race")
  (println ":Mando it's a Creed")
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
      (reset! settingsA {:apricotseed? true})

      (let [path-db (.getCanonicalPath ^File (Wichita.java.io/file data-dir-path "Deep-Thought"))]
        (Wichita.java.io/make-parents path-db)
        (let [config {:store {:backend :file :path path-db}
                      :keep-history? true
                      :name "main"}
              _ (when-not (Deep-Thought.api/database-exists? config)
                  (Deep-Thought.api/create-database config))
              conn (Deep-Thought.api/connect config)]

          (Deep-Thought.api/transact
           conn
           [{:db/cardinality :db.cardinality/one
             :db/ident :id
             :db/unique :db.unique/identity
             :db/valueType :db.type/uuid}
            {:db/ident :name
             :db/valueType :db.type/string
             :db/cardinality :db.cardinality/one}])

          (Deep-Thought.api/transact
           conn
           [{:id #uuid "3e7c14ce-5f00-4ac2-9822-68f7d5a60952"
             :name  "Deep-Thought"}
            {:id #uuid "f82dc4f3-59c1-492a-8578-6f01986cc4c2"
             :name  "Wichita"}
            {:id #uuid "5358b384-3568-47f9-9a40-a9a306d75b12"
             :name  "Little-Rock"}])

          (->>
           (Deep-Thought.api/q '[:find ?e ?n
                                 :where
                                 [?e :name ?n]]
                               @conn)
           (println))

          (->>
           (Deep-Thought.api/q '[:find [?ident ...]
                                 :where [_ :db/ident ?ident]]
                               @conn)
           (sort)
           (println)))))

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
                                         (componentShown [_ event]))))
              (.addWindowListener (proxy [WindowAdapter] []
                                    (windowClosing [event]
                                      (let [event ^WindowEvent event]
                                        #_(println :window-closing)
                                        (put! host| true)
                                        (-> event (.getWindow) (.dispose)))))))

            (doto jroot-panel
              #_(.setLayout (BoxLayout. jroot-panel BoxLayout/Y_AXIS))
              (.setLayout (MigLayout. "insets 10"
                                      "[grow,shrink,fill]"
                                      "[grow,shrink,fill]")))

            (when-let [url (Wichita.java.io/resource "icon.png")]
              (.setIconImage jframe (.getImage (ImageIcon. url))))

            (menubar-process
             {:jmenubar jmenubar
              :jframe jframe
              :menubar| ops|})
            (.setJMenuBar jframe jmenubar)

            #_(Cara-Dune.kiwis/toolbar-process
               {:jtoolbar jtoolbar})
            #_(.add jroot-panel jtoolbar "dock north")

            (editor-process
             {:ns-sym 'Cara-Dune.main
              :eval| eval|
              :jcode-panel jcode-panel
              :jrepl jrepl
              :joutput joutput
              :joutput-scroll joutput-scroll
              :jeditor jeditor
              :jeditor-scroll jeditor-scroll})
            (.add jroot-panel jcode-panel "dock west")

            (canvas-process
             {:jcanvas-panel jcanvas-panel
              :canvas canvas})
            (.add jroot-panel jcanvas-panel "dock east,width 50%:100%:100%, height 1:100%:")

            (.setPreferredSize jframe
                               (let [size (-> (Toolkit/getDefaultToolkit) (.getScreenSize))]
                                 (Dimension. (* 0.7 (.getWidth size)) (* 0.7 (.getHeight size)))
                                 #_(Dimension. (UIScale/scale 1024) (UIScale/scale 576)))
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
                              (if (:apricotseed? @settingsA)
                                (do nil)
                                (do nil))
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
                  (Cara-Dune.salt/print-ns-fns-docs 'Cara-Dune.main eval|))))))


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
        (when-let [value (<! ops|)]
          (condp = (:op value)

            :discover
            (let [discover-jframe (JFrame. "discover")]
              (discover-process
               {:jframe discover-jframe
                :root-jframe jframe
                :ops| ops|
                :gamesA gamesA
                :gameA gameA
                :stateA stateA})
              (reset! gameA @gameA))

            :settings
            (let [settings-jframe (JFrame. "settings")]
              (settings-process
               {:jframe settings-jframe
                :root-jframe jframe
                :ops| ops|
                :settingsA settingsA})
              (reset! settingsA @settingsA))

            :settings-value
            (let []
              (swap! settingsA merge value)))

          (recur))))
    
    )
  (println "Kuiil has spoken"))


(comment

  (.getName (class (make-array Object 1 1)))

  (.getName (class (make-array String 1)))

  ;
  )

(comment

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
                id| (chan 1)
                port (or (System/getenv "Jar_Jar_IPFS_PORT") "5001")
                ipfs-api-url (format "http://127.0.0.1:%s" port)
                games-topic (encode-base64url-u "raisins")
                game-topic (encode-base64url-u frequency)
                _ (subscribe-process
                   {:sub| sub|
                    :cancel| cancel-sub|
                    :frequency frequency
                    :ipfs-api-url ipfs-api-url
                    :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
                    :id| id|})
                host? (= role :host)
                {:keys [peer-id]} (<! id|)]
            #_(println :game value)
            (go
              (loop []
                (alt!
                  cancel-pub|
                  ([_] (do nil))

                  (timeout 2000)
                  ([_]
                   (when host?
                     (pubsub-pub
                      ipfs-api-url games-topic (str {:op :games
                                                     :timestamp (.getTime (java.util.Date.))
                                                     :frequency frequency
                                                     :host-peer-id peer-id}))
                     (pubsub-pub
                      ipfs-api-url game-topic (str {:op :game-state
                                                    :timestamp (.getTime (java.util.Date.))
                                                    :game-state {:host-peer-id peer-id}})))

                   (pubsub-pub
                    ipfs-api-url game-topic (str {:op :player-state
                                                  :timestamp (.getTime (java.util.Date.))
                                                  :peer-id peer-id}))
                   (recur))))))

          :leave
          (let [{:keys [frequency]} value]
            (>! cancel-sub| true)
            (>! cancel-pub| true)
            (reset! gameA {}))

          :discover
          (let [discover-jframe (JFrame. "discover")]
            (discover-process
             {:jframe discover-jframe
              :root-jframe jframe
              :ops| ops|
              :gamesA gamesA
              :gameA gameA
              :stateA stateA})
            (reset! gameA @gameA))

          :settings
          (let [settings-jframe (JFrame. "settings")]
            (settings-process
             {:jframe settings-jframe
              :root-jframe jframe
              :ops| ops|
              :settingsA settingsA})
            (reset! settingsA @settingsA))

          :settings-value
          (let []
            (swap! settingsA merge value))

          :host-yes
          (let [{:keys [frequency]} value]
            (println :frequency frequency)))

        (recur))))



  (let [port (or (System/getenv "Jar_Jar_IPFS_PORT") "5001")
        ipfs-api-url (format "http://127.0.0.1:%s" port)
        id| (chan 1)
        raw-stream-connection-pool (Simba.http/connection-pool {:connection-options {:raw-stream? true}})]

    (alter-var-root #'raw-stream-connection-pool (constantly raw-stream-connection-pool))
    (subscribe-process
     {:sub| sub|
      :raw-stream-connection-pool raw-stream-connection-pool
      :cancel| (chan (sliding-buffer 1))
      :frequency "raisins"
      :ipfs-api-url ipfs-api-url
      :ipfs-api-multiaddress (format "/ip4/127.0.0.1/tcp/%s" port)
      :id| id|}))

  ;
  )