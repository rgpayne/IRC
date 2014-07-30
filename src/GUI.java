import org.apache.commons.lang3.StringUtils;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class GUI extends JFrame {
    final static ImageIcon mainIcon = new ImageIcon("src/icons/weather-sun.png");
    final static ImageIcon quickConnectIcon = new ImageIcon("src/icons/connect.png");
    final static ImageIcon identityIcon = new ImageIcon("src/icons/edit-group.png");
    final static ImageIcon serverListIcon = new ImageIcon("src/icons/unsortedlist1.png");
    final static ImageIcon copyIcon = new ImageIcon("src/icons/edit-copy-3.png");
    final static ImageIcon cutIcon = new ImageIcon("src/icons/edit-cut-red.png");
    final static ImageIcon pasteIcon = new ImageIcon("src/icons/edit-paste-7.png");
    final static ImageIcon prevTabIcon = new ImageIcon("src/icons/go-previous.png");
    final static ImageIcon nextTabIcon = new ImageIcon("src/icons/go-next.png");
    final static ImageIcon moveTabLeftIcon = new ImageIcon("src/icons/go-previous-3.png");
    final static ImageIcon moveTabRightIcon = new ImageIcon("src/icons/go-next-3.png");
    final static ImageIcon closeTabIcon = new ImageIcon("src/icons/tab-close-2.png");
    final static ImageIcon disconnectIcon = new ImageIcon("src/icons/disconnect.png");
    final static ImageIcon reconnectIcon = new ImageIcon("src/icons/database-connect.png");
    final static ImageIcon globalAwayIcon = new ImageIcon("src/icons/im-user-away.png");
    final static ImageIcon joinChannelIcon = new ImageIcon("src/icons/irc-join-channel.png");
    final static ImageIcon quitProgramIcon = new ImageIcon("src/icons/document-close.png");
    final static ImageIcon showNicklistIcon = new ImageIcon("src/icons/preferences-system-windows-move.png");
    final static ImageIcon popupIgnoreIcon = new ImageIcon("src/icons/gg_ignored.png");
    final static ImageIcon popupQueryIcon = new ImageIcon("src/icons/window-new.png");
    final static ImageIcon popupWhoisIcon = new ImageIcon("src/icons/xwhois.png");
    final static ImageIcon popupPingIcon = new ImageIcon("src/icons/network-transmit-2.png");
    final static ImageIcon popupVersionIcon = new ImageIcon("src/icons/help-about.png");
    final static ImageIcon channelListIcon = new ImageIcon("src/icons/view-list-details-5.png");
    final static ImageIcon checkedBoxIcon = new ImageIcon("src/icons/checkbox-2.png");
    final static ImageIcon findTextIcon = new ImageIcon("src/icons/system-search-5.png");
    final static ImageIcon configureIcon = new ImageIcon("src/icons/configure-5.png");
    final static ImageIcon selectAllIcon = new ImageIcon("src/icons/edit-select-all.png");
    final static ImageIcon clearTextIcon = new ImageIcon("src/icons/edit-clear.png");
    final static ImageIcon newMessageIcon = new ImageIcon("src/icons/emblem-important.png");
    final static ImageIcon notConnectedIcon = new ImageIcon("src/icons/notConnectedIcon.png");


    static Color CTCP0 = Color.decode("#FAFAF8"), CTCP1 = Color.decode("#000000"), CTCP2 = Color.decode("#000080"), CTCP3 = Color.decode("#008000"), CTCP4 = Color.decode("#FF0000"),
            CTCP5 = Color.decode("#A52A2A"), CTCP6 = Color.decode("#800080"), CTCP7 = Color.decode("#FF8000"), CTCP8 = Color.decode("#808000"),
            CTCP9 = Color.decode("#00FF00"), CTCP10 = Color.decode("#008080"), CTCP11 = Color.decode("#00FFFF"), CTCP12 = Color.decode("#0000FF"),
            CTCP13 = Color.decode("#FFC0CB"), CTCP14 = Color.decode("#A0A0A0"), CTCP15 = Color.decode("#C0C0C0"),
            highlightColor = Color.pink;
    static Color chatColor0 = Color.decode("#E90E7F"), chatColor1 = Color.decode("#B30E0E"), chatColor2 = Color.decode("#8E55E9"), chatColor3 = Color.decode("#18B33C"),
            chatColor4 = Color.decode("#58ADB3"), chatColor5 = Color.decode("#9E54B3"), chatColor6 = Color.decode("#B39875"), chatColor7 = Color.decode("#3465A4"),
            chatColor8 = Color.decode("#CE5C00"), chatColor9 = Color.decode("#555753");

    static StyleContext sc;
    static Style style, chatStyle, timestampStyle, actionStyle, errorStyle, serverStyle, connectStyle, ctcpStyle, userNameStyle,
            disconnectStyle, joinStyle, hyperlinkUnclickedStyle, highlightStyle;
    static String font = "Courier New";
    static String fontStyle = "Plain";
    static int fontSize = 12;
    final static String errorColor = "#FF0000", chatColor = "#000000", serverColor = "#960096", connectColor = "#993300", timestampColor = "#909090";
    final static String actionColor = "#0000FF", disconnectColor = "#CAA234", joinColor = "#D46942";
    final static Map<Integer, Color> CTCPMap = new HashMap<>();
    final static Map<Integer, Color> chatColorMap = new HashMap<>();


    final static String appName = "Alpha IRC";
    final static Properties prop = new Properties();
    public static JTextField chatInputPane;
    static ArrayList<SavedConnection> savedConnections = new ArrayList<>();
    static JFrame frame;
    private static JLabel tabInfo;
    private static DnDTabbedPane tabbedPane;
    private static JMenuItem findTextAction;
    private static JMenuItem copyAction;
    private static JMenuItem cutAction;
    private static JMenuItem pasteAction;
    private static JMenuItem quickConnect;
    private static JMenuItem identity;
    private static JMenuItem serverList;
    private static JMenuItem clearWindow;
    private static JMenuItem clearAllWindows;
    private static JMenuItem showNickList;
    private static JMenuItem previousTab;
    private static JMenuItem nextTab;
    private static JMenuItem moveTabLeft;
    private static JMenuItem moveTabRight;
    private static JMenuItem closeTab;
    private static JMenuItem channelList;
    private static JMenuItem disconnect;
    private static JMenuItem reconnect;
    private static JMenuItem globalAway;
    private static JMenuItem joinChannel;
    private static JMenuItem quitProgram;
    private static JMenuItem configure;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu settingsMenu;
    private JMenu windowMenu;
    private JMenuBar menuBar;
    private JTextPane userListPane;



    public GUI() {
        super(appName);
        setIconImage(mainIcon.getImage());
        setStyles();
        makeHashMaps();
        loadProperties();
        initComponents();

        initActions();
        initMainwindowListeners();

        loadKeyBinds(chatInputPane.getActionMap(), chatInputPane.getInputMap());
        ChannelPanel.tabbedPane = tabbedPane;
        Connection.tabbedPane = tabbedPane;
        ChannelPanel.tabInfo = tabInfo;
        Connection.tabInfo = tabInfo;
        autoConnect();
        frame = GUI.this;
    }



    /** Maps menu items to their respective actions and shortcut keys*/
    public static void loadKeyBinds(ActionMap amap, InputMap imap) {
        amap.put("showNickList", showNickList.getAction());
        amap.put("globalAway", globalAway.getAction());
        amap.put("quitProgram", quitProgram.getAction());
        amap.put("joinChannel", joinChannel.getAction());
        amap.put("clearWindow", clearWindow.getAction());
        amap.put("clearAllWindows", clearAllWindows.getAction());
        amap.put("previousTab", previousTab.getAction());
        amap.put("nextTab", nextTab.getAction());
        amap.put("moveTabLeft", moveTabLeft.getAction());
        amap.put("moveTabRight", moveTabRight.getAction());
        amap.put("closeTab", closeTab.getAction());
        amap.put("channelList", channelList.getAction());
        amap.put("disconnect", disconnect.getAction());
        amap.put("reconnect", reconnect.getAction());
        amap.put("quickConnect", quickConnect.getAction());
        amap.put("identity", identity.getAction());
        amap.put("serverList", serverList.getAction());
        amap.put("findText", findTextAction.getAction());

        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "showNickList");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK), "globalAway");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "quitProgram");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "joinChannel");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "clearWindow");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK), "clearAllWindows");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.SHIFT_MASK), "previousTab");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.SHIFT_MASK), "nextTab");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "closeTab");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "moveTabLeft");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "moveTabRight");
        imap.put(KeyStroke.getKeyStroke("F5"), "channelList");
        imap.put(KeyStroke.getKeyStroke("F7"), "quickConnect");
        imap.put(KeyStroke.getKeyStroke("F8"), "identy");
        imap.put(KeyStroke.getKeyStroke("F2"), "serverList");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "findText");
    }



    /** Writes saved connections to a file for serialization */
    public static void serializeSavedConnections() {
        String filename = "servers.ser";
        FileOutputStream fos;
        ObjectOutputStream out;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(savedConnections);

            out.close();
        } catch (IOException ignored) {}
    }



    /** Loads serialized serer file for use */
    public static void deserializeSavedConnections() {
        FileInputStream fis;
        ObjectInputStream in;
        try {
            fis = new FileInputStream("servers.ser");
            in = new ObjectInputStream(fis);
            savedConnections = (ArrayList<SavedConnection>) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("Deserialization exception");
        }
    }



    /**
     * Swing menus are looking pretty bad on Linux when the GTK LaF is used (See
     * bug #6925412). It will most likely never be fixed anytime soon so this
     * method provides a workaround for it. It uses reflection to change the GTK
     * style objects of Swing so popup menu borders have a minimum thickness of
     * 1 and menu separators have a minimum vertical thickness of 1.
     */
    public static void installGtkPopupBugWorkaround() {
        // Get current look-and-feel implementation class
        LookAndFeel laf = UIManager.getLookAndFeel();
        Class<?> lafClass = laf.getClass();

        // Do nothing when not using the problematic LaF
        if (!lafClass.getName().equals(
                "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) return;

        // We do reflection from here on. Failure is silently ignored. The
        // workaround is simply not installed when something goes wrong here
        try {
            // Access the GTK style factory
            Field field = lafClass.getDeclaredField("styleFactory");
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object styleFactory = field.get(laf);
            field.setAccessible(accessible);

            // Fix the horizontal and vertical thickness of popup menu style
            Object style = getGtkStyle(styleFactory, new JPopupMenu(),
                    "POPUP_MENU");
            fixGtkThickness(style, "yThickness");
            fixGtkThickness(style, "xThickness");

            // Fix the vertical thickness of the popup menu separator style
            style = getGtkStyle(styleFactory, new JSeparator(),
                    "POPUP_MENU_SEPARATOR");
            fixGtkThickness(style, "yThickness");
        } catch (Exception e) {
            // Silently ignored. Workaround can't be applied.
        }
    }



    /**
     * Called internally by installGtkPopupBugWorkaround to fix the thickness
     * of a GTK style field by setting it to a minimum value of 1.
     *
     * @param style     The GTK style object.
     * @param fieldName The field name.
     * @throws Exception When reflection fails.
     */
    private static void fixGtkThickness(Object style, String fieldName) throws Exception {
        Field field = style.getClass().getDeclaredField(fieldName);
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.setInt(style, Math.max(1, field.getInt(style)));
        field.setAccessible(accessible);
    }




    /**
     * Called internally by installGtkPopupBugWorkaround. Returns a specific
     * GTK style object.
     *
     * @param styleFactory The GTK style factory.
     * @param component    The target component of the style.
     * @param regionName   The name of the target region of the style.
     * @return The GTK style.
     * @throws Exception When reflection fails.
     */
    private static Object getGtkStyle(Object styleFactory,
                                      JComponent component, String regionName) throws Exception {
        // Create the region object
        Class<?> regionClass = Class.forName("javax.swing.plaf.synth.Region");
        Field field = regionClass.getField(regionName);
        Object region = field.get(regionClass);

        // Get and return the style
        Class<?> styleFactoryClass = styleFactory.getClass();
        Method method = styleFactoryClass.getMethod("getStyle",
                new Class<?>[]{JComponent.class, regionClass});
        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Object style = method.invoke(styleFactory, component, region);
        method.setAccessible(accessible);
        return style;
    }




    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                installGtkPopupBugWorkaround();
                new GUI().setVisible(true);
            }
        });
    }





    private void initComponents() {
        chatInputPane = new JTextField();
        tabbedPane = new DnDTabbedPane();
        userListPane = new JTextPane();
        tabInfo = new JLabel();
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        windowMenu = new JMenu("Window");
        windowMenu.setMnemonic('W');
        settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic('S');
        findTextAction = new JMenuItem();
        copyAction = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyAction.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        copyAction.setText("Copy");
        copyAction.setIcon(copyIcon);
        editMenu.add(findTextAction);
        editMenu.add(copyAction);
        cutAction = new JMenuItem((new DefaultEditorKit.CutAction()));
        cutAction.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        cutAction.setText("Cut");
        cutAction.setIcon(cutIcon);
        editMenu.add(cutAction);
        pasteAction = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteAction.setAccelerator(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        pasteAction.setText("Paste");
        pasteAction.setIcon(pasteIcon);
        editMenu.add(pasteAction);
        editMenu.add(new JSeparator());
        clearWindow = new JMenuItem();
        editMenu.add(clearWindow);
        clearAllWindows = new JMenuItem();
        editMenu.add(clearAllWindows);
        previousTab = new JMenuItem();
        windowMenu.add(previousTab);
        nextTab = new JMenuItem();
        windowMenu.add(nextTab);
        windowMenu.add(new JSeparator());
        moveTabLeft = new JMenuItem();
        windowMenu.add(moveTabLeft);
        moveTabRight = new JMenuItem();
        windowMenu.add(moveTabRight);
        closeTab = new JMenuItem();
        windowMenu.add(closeTab);
        windowMenu.add(new JSeparator());
        channelList = new JMenuItem();
        windowMenu.add(channelList);
        serverList = new JMenuItem();
        fileMenu.add(serverList);
        quickConnect = new JMenuItem();
        fileMenu.add(quickConnect);
        fileMenu.add(new JSeparator());
        disconnect = new JMenuItem();
        fileMenu.add(disconnect);
        identity = new JMenuItem();
        showNickList = new JMenuItem();
        configure = new JMenuItem();
        settingsMenu.add(showNickList);
        settingsMenu.add(identity);
        settingsMenu.add(configure);
        reconnect = new JMenuItem();
        fileMenu.add(reconnect);
        joinChannel = new JMenuItem();
        fileMenu.add(joinChannel);
        fileMenu.add(new JSeparator());
        globalAway = new JMenuItem();
        fileMenu.add(globalAway);
        fileMenu.addSeparator();
        quitProgram = new JMenuItem();
        fileMenu.add(quitProgram);

        chatInputPane.setFocusTraversalKeysEnabled(false);


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        tabbedPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabbedPane.setTabLayoutPolicy(DnDTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(DnDTabbedPane.BOTTOM);
        tabbedPane.setCursor(new java.awt.Cursor(Cursor.DEFAULT_CURSOR)); //cursor necessary?
        tabbedPane.setFocusable(false);
        tabbedPane.setPreferredSize(new Dimension(600, 450));

        userListPane.setEditable(false);
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new Dimension(25, 25));

        tabInfo.setHorizontalAlignment(SwingConstants.RIGHT);

        menuBar.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        menuBar.setFocusable(false);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(settingsMenu);
        menuBar.add(windowMenu);


        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(chatInputPane, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE)
                        .addComponent(tabInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chatInputPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tabInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pack();
    }




    /** Initializizes actions and sets menu items to their respective actions */
    private void initActions()
    {
        findTextAction.setAction(FindTextAction.getInstance());
        findTextAction.setAccelerator(KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        quitProgram.setAction(QuitProgramAction.getInstance());
        quitProgram.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        joinChannel.setAction(JoinChannelAction.getInstance());
        joinChannel.setAccelerator(KeyStroke.getKeyStroke('J', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        clearWindow.setAction(ClearWindowAction.getInstance());
        clearWindow.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        clearAllWindows.setAction(ClearAllWindowsAction.getInstance());
        clearAllWindows.setDisplayedMnemonicIndex(6);
        clearAllWindows.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK, false));
        previousTab.setAction(PreviousTabAction.getInstance());
        previousTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.SHIFT_MASK, false));
        nextTab.setAction(NextTabAction.getInstance());
        nextTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.SHIFT_MASK, false));
        moveTabLeft.setAction(MoveTabLeftAction.getInstance());
        moveTabLeft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        moveTabRight.setAction(MoveTabRightAction.getInstance());
        moveTabRight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        closeTab.setAction(CloseTabAction.getInstance());
        closeTab.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        globalAway.setAction(GlobalAwayAction.getInstance());
        globalAway.setDisplayedMnemonicIndex(7);
        disconnect.setAction(DisconnectAction.getInstance());
        reconnect.setAction(ReconnectAction.getInstance());
        quickConnect.setAction(QuickConnectAction.getInstance());
        quickConnect.setAccelerator(KeyStroke.getKeyStroke("F7"));
        showNickList.setAction(ShowNickListAction.getInstance());
        showNickList.setAccelerator(KeyStroke.getKeyStroke('H', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        identity.setAction(IdentityAction.getInstance());
        identity.setAccelerator(KeyStroke.getKeyStroke("F2"));
        configure.setAction(ConfigureAction.getInstance());
        serverList.setAction(ServerListAction.getInstance());
        serverList.setAccelerator(KeyStroke.getKeyStroke("F2"));
        channelList.setAction(ChannelListAction.getInstance());
        channelList.setDisplayedMnemonicIndex(8);
        channelList.setAccelerator(KeyStroke.getKeyStroke("F5"));
    }




    /** Sets up input key listener, tabbedPane container listener and change listener*/
    private void initMainwindowListeners()
    {
        chatInputPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt)
            {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                if (channel == null) return;

                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (chatInputPane.getText().equals("")) return;
                    try {
                        String msg = chatInputPane.getText();
                        String target = channel.name;
                        channel.history.add(msg);
                        String output = "PRIVMSG " + target + " :" + msg;
                        channel.historyCounter = channel.history.size();

                        if (msg.charAt(0) != '/') {
                            channel.connection.send(output);
                            String[] inp = {Connection.currentNick, msg.trim()};
                            channel.insertString(inp, chatStyle, false);
                            chatInputPane.setText(null);
                            evt.consume();
                            return;
                        } else {
                            channel.connection.send(msg.substring(1));
                            chatInputPane.setText(null);
                            evt.consume();
                        }

                    } catch (IOException e) {
                        try {
                            String[] msg = {null, "[Error] *** You are not connected to the server."};
                            channel.insertString(msg, errorStyle, false);
                            chatInputPane.setText(null);
                            evt.consume();
                        } catch (BadLocationException | IOException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (BadLocationException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    if (channel.history.size() <= 0) return;
                    String msg = "";
                    if (channel.historyCounter > 0) msg = channel.history.get(channel.historyCounter - 1);
                    chatInputPane.setText(msg);
                    if (channel.historyCounter > 0) channel.historyCounter--;
                }
                if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (channel.historyCounter == channel.history.size()) return;
                    String msg = "";
                    if (channel.historyCounter < channel.history.size())
                        msg = channel.history.get(channel.historyCounter);
                    chatInputPane.setText(msg);
                    if (channel.historyCounter < channel.history.size()) channel.historyCounter++;
                }
                if (evt.getKeyCode() == KeyEvent.VK_TAB) {
                    String a = chatInputPane.getText();
                    int lastSpace = a.lastIndexOf(" ") + 1;
                    if (lastSpace == -1) lastSpace = 0;
                    String nickStart = a.substring(lastSpace);
                    System.out.println(nickStart);

                    if (ChannelPanel.tabNicks == null || ChannelPanel.tabNicks.isEmpty())
                        ChannelPanel.tabNicks = channel.getMatchingUsers(nickStart);

                    String tabNick;
                    if (ChannelPanel.tabNicks.size() > 0) {
                        tabNick = ChannelPanel.tabNicks.get(0);
                        ChannelPanel.tabNicks.remove(tabNick);
                    } else return;

                    chatInputPane.setText(chatInputPane.getText().substring(0, lastSpace) + tabNick);
                    return;
                }
                if (ChannelPanel.tabNicks != null) ChannelPanel.tabNicks.clear();
            }
        });




        /** Listens for tabbedPane changes and updates components based on position of active tab*/
        ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (ChannelPanel.tabNicks != null && !ChannelPanel.tabNicks.isEmpty()) ChannelPanel.tabNicks.clear();
                DnDTabbedPane pane = (DnDTabbedPane) changeEvent.getSource();
                int index = pane.getSelectedIndex();
                if (index == -1) return;
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(index);
                if (!channel.connection.isConnected) pane.setForegroundAt(index, Color.gray);
                else pane.setForegroundAt(index, Color.BLACK);
                if (index == 0) {
                    moveTabLeft.setForeground(Color.gray);
                    previousTab.setForeground(Color.gray);
                } else {
                    moveTabLeft.setForeground(Color.black);
                    previousTab.setForeground(Color.black);
                }
                if (index == tabbedPane.getTabCount() - 1) {
                    moveTabRight.setForeground(Color.gray);
                    nextTab.setForeground(Color.gray);
                } else {
                    moveTabRight.setForeground(Color.black);
                    nextTab.setForeground(Color.black);
                }
                channel.updateTabInfo();
            }
        };
        tabbedPane.addChangeListener(changeListener);



        /** Listens for additions to tabbedPane and updates components based on position of active tab*/
        ContainerListener containerListener = new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index == -1) return;

                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(index);

                if (index == 0) {
                    moveTabLeft.setForeground(Color.gray);
                    previousTab.setForeground(Color.gray);
                } else {
                    moveTabLeft.setForeground(Color.black);
                    previousTab.setForeground(Color.black);
                }
                if (index == tabbedPane.getTabCount() - 1) {
                    moveTabRight.setForeground(Color.gray);
                    nextTab.setForeground(Color.gray);
                } else {
                    moveTabRight.setForeground(Color.black);
                    nextTab.setForeground(Color.black);
                }
                channel.updateTabInfo();
            }
            @Override
            public void componentRemoved(ContainerEvent e) {

                int index = tabbedPane.getSelectedIndex();
                if (index == -1) {
                    setTitle(GUI.appName);
                    return;
                }
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(index);

                if (index == 0) {
                    moveTabLeft.setForeground(Color.gray);
                    previousTab.setForeground(Color.gray);
                } else {
                    moveTabLeft.setForeground(Color.black);
                    previousTab.setForeground(Color.black);
                }
                if (index == tabbedPane.getTabCount() - 1) {
                    moveTabRight.setForeground(Color.gray);
                    nextTab.setForeground(Color.gray);
                } else {
                    moveTabRight.setForeground(Color.black);
                    nextTab.setForeground(Color.black);
                }
                channel.updateTabInfo();
            }
        };
        tabbedPane.addContainerListener(containerListener);
    }





    /** Sets up the text styles used by the chatPane */
    public static void setStyles() {
        sc = StyleContext.getDefaultStyleContext();
        style = sc.addStyle("DefaultStyle", null);
        chatStyle = sc.addStyle("DefaultStyle", style);
        timestampStyle = sc.addStyle("DefaultStyle", style);
        actionStyle = sc.addStyle("Defaultstyle", style);
        errorStyle = sc.addStyle("DefaultStyle", style);
        serverStyle = sc.addStyle("Defaultstyle", style);
        connectStyle = sc.addStyle("Defaultstyle", style);
        ctcpStyle = sc.addStyle("Defaultstyle", style);
        userNameStyle = sc.addStyle("Defaultstyle", style);
        disconnectStyle = sc.addStyle("Defaultstyle", style);
        joinStyle = sc.addStyle("Defaultstyle", style);
        hyperlinkUnclickedStyle = sc.addStyle("Hyperlink", null);
        highlightStyle = sc.addStyle("Highlight", null);

        StyleConstants.setFontFamily(style, font);
        StyleConstants.setFontSize(style, fontSize);

        StyleConstants.setForeground(chatStyle, Color.decode(chatColor));
        StyleConstants.setForeground(timestampStyle, Color.decode(timestampColor));
        StyleConstants.setForeground(actionStyle, Color.decode(actionColor));
        StyleConstants.setForeground(errorStyle, Color.decode(errorColor));
        StyleConstants.setForeground(serverStyle, Color.decode(serverColor));
        StyleConstants.setForeground(connectStyle, Color.decode(connectColor));
        StyleConstants.setForeground(disconnectStyle, Color.decode(disconnectColor));
        StyleConstants.setForeground(joinStyle, Color.decode(joinColor));
        StyleConstants.setForeground(hyperlinkUnclickedStyle, (Color.blue));
        StyleConstants.setFontFamily(hyperlinkUnclickedStyle, font);
        StyleConstants.setFontSize(hyperlinkUnclickedStyle, 12);
        StyleConstants.setUnderline(hyperlinkUnclickedStyle, true);
        StyleConstants.setFontFamily(highlightStyle, font);
        StyleConstants.setFontSize(highlightStyle, 12);
        StyleConstants.setBackground(highlightStyle, Color.YELLOW);
    }




    /** Maps colors used in the chatPane  */
    public static void makeHashMaps() {

        //Colors used by CTCP actions
        CTCPMap.put(0, CTCP0);
        CTCPMap.put(1, CTCP1);
        CTCPMap.put(2, CTCP2);
        CTCPMap.put(3, CTCP3);
        CTCPMap.put(4, CTCP4);
        CTCPMap.put(5, CTCP5);
        CTCPMap.put(6, CTCP6);
        CTCPMap.put(7, CTCP7);
        CTCPMap.put(8, CTCP8);
        CTCPMap.put(9, CTCP9);
        CTCPMap.put(10, CTCP10);
        CTCPMap.put(11, CTCP11);
        CTCPMap.put(12, CTCP12);
        CTCPMap.put(13, CTCP13);
        CTCPMap.put(14, CTCP14);
        CTCPMap.put(15, CTCP15);

        /**
        Maps the colors used for nicks when a user types in chat.
        When a user types he is assigned a random color from this map
        */
        chatColorMap.put(0, chatColor0);
        chatColorMap.put(1, chatColor1);
        chatColorMap.put(2, chatColor2);
        chatColorMap.put(3, chatColor3);
        chatColorMap.put(4, chatColor4);
        chatColorMap.put(5, chatColor5);
        chatColorMap.put(6, chatColor6);
        chatColorMap.put(7, chatColor7);
        chatColorMap.put(8, chatColor8);
        chatColorMap.put(9, chatColor9);
    }




    /** Loads the settings file and initializes the values  */
    private void loadProperties() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");
            prop.load(input);

            Connection.real = prop.getProperty("Real");
            Connection.nicks[0] = prop.getProperty("Nick");
            Connection.currentNick = Connection.nicks[0];
            Connection.nicks[1] = prop.getProperty("Second");
            Connection.nicks[2] = prop.getProperty("Third");
            deserializeSavedConnections();
        } catch (IOException ex) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }
    }




    /** Run on GUI initialization. Connects to servers saved to savedConnections if the server's autoConnect field is true */
    private void autoConnect() {
        for (SavedConnection conn : savedConnections) {
            if (conn.retrieveAutoConnect()) {
                new Connection(conn.retrieveName(), conn.retrieveServer(), conn.retrievePort(), true);
            }
        }
    }



    /** Creates a dialog that shows a list of all channels on currently selected tab's server*/
    static class ChannelListAction extends AbstractAction {
        private static ChannelListAction ref = null;

        private ChannelListAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ChannelListAction getInstance() {
            if (ref == null) ref = new ChannelListAction("Channel List", channelListIcon, null, KeyEvent.VK_C);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();

            JDialog dialog = new JDialog(frame, "Channel List - " + channel.connection.title, true);
            SpringLayout layout = new SpringLayout();
            JPanel panel = new JPanel(layout);

            dialog.add(panel);
            dialog.setPreferredSize(new Dimension(600, 400));

            Container contentpane = dialog.getContentPane();
            contentpane.setLayout(layout);
            final BeanTableModel<ListChannel> model = new BeanTableModel<>(ListChannel.class);
            model.sortColumnNames();
            for (int i = 0; i < channel.connection.channelList.size(); i++)
                model.addRow(channel.connection.channelList.get(i));

            JButton joinButton = new JButton("Join Channel");
            JButton refreshList = new JButton("Refresh List");
            final JTextArea filterArea = new JTextArea();
            filterArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);



            final JTable table = new JTable(model) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
                /** Wraps tooltips to multiple lines*/
                @Override
                public String getToolTipText(MouseEvent e) {
                    Point p = e.getPoint();
                    int rowIndex = convertRowIndexToModel(rowAtPoint(p));

                    String tip = (String) getModel().getValueAt(rowIndex, 2);
                    boolean split = false;
                    int counter = 0;
                    for (int i = 0; i < tip.length(); i++) {
                        if (counter > 80) {
                            if (tip.charAt(i) == ' ') {
                                split = true;
                                counter = 0;
                            }
                        }
                        if (split || counter > 100) {
                            String beg = tip.substring(0, i);
                            String end = tip.substring(i, tip.length());
                            tip = beg + "<br>" + end; //HTML break
                            i = i + 4;
                            counter = 0;
                            split = false;
                        }
                        counter++;
                    }
                    return "<HTML>" + tip + "</HTML>";
                }
            };




            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
            table.setShowGrid(false);
            table.setAutoCreateRowSorter(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(0).setMinWidth(50);
            table.getColumnModel().getColumn(0).setMaxWidth(50);
            table.getColumnModel().getColumn(0).setResizable(false);

            DefaultTableCellRenderer left = new DefaultTableCellRenderer();
            left.setHorizontalAlignment(SwingConstants.LEFT);
            table.getColumnModel().getColumn(0).setCellRenderer(left);

            table.getColumnModel().getColumn(1).setPreferredWidth(130);
            table.getColumnModel().getColumn(1).setMaxWidth(250);
            table.getColumnModel().getColumn(1).setMinWidth(130);

            table.getColumnModel().getColumn(2).setPreferredWidth(300);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setViewportView(table);

            contentpane.add(scrollPane);
            contentpane.add(joinButton);
            contentpane.add(refreshList);
            contentpane.add(filterArea);
            filterArea.setBorder(BorderFactory.createLineBorder(Color.gray));

            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getViewport().setBackground(Color.white);

            layout.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, contentpane);
            layout.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, contentpane);
            layout.putConstraint(SpringLayout.NORTH, scrollPane, 5, SpringLayout.NORTH, contentpane);
            layout.putConstraint(SpringLayout.SOUTH, scrollPane, -40, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.EAST, joinButton, -7, SpringLayout.EAST, contentpane);
            layout.putConstraint(SpringLayout.SOUTH, joinButton, -8, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.SOUTH, refreshList, -8, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.EAST, refreshList, -7, SpringLayout.WEST, joinButton);
            layout.putConstraint(SpringLayout.SOUTH, filterArea, -10, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.WEST, filterArea, 7, SpringLayout.WEST, contentpane);
            layout.putConstraint(SpringLayout.EAST, filterArea, -10, SpringLayout.WEST, refreshList);
            layout.putConstraint(SpringLayout.NORTH, filterArea, 10, SpringLayout.SOUTH, scrollPane);


            /** Filters channel results based on user input */
            final RowFilter<Object, Object> filter = new RowFilter<Object, Object>() {
                @Override
                public boolean include(RowFilter.Entry entry) {
                    String filter = filterArea.getText();
                    for (int i = entry.getValueCount() - 1; i >= 0; i--) {
                        if (entry.getStringValue(i).contains(filter)) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            /** Sends the /list command and sleeps while waiting for server to finish sending the data*/
            refreshList.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        channel.connection.send("LIST");
                        Thread.sleep(100);
                        model.modelData.clear();
                        model.fireTableDataChanged();
                        while (channel.connection.currentlyUpdating) {
                            Thread.sleep(100);
                        }

                        if (channel.connection.doneUpdating) {
                            for (ListChannel channelList1 : channel.connection.channelList) {
                                model.addRow(channelList1);
                            }
                            channel.connection.doneUpdating = false;
                        }
                    } catch (IOException | BadLocationException | InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });


            joinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int r = table.getSelectedRow();
                    if (r == -1) return;
                    int row = table.convertRowIndexToModel(table.getSelectedRow());
                    String chan = (String) model.getValueAt(row, 1);

                    int index = Connection.findTab(chan, channel.connection); //if already in channel
                    if (index != -1) {
                        tabbedPane.setSelectedIndex(index);
                        return;
                    }

                    try {
                        channel.connection.send("JOIN " + chan);
                    } catch (IOException | BadLocationException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });


            filterArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
                    sorter.setRowFilter(filter);
                    table.setRowSorter(sorter);
                }
            });
            dialog.pack();
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }






    /** Clears text in all chatPane windows */
    static class ClearAllWindowsAction extends AbstractAction {
        private static ClearAllWindowsAction ref = null;

        private ClearAllWindowsAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ClearAllWindowsAction getInstance() {
            if (ref == null) ref = new ClearAllWindowsAction("Clear All Windows", null, null, KeyEvent.VK_A);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                try {
                    channel.chatPane.getDocument().remove(0, channel.chatPane.getDocument().getLength());
                } catch (BadLocationException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }



    /** Clears text in currently selected window */
    static class ClearWindowAction extends AbstractAction {
        private static ClearWindowAction ref = null;

        public ClearWindowAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ClearWindowAction getInstance() {
            if (ref == null) ref = new ClearWindowAction("Clear Window", null, null, KeyEvent.VK_C);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            try {
                channel.chatPane.getDocument().remove(0, channel.chatPane.getDocument().getLength());
            } catch (BadLocationException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }



    /** The action for closing removing a tab from the tabbedPane */
    static class CloseTabAction extends AbstractAction {
        private static CloseTabAction ref = null;

        private CloseTabAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static CloseTabAction getInstance() {
            if (ref == null) ref = new CloseTabAction("Close Tab", closeTabIcon, null, KeyEvent.VK_T);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = tabbedPane.getSelectedIndex();
            if (index == -1) return;
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            if (channel == null) return;
            channel.closeTab();
        }
    }



    /** JColorChooser action for choosing chat colors in the settings menu */
    static class ColorChooseAction extends AbstractAction {
        private static ColorChooseAction ref = null;

        private ColorChooseAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ColorChooseAction getInstance() {
            if (ref == null) ref = new ColorChooseAction("Choose Color", null, null, null);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Button button = (Button) e.getSource();
            Color c = JColorChooser.showDialog(frame, "Select Color", button.getBackground());
            if (c != null) {
                button.setBackground(c);
                button.revalidate();
            }
        }
    }




    /** Sets up the settings menu */
    static class ConfigureAction extends AbstractAction {
        private static ConfigureAction ref = null;

        private ConfigureAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ConfigureAction getInstance() {
            if (ref == null) ref = new ConfigureAction("Configure", configureIcon, null, KeyEvent.VK_C);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JDialog dialog = new JDialog(frame, "Configure", true);
            final SpringLayout layout = new SpringLayout();

            dialog.setPreferredSize(new Dimension(500, 400));

            Container contentpane = dialog.getContentPane();
            contentpane.setLayout(layout);

            JLabel fontLabel = new JLabel("Font");

            final JTextField fontField = new JTextField(" " + font + " " + fontSize);
            fontField.setFont(Font.decode(font));
            fontField.setEditable(false);
            fontField.setPreferredSize(new Dimension(200, 25));
            JButton fontButton = new JButton("Choose");

            JLabel colorsLabel = new JLabel("CTCP Text Colors");
            JLabel nickColorsLabel = new JLabel("Nick Colors");

            layout.putConstraint(SpringLayout.WEST, fontLabel, 10, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, fontLabel, 15, SpringLayout.NORTH, dialog);
            layout.putConstraint(SpringLayout.WEST, fontField, 10, SpringLayout.EAST, fontLabel);
            layout.putConstraint(SpringLayout.NORTH, fontField, 10, SpringLayout.NORTH, dialog);
            layout.putConstraint(SpringLayout.NORTH, fontButton, 8, SpringLayout.NORTH, dialog);
            layout.putConstraint(SpringLayout.WEST, fontButton, 10, SpringLayout.EAST, fontField);


            layout.putConstraint(SpringLayout.WEST, colorsLabel, 10, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, colorsLabel, 15, SpringLayout.SOUTH, fontLabel);

            for (int i = 0; i < CTCPMap.size(); i++) {
                Color c = CTCPMap.get(i);
                Button colorsButton = new Button();
                colorsButton.setBackground(c);
                colorsButton.addActionListener(ColorChooseAction.getInstance());
                colorsButton.setPreferredSize(new Dimension(16, 16));
                dialog.add(colorsButton);

                layout.putConstraint(SpringLayout.WEST, colorsButton, 30 + 20 * i, SpringLayout.WEST, dialog);
                layout.putConstraint(SpringLayout.NORTH, colorsButton, 20, SpringLayout.NORTH, colorsLabel);
            }


            layout.putConstraint(SpringLayout.WEST, nickColorsLabel, 10, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, nickColorsLabel, 25, SpringLayout.SOUTH, colorsLabel);

            for (int i = 0; i < chatColorMap.size(); i++) {
                Color c = chatColorMap.get(i);
                Button colorsButton = new Button();
                colorsButton.setBackground(c);
                colorsButton.addActionListener(ColorChooseAction.getInstance());
                colorsButton.setPreferredSize(new Dimension(16, 16));
                dialog.add(colorsButton);

                layout.putConstraint(SpringLayout.WEST, colorsButton, 30 + 20 * i, SpringLayout.WEST, dialog);
                layout.putConstraint(SpringLayout.NORTH, colorsButton, 20, SpringLayout.NORTH, nickColorsLabel);
            }


            dialog.add(fontLabel);
            dialog.add(fontField);
            dialog.add(fontButton);
            dialog.add(colorsLabel);
            dialog.add(nickColorsLabel);
            dialog.revalidate();


            fontButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {


                    SpringLayout fontLayout = new SpringLayout();
                    final JDialog fontDialog = new JDialog(frame, "Select Font", true);
                    fontDialog.getContentPane().setLayout(fontLayout);
                    fontDialog.setPreferredSize(new Dimension(395, 350));
                    fontDialog.setResizable(false);

                    JLabel fontLabel = new JLabel("Family");
                    JLabel fontStyleLabel = new JLabel("Style");
                    JLabel fontSizeLabel = new JLabel("Size");
                    JLabel fontPreview = new JLabel("Preview");
                    final JButton OKButton = new JButton("OK");
                    OKButton.setPreferredSize(new Dimension(70, 30));
                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.setPreferredSize(new Dimension(70, 30));


                    String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                    final JList<String> ffList = new JList<>(fonts);
                    ffList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    JScrollPane ffsp = new JScrollPane(ffList);
                    ffsp.setPreferredSize(new Dimension(200, 200));
                    ffList.setSelectedValue(font, true);


                    String fontStyles[] = {"Plain", "Bold", "Italic", "Bold Italic"};
                    final JList<String> fsList = new JList<>(fontStyles);
                    fsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                    JScrollPane fssp = new JScrollPane(fsList);
                    fssp.setPreferredSize(new Dimension(100, 200));
                    fsList.setSelectedValue(fontStyle, true);

                    Integer[] fontSizes = {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 24, 26, 28, 30, 32};
                    final JList<Integer> sizeList = new JList<>(fontSizes);
                    sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    JScrollPane sizesp = new JScrollPane(sizeList);
                    sizesp.setPreferredSize(new Dimension(50, 200));
                    sizeList.setSelectedValue(fontSize, true);

                    final JTextField previewArea = new JTextField();
                    previewArea.setPreferredSize(new Dimension(370, 70));
                    previewArea.setAlignmentY(JTextField.CENTER_ALIGNMENT);
                    previewArea.setHorizontalAlignment(JTextField.CENTER);
                    final String fontName = ffList.getSelectedValue();
                    final int size = sizeList.getSelectedValue();
                    final String style = fsList.getSelectedValue();

                    Font font;
                    if (style.equals("Bold")) font = new Font(fontName, Font.BOLD, size);
                    if (style.equals("Italic")) font = new Font(fontName, Font.ITALIC, size);
                    if (style.equals("Bold Italic")) font = new Font(fontName, Font.ITALIC | Font.BOLD, size);
                    else font = new Font(fontName, Font.PLAIN, size);

                    previewArea.setFont(font);
                    previewArea.setText("The quick brown fox jumps over the lazy dog");

                    fontLayout.putConstraint(SpringLayout.NORTH, fontLabel, 0, SpringLayout.NORTH, fontDialog);
                    fontLayout.putConstraint(SpringLayout.WEST, fontLabel, 10, SpringLayout.WEST, fontDialog);
                    fontLayout.putConstraint(SpringLayout.NORTH, ffsp, 2, SpringLayout.SOUTH, fontLabel);
                    fontLayout.putConstraint(SpringLayout.WEST, ffsp, 0, SpringLayout.WEST, fontLabel);

                    fontLayout.putConstraint(SpringLayout.NORTH, fontStyleLabel, 0, SpringLayout.NORTH, fontDialog);
                    fontLayout.putConstraint(SpringLayout.WEST, fontStyleLabel, 10, SpringLayout.EAST, ffsp);
                    fontLayout.putConstraint(SpringLayout.NORTH, fssp, 2, SpringLayout.SOUTH, fontStyleLabel);
                    fontLayout.putConstraint(SpringLayout.WEST, fssp, 10, SpringLayout.EAST, ffsp);

                    fontLayout.putConstraint(SpringLayout.NORTH, fontSizeLabel, 0, SpringLayout.NORTH, fontDialog);
                    fontLayout.putConstraint(SpringLayout.WEST, fontSizeLabel, 10, SpringLayout.EAST, fssp);
                    fontLayout.putConstraint(SpringLayout.NORTH, sizesp, 2, SpringLayout.SOUTH, fontSizeLabel);
                    fontLayout.putConstraint(SpringLayout.WEST, sizesp, 10, SpringLayout.EAST, fssp);

                    fontLayout.putConstraint(SpringLayout.NORTH, fontPreview, 5, SpringLayout.SOUTH, ffsp);
                    fontLayout.putConstraint(SpringLayout.WEST, fontPreview, 10, SpringLayout.WEST, fontDialog);
                    fontLayout.putConstraint(SpringLayout.NORTH, previewArea, 2, SpringLayout.SOUTH, fontPreview);
                    fontLayout.putConstraint(SpringLayout.WEST, previewArea, 0, SpringLayout.WEST, ffsp);

                    fontLayout.putConstraint(SpringLayout.EAST, OKButton, -15, SpringLayout.EAST, fontDialog);
                    fontLayout.putConstraint(SpringLayout.SOUTH, OKButton, -5, SpringLayout.SOUTH, fontDialog);
                    fontLayout.putConstraint(SpringLayout.SOUTH, cancelButton, 0, SpringLayout.SOUTH, OKButton);
                    fontLayout.putConstraint(SpringLayout.EAST, cancelButton, -5, SpringLayout.WEST, OKButton);

                    fontDialog.getRootPane().setDefaultButton(OKButton);
                    OKButton.requestFocus();

                    fontDialog.add(OKButton);
                    fontDialog.add(cancelButton);
                    fontDialog.add(fontLabel);
                    fontDialog.add(fontStyleLabel);
                    fontDialog.add(fontSizeLabel);
                    fontDialog.add(fontPreview);
                    fontDialog.add(previewArea);
                    fontDialog.add(ffsp);
                    fontDialog.add(fssp);
                    fontDialog.add(sizesp);

                    cancelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            fontDialog.dispose();
                        }
                    });
                    cancelButton.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                                fontDialog.dispose();
                            }
                        }
                    });
                    OKButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            fontSize = sizeList.getSelectedValue();
                            fontStyle = fsList.getSelectedValue();
                            setStyles();
                            fontDialog.dispose();
                        }
                    });
                    OKButton.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                                fontDialog.dispose();
                            }
                            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                                OKButton.getActionListeners()[0].actionPerformed(null);
                            }
                        }
                    });
                    ffList.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            final String fontName = ffList.getSelectedValue();
                            final int size = sizeList.getSelectedValue();
                            final String style = fsList.getSelectedValue();

                            Font font;
                            switch (style) {
                                case "Bold":
                                    font = new Font(fontName, Font.BOLD, size);
                                    break;
                                case "Italic":
                                    font = new Font(fontName, Font.ITALIC, size);
                                    break;
                                case "Bold Italic":
                                    font = new Font(fontName, Font.ITALIC | Font.BOLD, size);
                                    break;
                                default:
                                    font = new Font(fontName, Font.PLAIN, size);
                                    break;
                            }
                            previewArea.setFont(font);
                            String text = previewArea.getText();
                            try {
                                previewArea.getDocument().remove(0, previewArea.getDocument().getLength());
                                previewArea.setText(text);
                                fontField.setText(text);
                            } catch (BadLocationException ex) {
                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                    fsList.addListSelectionListener(ffList.getListSelectionListeners()[0]);
                    sizeList.addListSelectionListener(ffList.getListSelectionListeners()[0]);

                    fontDialog.pack();
                    fontDialog.setResizable(false);
                    fontDialog.setLocationRelativeTo(dialog);
                    fontDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    fontDialog.setVisible(true);
                }
            });


            dialog.pack();
            dialog.setResizable(false);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }




    /** Action for disconnecting from a server */
    static class DisconnectAction extends AbstractAction {
        private static DisconnectAction ref = null;

        private DisconnectAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static DisconnectAction getInstance() {
            if (ref == null) ref = new DisconnectAction("Disconnect", disconnectIcon, null, KeyEvent.VK_D);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = tabbedPane.getSelectedIndex();
            if (index == -1) return;
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            if (!channel.connection.isConnected) return;
            channel.model.removeAll();
            channel.connection.disconnect();
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel otherChannel = (ChannelPanel) tabbedPane.getComponentAt(i);

                if (otherChannel.name.equals(channel.server)) {
                    try {
                        String[] msg = {null, "[Info] Disconnected from " + otherChannel.server + " (port " + otherChannel.connection.port + ")"};
                        otherChannel.insertString(msg, serverStyle, false);
                        continue;
                    } catch (BadLocationException | IOException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (otherChannel.connection == channel.connection) {
                    tabbedPane.setIconAt(i, notConnectedIcon);
                    otherChannel.model.removeAll();
                }

            }
            channel.updateTabInfo();
        }

    }




    /** Action for finding text in the chatPane i.e. ctrl+F */
    static class FindTextAction extends AbstractAction {
        static boolean isOpened;
        private static FindTextAction ref = null;

        private FindTextAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static FindTextAction getInstance() {
            if (ref == null) ref = new FindTextAction("Find", findTextIcon, null, KeyEvent.VK_F);
            return ref;
        }

        private void removeHighlights() {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                DefaultHighlighter highlighter = (DefaultHighlighter) channel.chatPane.getHighlighter();
                highlighter.removeAllHighlights();
                channel.lastSearchIndex = -1;
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isOpened) return;
            final JDialog dialog = new JDialog(frame, true);
            dialog.setTitle("Find");
            SpringLayout layout = new SpringLayout();
            JPanel panel = new JPanel(layout);

            dialog.add(panel);
            dialog.setPreferredSize(new Dimension(240, 100));
            dialog.setResizable(false);

            Container contentPane = dialog.getContentPane();
            contentPane.setLayout(layout);

            final JTextField field = new JTextField();

            final JCheckBox matchCase = new JCheckBox("Match Case");
            final JCheckBox highlightAll = new JCheckBox("Highlight All");
            final JButton findButton = new JButton("Find");
            final JButton closeButton = new JButton("Close");
            findButton.setPreferredSize(new Dimension(100, 25));
            closeButton.setPreferredSize(new Dimension(100, 25));
            contentPane.add(field);
            contentPane.add(matchCase);
            contentPane.add(highlightAll);
            contentPane.add(findButton);
            contentPane.add(closeButton);
            layout.putConstraint(SpringLayout.NORTH, field, 5, SpringLayout.NORTH, dialog);
            layout.putConstraint(SpringLayout.WEST, field, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.EAST, field, -15, SpringLayout.EAST, dialog);
            layout.putConstraint(SpringLayout.WEST, matchCase, 0, SpringLayout.WEST, field);
            layout.putConstraint(SpringLayout.NORTH, matchCase, 5, SpringLayout.SOUTH, field);
            layout.putConstraint(SpringLayout.NORTH, highlightAll, 5, SpringLayout.SOUTH, field);
            layout.putConstraint(SpringLayout.EAST, highlightAll, -5, SpringLayout.EAST, contentPane);
            layout.putConstraint(SpringLayout.SOUTH, findButton, -5, SpringLayout.SOUTH, contentPane);
            layout.putConstraint(SpringLayout.WEST, findButton, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.SOUTH, closeButton, -5, SpringLayout.SOUTH, contentPane);
            layout.putConstraint(SpringLayout.EAST, closeButton, -15, SpringLayout.EAST, dialog);
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeHighlights();
                    dialog.dispose();
                }
            });
            closeButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        closeButton.getActionListeners()[0].actionPerformed(null);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        closeButton.getActionListeners()[0].actionPerformed(null);
                    }
                }
            });

            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeButton.getActionListeners()[0].actionPerformed(null);
                }
            });
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    removeHighlights();
                    findButton.getActionListeners()[0].actionPerformed(null);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    removeHighlights();
                    findButton.getActionListeners()[0].actionPerformed(null);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    removeHighlights();
                    findButton.getActionListeners()[0].actionPerformed(null);
                }
            });
            field.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        closeButton.getActionListeners()[0].actionPerformed(null);
                    }
                    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
                        closeButton.getActionListeners()[0].actionPerformed(null);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        findButton.getActionListeners()[0].actionPerformed(null);
                    }
                }
            });
            findButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        closeButton.getActionListeners()[0].actionPerformed(null);
                    }
                    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
                        closeButton.getActionListeners()[0].actionPerformed(null);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        findButton.getActionListeners()[0].actionPerformed(null);
                    }
                }
            });

            findButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    StyledDocument doc = channel.chatPane.getStyledDocument();
                    DefaultHighlighter.DefaultHighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(highlightColor);
                    DefaultHighlighter highlighter = (DefaultHighlighter) channel.chatPane.getHighlighter();
                    highlighter.setDrawsLayeredHighlights(false);
                    String searchString = field.getText();


                    if (highlightAll.isSelected()) {
                        highlightAll(searchString);
                        return;
                    }


                    String s = "";
                    try {
                        s = doc.getText(0, doc.getLength());
                    } catch (BadLocationException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    int location;
                    if (matchCase.isSelected()) {
                        if (channel.lastSearchIndex == -1) location = s.indexOf(searchString);
                        else location = s.indexOf(searchString, channel.lastSearchIndex + searchString.length());
                    } else {
                        if (channel.lastSearchIndex == -1) location = StringUtils.indexOfIgnoreCase(s, searchString);
                        else
                            location = StringUtils.indexOfIgnoreCase(s, searchString, channel.lastSearchIndex + searchString.length());
                    }

                    if (location != -1) {
                        try {
                            removeHighlights();
                            highlighter.addHighlight(location, location + searchString.length(), p);
                            channel.lastSearchIndex = location;
                            DefaultCaret caret = (DefaultCaret) channel.chatPane.getCaret();
                            caret.setDot(location);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

                private void highlightAll(String searchString) {
                    ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
                    StyledDocument doc = channel.chatPane.getStyledDocument();
                    DefaultHighlighter.DefaultHighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(highlightColor);
                    DefaultHighlighter highlighter = (DefaultHighlighter) channel.chatPane.getHighlighter();
                    String s = "";
                    try {
                        s = doc.getText(0, doc.getLength());
                    } catch (BadLocationException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    removeHighlights();

                    int location = 0;

                    while (location != -1) {
                        if (matchCase.isSelected()) {
                            if (channel.lastSearchIndex == -1) location = s.indexOf(searchString);
                            else location = s.indexOf(searchString, channel.lastSearchIndex + searchString.length());
                        } else {
                            if (channel.lastSearchIndex == -1)
                                location = StringUtils.indexOfIgnoreCase(s, searchString);
                            else
                                location = StringUtils.indexOfIgnoreCase(s, searchString, channel.lastSearchIndex + searchString.length());
                        }
                        channel.lastSearchIndex = location;
                        if (location == -1) break;
                        try {
                            highlighter.addHighlight(location, location + searchString.length(), p);
                        } catch (BadLocationException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            findButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    findButton.getActionListeners()[0].actionPerformed(null);
                }
            });
            highlightAll.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    matchCase.getItemListeners()[0].itemStateChanged(null);
                }
            });
            matchCase.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (field.getText().isEmpty()) return;
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                        DefaultHighlighter highlighter = (DefaultHighlighter) channel.chatPane.getHighlighter();
                        if (!channel.isShowing()) highlighter.removeAllHighlights();
                        channel.lastSearchIndex = -1;
                    }
                    findButton.getActionListeners()[0].actionPerformed(null);
                }
            });
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    removeHighlights();
                    isOpened = false;
                }
            });
            dialog.pack();
            isOpened = true;
            dialog.setModalityType(Dialog.ModalityType.MODELESS);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(!dialog.isShowing());
        }

    }




    /** Action for settings self to away in in all channels and connections */
    static class GlobalAwayAction extends AbstractAction {
        private static GlobalAwayAction ref = null;

        private GlobalAwayAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static GlobalAwayAction getInstance() {
            if (ref == null) ref = new GlobalAwayAction("Global Away", globalAwayIcon, null, KeyEvent.VK_A);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                String name = channel.name;
                String server = channel.server;
                String message;
                if (name.equals(server)) {
                    if (ChannelPanel.awayStatus) {
                        message = "AWAY";
                        ChannelPanel.awayStatus = false;
                    } else {
                        message = "AWAY " + ChannelPanel.awayMessage;
                        ChannelPanel.awayStatus = true;
                    }
                    try {
                        channel.connection.send(message);
                    } catch (IOException | BadLocationException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
    }




    /** Builds menu for settings nick fields */
    static class IdentityAction extends AbstractAction {
        private static IdentityAction ref = null;

        private IdentityAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static IdentityAction getInstance() {
            if (ref == null) ref = new IdentityAction("Identities", identityIcon, null, KeyEvent.VK_I);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JDialog dialog = new JDialog(frame, "Identity", true);
            SpringLayout layout = new SpringLayout();
            dialog.getContentPane().setLayout(layout);
            dialog.setPreferredSize(new Dimension(200, 290));

            JLabel realNameLabel = new JLabel(" Real Name");
            realNameLabel.setPreferredSize(new Dimension(175, 25));
            JLabel firstChoiceLabel = new JLabel(" Nick");
            firstChoiceLabel.setPreferredSize(new Dimension(175, 25));
            JLabel secondChoiceLabel = new JLabel("Second choice");
            secondChoiceLabel.setPreferredSize(new Dimension(175, 25));
            JLabel thirdChoiceLabel = new JLabel(" Third Choice");
            thirdChoiceLabel.setPreferredSize(new Dimension(175, 25));

            final JTextField realNameField = new JTextField();
            realNameField.setText(Connection.real);
            realNameField.setPreferredSize(new Dimension(175, 25));
            final JTextField nameField = new JTextField();
            nameField.setText(Connection.nicks[0]);
            nameField.setPreferredSize(new Dimension(175, 25));
            final JTextField secondNameField = new JTextField();
            secondNameField.setText(Connection.nicks[1]);
            secondNameField.setPreferredSize(new Dimension(175, 25));
            final JTextField thirdNameField = new JTextField();
            thirdNameField.setText(Connection.nicks[2]);
            thirdNameField.setPreferredSize(new Dimension(175, 25));

            final JButton saveButton = new JButton("Save");
            saveButton.setPreferredSize(new Dimension(70, 30));

            layout.putConstraint(SpringLayout.WEST, realNameLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, realNameLabel, 5, SpringLayout.NORTH, dialog);

            layout.putConstraint(SpringLayout.WEST, realNameField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, realNameField, 0, SpringLayout.SOUTH, realNameLabel);

            layout.putConstraint(SpringLayout.WEST, firstChoiceLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, firstChoiceLabel, 5, SpringLayout.SOUTH, realNameField);

            layout.putConstraint(SpringLayout.WEST, nameField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, nameField, 0, SpringLayout.SOUTH, firstChoiceLabel);

            layout.putConstraint(SpringLayout.WEST, secondChoiceLabel, 5, SpringLayout.WEST, nameField);
            layout.putConstraint(SpringLayout.NORTH, secondChoiceLabel, 5, SpringLayout.SOUTH, nameField);

            layout.putConstraint(SpringLayout.WEST, secondNameField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, secondNameField, 0, SpringLayout.SOUTH, secondChoiceLabel);

            layout.putConstraint(SpringLayout.WEST, thirdChoiceLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, thirdChoiceLabel, 5, SpringLayout.SOUTH, secondNameField);

            layout.putConstraint(SpringLayout.WEST, thirdNameField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, thirdNameField, 0, SpringLayout.SOUTH, thirdChoiceLabel);

            layout.putConstraint(SpringLayout.EAST, saveButton, -35, SpringLayout.EAST, dialog);
            layout.putConstraint(SpringLayout.SOUTH, saveButton, -35, SpringLayout.SOUTH, dialog);

            dialog.add(realNameLabel);
            dialog.add(realNameField);
            dialog.add(firstChoiceLabel);
            dialog.add(nameField);
            dialog.add(secondChoiceLabel);
            dialog.add(secondNameField);
            dialog.add(thirdChoiceLabel);
            dialog.add(thirdNameField);
            dialog.add(saveButton);


            saveButton.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        saveButton.getActionListeners()[0].actionPerformed(null);
                    }
                }
            });
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Connection.real = realNameField.getText().trim();
                        Connection.nicks[0] = nameField.getText().trim();
                        Connection.nicks[1] = secondNameField.getText().trim();
                        Connection.nicks[2] = thirdNameField.getText().trim();

                        prop.load(new FileInputStream("config.properties"));
                        prop.setProperty("Real", Connection.real);
                        prop.setProperty("Nick", Connection.nicks[0]);
                        prop.setProperty("Second", Connection.nicks[1]);
                        prop.setProperty("Third", Connection.nicks[2]);
                        prop.store(new FileOutputStream("config.properties"), null);

                    } catch (IOException io) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, io);
                    }
                    dialog.dispose();
                }

            });

            dialog.pack();
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }




    /** A dialog for joining a channel */
    static class JoinChannelAction extends AbstractAction {
        static JoinChannelAction ref = null;

        private JoinChannelAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static JoinChannelAction getInstance() {
            if (ref == null) ref = new JoinChannelAction("Join Channel", joinChannelIcon, null, KeyEvent.VK_J);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            SortedSet<String> set = new TreeSet<>();
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                String srv = channel.connection.title;
                set.add(srv);
            }
            Object[] things = set.toArray();


            final JDialog dialog = new JDialog(frame, "Join Channel", true);
            dialog.setSize(new Dimension(300, 180));
            SpringLayout layout = new SpringLayout();
            JPanel panel = new JPanel(layout);
            dialog.setResizable(false);
            dialog.add(panel);
            final JComboBox<Object> combobox = new JComboBox<Object>(things);
            JLabel serverLabel = new JLabel("Connection");
            JLabel chanLabel = new JLabel("Channel");

            JLabel pwLabel = new JLabel("Password");
            final JTextField channelField = new JTextField();
            channelField.setPreferredSize(new Dimension(180, 20));
            JTextField pwField = new JTextField();
            pwField.setPreferredSize(new Dimension(180, 20));
            combobox.setPreferredSize(new Dimension(180, 20));
            JButton cancel = new JButton("Cancel");
            final JButton ok = new JButton("Join");

            cancel.setPreferredSize(new Dimension(70, 30));
            ok.setPreferredSize(new Dimension(70, 30));

            panel.add(serverLabel);
            panel.add(combobox);
            panel.add(chanLabel);
            panel.add(channelField);
            panel.add(pwLabel);
            panel.add(pwField);
            panel.add(cancel);
            panel.add(ok);


            layout.putConstraint(SpringLayout.NORTH, serverLabel, 15, SpringLayout.NORTH, dialog);
            layout.putConstraint(SpringLayout.WEST, serverLabel, 15, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, combobox, 13, SpringLayout.NORTH, dialog);
            layout.putConstraint(SpringLayout.WEST, combobox, 10, SpringLayout.EAST, serverLabel);
            layout.putConstraint(SpringLayout.NORTH, chanLabel, 16, SpringLayout.SOUTH, combobox);
            layout.putConstraint(SpringLayout.WEST, chanLabel, 0, SpringLayout.WEST, serverLabel);
            layout.putConstraint(SpringLayout.NORTH, channelField, 15, SpringLayout.SOUTH, combobox);
            layout.putConstraint(SpringLayout.WEST, channelField, 0, SpringLayout.WEST, combobox);
            layout.putConstraint(SpringLayout.NORTH, pwLabel, 18, SpringLayout.SOUTH, chanLabel);
            layout.putConstraint(SpringLayout.WEST, pwLabel, 0, SpringLayout.WEST, serverLabel);
            layout.putConstraint(SpringLayout.NORTH, pwField, 13, SpringLayout.SOUTH, channelField);
            layout.putConstraint(SpringLayout.WEST, pwField, 0, SpringLayout.WEST, combobox);
            layout.putConstraint(SpringLayout.WEST, cancel, 115, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.SOUTH, cancel, 120, SpringLayout.SOUTH, dialog);
            layout.putConstraint(SpringLayout.WEST, ok, 10, SpringLayout.EAST, cancel);
            layout.putConstraint(SpringLayout.SOUTH, ok, 0, SpringLayout.SOUTH, cancel);

            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });
            cancel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) dialog.dispose();
                }
            });


            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String choice = (String) combobox.getSelectedItem();
                    String server = channelField.getText();


                    if (!server.startsWith("#")) server = "#" + server;
                    if (choice == null) {
                        JOptionPane.showConfirmDialog(dialog, "Please choose a connection.", "No connection chosen", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    ChannelPanel channel = null;
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        channel = ((ChannelPanel) tabbedPane.getComponentAt(i));
                        if (channel.server.equals(choice)) {
                            break;
                        }
                    }
                    try {
                        if (channel != null) {
                            channel.connection.send("JOIN " + server);
                        }
                        dialog.dispose();
                    } catch (IOException | BadLocationException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });
            ok.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        ok.getActionListeners()[0].actionPerformed(null);
                    }
                }
            });
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }





    /** Shifts selected tab left on the tabbedPane and maintains the selected window */
    static class MoveTabLeftAction extends AbstractAction {
        private static MoveTabLeftAction ref = null;

        private MoveTabLeftAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static MoveTabLeftAction getInstance() {
            if (ref == null) ref = new MoveTabLeftAction("Move Tab Left", moveTabLeftIcon, null, KeyEvent.VK_L);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = tabbedPane.getSelectedIndex();
            if (index <= 0) return;
            ChannelPanel moved = (ChannelPanel) tabbedPane.getComponentAt(index);
            Color c = tabbedPane.getForegroundAt(index);
            String label = tabbedPane.getTitleAt(index);
            tabbedPane.add(moved, index - 1);
            tabbedPane.setTitleAt(index - 1, label);
            tabbedPane.setSelectedComponent(moved);
            tabbedPane.setForegroundAt(index - 1, c);
        }
    }





    /** Shifts selected tab right on the tabbedPane and maintains the selected window */
    static class MoveTabRightAction extends AbstractAction {
        private static MoveTabRightAction ref = null;

        private MoveTabRightAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static MoveTabRightAction getInstance() {
            if (ref == null) ref = new MoveTabRightAction("Move Tab Right", moveTabRightIcon, null, KeyEvent.VK_R);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = tabbedPane.getSelectedIndex() + 1;
            if (index >= tabbedPane.getTabCount()) return;
            ChannelPanel moved = (ChannelPanel) tabbedPane.getComponentAt(index);
            Color c = tabbedPane.getForegroundAt(index);
            String label = tabbedPane.getTitleAt(index);
            tabbedPane.add(moved, index - 1);
            tabbedPane.setTitleAt(index - 1, label);
            tabbedPane.setSelectedIndex(index);
            tabbedPane.setForegroundAt(index - 1, c);
        }
    }





    /** Action for changing the currently selected tab */
    static class NextTabAction extends AbstractAction {
        private static NextTabAction ref = null;

        private NextTabAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static NextTabAction getInstance() {
            if (ref == null) ref = new NextTabAction("Next Tab", nextTabIcon, null, KeyEvent.VK_N);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int indexOfChannel = tabbedPane.getSelectedIndex();
            if (indexOfChannel < tabbedPane.getTabCount() - 1) tabbedPane.setSelectedIndex(indexOfChannel + 1);
        }
    }





    /** Action for changing the currently selected tab */
    static class PreviousTabAction extends AbstractAction {
        private static PreviousTabAction ref = null;

        private PreviousTabAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static PreviousTabAction getInstance() {
            if (ref == null) ref = new PreviousTabAction("PreviousTab", prevTabIcon, null, KeyEvent.VK_P);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int indexOfChannel = tabbedPane.getSelectedIndex();
            if (indexOfChannel > 0) tabbedPane.setSelectedIndex(indexOfChannel - 1);
        }
    }




    /** A small dialog allowing quick connection to a server */
    static class QuickConnectAction extends AbstractAction {
        private static QuickConnectAction ref = null;

        private QuickConnectAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static QuickConnectAction getInstance() {
            if (ref == null) ref = new QuickConnectAction("Quick Connect", quickConnectIcon, null, KeyEvent.VK_C);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SpringLayout layout = new SpringLayout();

            final JDialog dialog = new JDialog(frame, "Quick Connect", true);
            dialog.getContentPane().setLayout(layout);
            dialog.setPreferredSize(new Dimension(200, 320));
            final JButton connectButton = new JButton("Connect");
            JButton cancelButton = new JButton("Cancel");
            connectButton.setPreferredSize(new Dimension(70, 30));
            cancelButton.setPreferredSize(new Dimension(70, 30));

            JLabel networkNameLabel = new JLabel("Network Name");
            JLabel serverLabel = new JLabel("Server");
            JLabel portLabel = new JLabel("Port");
            JLabel nickLabel = new JLabel("Nick");
            JLabel passLabel = new JLabel("Password");

            final JTextField networkNameField = new JTextField();
            networkNameField.setPreferredSize(new Dimension(175, 25));
            final JTextField serverField = new JTextField();
            serverField.setPreferredSize(new Dimension(175, 25));
            final JTextField portField = new JTextField();
            portField.setPreferredSize(new Dimension(175, 25));
            final JTextField nickField = new JTextField();
            nickField.setPreferredSize(new Dimension(175, 25));
            final JTextField passField = new JTextField();
            passField.setPreferredSize(new Dimension(175, 25));

            layout.putConstraint(SpringLayout.WEST, networkNameLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, networkNameLabel, 5, SpringLayout.NORTH, dialog);

            layout.putConstraint(SpringLayout.WEST, networkNameField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, networkNameField, 5, SpringLayout.SOUTH, networkNameLabel);

            layout.putConstraint(SpringLayout.WEST, serverLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, serverLabel, 5, SpringLayout.SOUTH, networkNameField);

            layout.putConstraint(SpringLayout.WEST, serverField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, serverField, 5, SpringLayout.SOUTH, serverLabel);

            layout.putConstraint(SpringLayout.WEST, portLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, portLabel, 5, SpringLayout.SOUTH, serverField);

            layout.putConstraint(SpringLayout.WEST, portField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, portField, 5, SpringLayout.SOUTH, portLabel);

            layout.putConstraint(SpringLayout.WEST, nickLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, nickLabel, 5, SpringLayout.SOUTH, portField);

            layout.putConstraint(SpringLayout.WEST, nickField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, nickField, 5, SpringLayout.SOUTH, nickLabel);

            layout.putConstraint(SpringLayout.WEST, passLabel, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, passLabel, 5, SpringLayout.SOUTH, nickField);

            layout.putConstraint(SpringLayout.WEST, passField, 5, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.NORTH, passField, 5, SpringLayout.SOUTH, passLabel);

            layout.putConstraint(SpringLayout.WEST, cancelButton, 15, SpringLayout.WEST, dialog);
            layout.putConstraint(SpringLayout.SOUTH, cancelButton, -35, SpringLayout.SOUTH, dialog);

            layout.putConstraint(SpringLayout.WEST, connectButton, 10, SpringLayout.EAST, cancelButton);
            layout.putConstraint(SpringLayout.SOUTH, connectButton, -35, SpringLayout.SOUTH, dialog);

            dialog.add(networkNameLabel);
            dialog.add(networkNameField);
            dialog.add(serverLabel);
            dialog.add(serverField);
            dialog.add(portLabel);
            dialog.add(portField);
            dialog.add(nickLabel);
            dialog.add(nickField);
            dialog.add(passLabel);
            dialog.add(passField);
            dialog.add(connectButton);
            dialog.add(cancelButton);


            connectButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        connectButton.getActionListeners()[0].actionPerformed(null);
                    }
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        dialog.dispose();
                    }
                }
            });

            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String networkName = networkNameField.getText().trim();
                    String server = serverField.getText().trim();
                    String port = portField.getText().trim();
                    String nick = nickField.getText().trim();
                    String pass = passField.getText().trim();

                    if (networkName.isEmpty() || server.isEmpty() || port.isEmpty() || nick.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Network name, server, port and nick are required");
                    } else {
                        dialog.dispose();
                        Connection.nicks[0] = nick;

                        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                            ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                            if (networkName.equals(channel.title)) {
                                tabbedPane.setSelectedComponent(channel);
                                String[] msg = {null, "You are already connected to " + networkName + "."};
                                try {
                                    channel.insertString(msg, errorStyle, false);
                                } catch (BadLocationException | IOException ex) {
                                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                return;
                            }
                        }

                        Connection c = new Connection(networkName, server, Integer.valueOf(port), false);
                        if (!pass.isEmpty()) c.password = pass;
                    }
                }

            });
            cancelButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        dialog.dispose();
                    }
                }
            });
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });

            cancelButton.getRootPane().setDefaultButton(cancelButton);
            cancelButton.requestFocus();
            dialog.pack();
            dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }




    /** Menu item for closing the client */
    static class QuitProgramAction extends AbstractAction {
        private static QuitProgramAction ref = null;

        private QuitProgramAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static QuitProgramAction getInstance() {
            if (ref == null) ref = new QuitProgramAction("Quit", quitProgramIcon, null, KeyEvent.VK_Q);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }




    /** Closes and reopens server connections */
    static class ReconnectAction extends AbstractAction {
        private static ReconnectAction ref = null;

        private ReconnectAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ReconnectAction getInstance() {
            if (ref == null) ref = new ReconnectAction("Reconnect", reconnectIcon, null, KeyEvent.VK_R);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ChannelPanel selected = (ChannelPanel) tabbedPane.getSelectedComponent();
            if (selected.connection.isConnected) {
                System.out.println("disconnecting");
                selected.connection.disconnect();
            }


            for (int j = 0; j < tabbedPane.getTabCount(); j++) {
                ChannelPanel otherChannel = (ChannelPanel) tabbedPane.getComponentAt(j);
                if (selected.connection == otherChannel.connection) {
                    selected.connection.thread = new Thread(selected.connection);
                    selected.connection.thread.start();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    break;
                }
            }
            selected.connection.autoconnect = false;

            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                if (channel.connection == selected.connection) {
                    if (channel.title.startsWith("#")) {
                        try {
                            selected.connection.send("JOIN " + channel.title);
                            System.out.println("asdfasdf");
                        } catch (IOException ex) {
                            System.out.println("io");
                        } catch (BadLocationException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }





    /** Displays the list of saved connections and channels to auto-connect to*/
    static class ServerListAction extends AbstractAction {
        private static ServerListAction ref = null;

        private ServerListAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ServerListAction getInstance() {
            if (ref == null) ref = new ServerListAction("Server List", serverListIcon, null, KeyEvent.VK_L);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JDialog dialog = new JDialog(frame, "Server List", true);
            Container contentpane = dialog.getContentPane();
            SpringLayout layout = new SpringLayout();
            contentpane.setLayout(layout);
            contentpane.setPreferredSize(new Dimension(550, 200));
            final BeanTableModel<SavedConnection> model = new BeanTableModel<>(SavedConnection.class);
            model.sortColumnNames();
            for (SavedConnection savedConnection : savedConnections) model.addRow(savedConnection);


            final JTable table = new JTable(model) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            table.setShowGrid(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getColumnModel().getColumn(0).setPreferredWidth(150);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(272);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(540, 150));
            scrollPane.setViewportView(table);

            final JButton add = new JButton("Add");
            JButton edit = new JButton("Edit");
            JButton remove = new JButton("Remove");
            JButton connect = new JButton("Connect");
            add.setPreferredSize(new Dimension(70, 30));
            edit.setPreferredSize(new Dimension(70, 30));
            remove.setPreferredSize(new Dimension(70, 30));
            connect.setPreferredSize(new Dimension(70, 30));

            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            dialog.setResizable(false);
            dialog.setTitle("Server List");
            contentpane.add(scrollPane);
            contentpane.add(add);
            contentpane.add(edit);
            contentpane.add(remove);
            contentpane.add(connect);
            layout.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, contentpane);
            layout.putConstraint(SpringLayout.SOUTH, add, -10, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.WEST, add, 10, SpringLayout.WEST, contentpane);
            layout.putConstraint(SpringLayout.SOUTH, edit, -10, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.WEST, edit, 10, SpringLayout.EAST, add);
            layout.putConstraint(SpringLayout.SOUTH, remove, -10, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.WEST, remove, 10, SpringLayout.EAST, edit);
            layout.putConstraint(SpringLayout.SOUTH, connect, -10, SpringLayout.SOUTH, contentpane);
            layout.putConstraint(SpringLayout.EAST, connect, -15, SpringLayout.EAST, contentpane);

            add.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JTextField name = new JTextField();
                    JTextField server = new JTextField();
                    JTextField port = new JTextField();
                    JTextField password = new JTextField();
                    JTextField channels = new JTextField();
                    JCheckBox autoconnect = new JCheckBox();

                    Object[] fields = {
                            "Name", name,
                            "Server", server,
                            "Port", port,
                            "Password", password,
                            "Auto Join Channels (#c1 #c2)", channels,
                            "Connect on startup", autoconnect,
                    };
                    int x = JOptionPane.showConfirmDialog(dialog, fields, "Add Server", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (x == JOptionPane.CLOSED_OPTION || x == JOptionPane.CANCEL_OPTION) return;

                    String[] s = channels.getText().split(" ");
                    ArrayList<String> channelList = new ArrayList<>(Arrays.asList(s));
                    if (!StringUtils.isNumeric(port.getText())) {
                        JOptionPane.showMessageDialog(dialog, "Port must be an integer.");
                        return;
                    }
                    SavedConnection connection = new SavedConnection(name.getText(), server.getText(), password.getText(), channelList, autoconnect.isSelected(), Integer.valueOf(port.getText().trim()));
                    model.addRow(connection);
                    savedConnections.add(connection);
                    serializeSavedConnections();
                }
            });

            add.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        add.getActionListeners()[0].actionPerformed(null);
                    }
                }
            });
            edit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int rowIndex = table.getSelectedRow();
                    if (rowIndex == -1) return;
                    int modelIndex = table.convertRowIndexToModel(rowIndex);
                    SavedConnection conn = model.modelData.get(modelIndex);

                    JTextField ename = new JTextField(conn.retrieveName());
                    JTextField eserver = new JTextField(conn.retrieveServer());
                    JTextField eport = new JTextField(Integer.toString(conn.retrievePort()));
                    JTextField epassword = new JTextField(conn.retrievePassword());
                    String c = conn.retrieveChannels().toString();
                    JTextField echannels = new JTextField(c.substring(1, c.length() - 1));
                    JCheckBox eautoconnect = new JCheckBox();
                    if (conn.retrieveAutoConnect()) eautoconnect.setSelected(true);
                    else eautoconnect.setSelected(false);

                    Object[] efields = {
                            "Name", ename,
                            "Server", eserver,
                            "Port", eport,
                            "Password", epassword,
                            "Auto Join Channels (#c1 #c2)", echannels,
                            "Connect on startup", eautoconnect,
                    };
                    int ex = JOptionPane.showConfirmDialog(dialog, efields, "Edit Server", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (ex == JOptionPane.CLOSED_OPTION || ex == JOptionPane.CANCEL_OPTION) return;

                    String[] es = echannels.getText().split(" ");
                    ArrayList<String> echannelList = new ArrayList<>(Arrays.asList(es));
                    if (!StringUtils.isNumeric(eport.getText())) {
                        JOptionPane.showMessageDialog(dialog, "Port must be an integer.");
                        return;
                    }
                    model.removeRowRange(modelIndex, modelIndex);
                    savedConnections.remove(conn);
                    SavedConnection connection = new SavedConnection(ename.getText(), eserver.getText(), epassword.getText(), echannelList, eautoconnect.isSelected(), Integer.valueOf(eport.getText().trim()));
                    model.addRow(connection);
                    savedConnections.add(connection);
                    serializeSavedConnections();
                }
            });

            remove.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int rowIndex = table.getSelectedRow();
                    if (rowIndex == -1) return;
                    int modelIndex = table.convertRowIndexToModel(rowIndex);
                    SavedConnection conn = model.modelData.get(modelIndex);
                    savedConnections.remove(conn);
                    serializeSavedConnections();
                    model.removeRowRange(modelIndex, modelIndex);
                }
            });

            connect.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int rowIndex = table.getSelectedRow();
                    if (rowIndex == -1) return;
                    SavedConnection conn = model.modelData.get(table.convertRowIndexToModel(rowIndex));

                    String network = conn.getNetwork(), server = conn.getServer();
                    int port = conn.retrievePort();
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                        if (network.equals(channel.title)) {
                            tabbedPane.setSelectedComponent(channel);
                            String[] msg = {null, "You are already connected to " + network + "."};
                            try {
                                channel.insertString(msg, errorStyle, false);
                            } catch (BadLocationException | IOException ex) {
                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            dialog.dispose();
                            return;
                        }
                    }
                    new Connection(network, server, port);
                    dialog.dispose();
                }
            });
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }





    /** Toggles visibility of the user list of a given channel */
    static class ShowNickListAction extends AbstractAction {
        private static ShowNickListAction ref = null;

        private ShowNickListAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public static ShowNickListAction getInstance() {
            if (ref == null) ref = new ShowNickListAction("Show/Hide Nicklist", showNicklistIcon, null, KeyEvent.VK_N);
            return ref;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);
                if (channel.title.equals(channel.name)) {
                    if (channel.getRightComponent() != null) {
                        channel.setRightComponent(null);
                        channel.setDividerSize(0);
                        channel.setDividerLocation(0);
                    } else {
                        channel.setRightComponent(channel.userListScrollPane);
                        channel.setDividerSize(5);
                        channel.setDividerLocation(GUI.frame.getWidth() - 160);
                    }
                }
            }
        }
    }
}