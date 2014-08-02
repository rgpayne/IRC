import org.apache.commons.lang3.StringUtils;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class ChannelPanel extends JSplitPane {
    final static Map<String, Color> userMap = new HashMap<>();
    static boolean awayStatus = false;
    static String CTCPFingerMessage = "this is the finger message", CTCPUserInfo = "user info string";
    static DnDTabbedPane tabbedPane;
    static JLabel tabInfo;
    static ArrayList<String> tabNicks;

    final String title;
    final JTextPane chatPane = new JTextPane();
    final JList<User> userListPane;
    final JScrollPane userListScrollPane = new JScrollPane(), chatScrollPane = new JScrollPane();
    public ArrayList<String> ignoreList = new ArrayList<>();
    String name;
    String topic = "", signOnTime, server;
    int population, ops = 0;
    int listSelectedIndex = -1;

    boolean enableNotifications;

    Connection connection;
    SortedListModel<User> model = new SortedListModel<>();
    ArrayList<String> list = new ArrayList<>();
    StyledDocument doc;
    int lastSearchIndex = -1;

    ArrayList<String> history;
    int historyCounter = 0;




    public ChannelPanel(String title, String name, Connection c) throws BadLocationException, IOException {
        this.title = title; //this is what is shown on a tab
        this.name = name;
        this.connection = c;
        doc = chatPane.getStyledDocument();

        PlainTextHyperlinkListener hyperlinkListener = new PlainTextHyperlinkListener(chatPane);
        chatPane.addHyperlinkListener(hyperlinkListener);

        userListPane = new JList(model);
        if (GUI.showTimestamp) history = new ArrayList<>();

        makePanel();
        GUI.loadKeyBinds(chatPane.getActionMap(), chatPane.getInputMap());


        TextClickListener tcl = new TextClickListener(chatPane, this.connection);
        TextMotionListener tml = new TextMotionListener(chatPane);
        chatPane.addMouseMotionListener(tml);
        chatPane.addMouseListener(tcl);

        addTab();
    }



    private void makePanel() throws BadLocationException, IOException {
        userListPane.setModel(model);
        userListPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userListPane.setLayoutOrientation(JList.VERTICAL);
        userListPane.setCellRenderer(new CustomRenderer());
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new Dimension(25, 25));


        JPopupMenu ULpopup = new JPopupMenu();
        JMenuItem ULpopOpenQuery = new JMenuItem("Open Query", GUI.popupQueryIcon);
        JMenuItem ULpopWhois = new JMenuItem("Whois", GUI.popupWhoisIcon);
        JMenuItem ULpopVersion = new JMenuItem("Version", GUI.popupVersionIcon);
        JMenuItem ULpopPing = new JMenuItem("Ping", GUI.popupPingIcon);
        JMenuItem ULpopIgnore = new JMenuItem("Ignore", GUI.popupIgnoreIcon);
        ULpopup.add(ULpopOpenQuery);
        ULpopup.add(new JSeparator());
        ULpopup.add(ULpopWhois);
        ULpopup.add(ULpopVersion);
        ULpopup.add(ULpopPing);
        ULpopup.add(new JSeparator());
        ULpopup.add(ULpopIgnore);
        userListPane.setComponentPopupMenu(ULpopup);
        ULpopOpenQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User nick = userListPane.getSelectedValue();
                if (nick == null) return;
                String nickname = nick.getText();
                if (nickname.equals(Connection.currentNick)) return;
                try {
                    ChannelPanel channel = new ChannelPanel(nickname, nickname, ChannelPanel.this.connection);
                    channel.setRightComponent(null);
                    channel.setDividerSize(0);
                    tabbedPane.setSelectedComponent(channel);

                } catch (BadLocationException | IOException ex) {
                    Logger.getLogger(ChannelPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        ULpopWhois.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User nick = userListPane.getSelectedValue();
                if (nick == null) return;
                String nickname = nick.getText();
                if (nickname.equals(Connection.currentNick)) return;
                try {
                    ChannelPanel.this.connection.send("WHOIS " + nickname);
                } catch (IOException | BadLocationException ex) {
                    Logger.getLogger(ChannelPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        ULpopVersion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User nick = userListPane.getSelectedValue();
                if (nick == null) return;
                String nickname = nick.getText();
                if (nickname.equals(Connection.currentNick)) return;
                try {
                    ChannelPanel.this.connection.send("PRIVMSG " + nickname + " \001VERSION");
                } catch (IOException | BadLocationException ex) {
                    Logger.getLogger(ChannelPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        ULpopPing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Long longTime = System.currentTimeMillis() / 1000L;
                User nick = userListPane.getSelectedValue();
                if (nick == null) return;
                String nickname = nick.getText();
                if (nickname.equals(Connection.currentNick)) return;
                try {
                    ChannelPanel.this.connection.send("PRIVMSG " + nickname + " :\001PING " + longTime + "\001");
                } catch (IOException | BadLocationException ex) {
                    Logger.getLogger(ChannelPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        ULpopIgnore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User nick = userListPane.getSelectedValue();
                if (nick == null) return;
                String nickname = nick.getText();
                if (nickname.equals(Connection.currentNick)) return;
                if (ignoreList.contains(nick.getText())) {
                    String[] msg = {null, "*** Removed " + nickname + " from ignore list."};
                    try {
                        insertString(msg, GUI.serverStyle, false);
                    } catch (BadLocationException | IOException ex) {
                        Logger.getLogger(ChannelPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    nick.foreground = Color.black;
                    nick.setForeground(nick.foreground);
                } else {
                    ignoreList.add(nick.getText());
                    String[] msg = {null, "*** " + nickname + " added to ignore list."};
                    try {
                        insertString(msg, GUI.serverStyle, false);
                    } catch (BadLocationException | IOException ex) {
                        Logger.getLogger(ChannelPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    nick.foreground = Color.LIGHT_GRAY;
                    nick.setForeground(nick.foreground);
                }
            }
        });

        JPopupMenu CPpopup = new JPopupMenu();
        final JMenuItem CPCopy = new JMenuItem("Copy", GUI.copyIcon);
        JMenuItem CPFind = new JMenuItem("Find", GUI.findTextIcon);
        JMenuItem CPSelectAll = new JMenuItem("Select All", GUI.selectAllIcon);
        CPpopup.add(CPCopy);
        CPpopup.add(CPFind);
        CPpopup.add(CPSelectAll);
        chatPane.setComponentPopupMenu(CPpopup);
        CPCopy.addActionListener(new DefaultEditorKit.CopyAction());
        CPFind.addActionListener(GUI.FindTextAction.getInstance());
        CPSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        chatPane.selectAll();
                    }
                });
            }
        });
        CPpopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (chatPane.getSelectedText() == null) CPCopy.setForeground(Color.gray);
                    // if (chatPane.getSelectedText().isEmpty()) CPCopy.setForeground(Color.gray);
                else CPCopy.setForeground(Color.black);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        JPopupMenu CIPpopup = new JPopupMenu();
        final JMenuItem CIPCut = new JMenuItem("Cut", GUI.cutIcon);
        final JMenuItem CIPCopy = new JMenuItem("Copy", GUI.copyIcon);
        final JMenuItem CIPPaste = new JMenuItem("Paste", GUI.pasteIcon);
        final JMenuItem CIPClear = new JMenuItem("Clear", GUI.clearTextIcon);
        final JMenuItem CIPSelectAll = new JMenuItem("Select All", GUI.selectAllIcon);
        CIPpopup.add(CIPCut);
        CIPpopup.add(CIPCopy);
        CIPpopup.add(CIPPaste);
        CIPpopup.add(new JSeparator());
        CIPpopup.add(CIPClear);
        CIPpopup.add(CIPSelectAll);
        GUI.chatInputPane.setComponentPopupMenu(CIPpopup);

        CIPCopy.addActionListener(new DefaultEditorKit.CopyAction());
        CIPCut.addActionListener(new DefaultEditorKit.CutAction());
        CIPPaste.addActionListener(new DefaultEditorKit.PasteAction());
        CIPClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        GUI.chatInputPane.setText(null);
                    }
                });
            }
        });
        CIPSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        GUI.chatInputPane.selectAll();
                    }
                });
            }
        });
        CIPpopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (GUI.chatInputPane.getText().isEmpty()) {
                    CIPCut.setForeground(Color.gray);
                    CIPPaste.setForeground(Color.black);
                    CIPCopy.setForeground(Color.gray);
                    CIPSelectAll.setForeground(Color.gray);
                    CIPClear.setForeground(Color.gray);
                } else {
                    CIPCut.setForeground(Color.black);
                    CIPPaste.setForeground(Color.black);
                    CIPCopy.setForeground(Color.black);
                    CIPSelectAll.setForeground(Color.black);
                    CIPClear.setForeground(Color.black);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));


        setResizeWeight(1.0);
        setDividerSize(5);
        setVerifyInputWhenFocusTarget(false);

        chatPane.setEditable(false);
        chatPane.setAutoscrolls(true);
        userListScrollPane.setViewportView(userListPane);
        chatScrollPane.setViewportView(chatPane);


        setLeftComponent(chatScrollPane);
        if (name.startsWith("#")) {
            setRightComponent(userListScrollPane);
            setDividerLocation(GUI.frame.getWidth() - 160);
        } else {
            setRightComponent(null);
            setDividerSize(0);
        }

    }



    private static int compareNicks(String u1, String u2) {
        String user1 = u1.toLowerCase();
        String user2 = u2.toLowerCase();
        return user1.compareTo(user2);
    }




    public void closeTab() {
        String name = this.name;

        if (!name.startsWith("#") && !name.equals(this.server)) //closing IM
        {
            tabbedPane.remove(this);
        }

        if (name.startsWith("#")) //closing channel
        {
            try {
                this.connection.send("PART " + name+" "+GUI.leaveMessage);
                return;
            } catch (IOException | BadLocationException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (name.equals(this.server)) //closing server
        {
            if (this.connection.isConnected) {
                int warning = JOptionPane.showConfirmDialog(null, "Do you wish to disconnect from " + this.server + "? All tabs will be closed.", "Are you sure?", JOptionPane.WARNING_MESSAGE);
                if (warning == JOptionPane.CANCEL_OPTION || warning == JOptionPane.CLOSED_OPTION) return;
            }
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                ChannelPanel c = (ChannelPanel) tabbedPane.getComponentAt(i);
                if (c.server.equals(this.server)) {
                    tabbedPane.remove(i);
                    i--;
                }
            }
            tabbedPane.remove(this);
            this.connection.disconnect();
        }
    }



    /** Makes a timestamp for use by insertString */
    private String makeTimestamp() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        return sdf.format(date);
    }




    /** adds this ChannelPanel to tabbedPane */
    private void addTab() {
        if (!GUI.sortTabsAlphabetically) tabbedPane.add(this, this.title);
        else {
            int selection = -1;
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getTabCount() == 1) break;
                ChannelPanel c = (ChannelPanel) tabbedPane.getComponentAt(i);
                if ((c.title.equals(c.connection.title))) continue;
                if (this.title.compareTo(c.title) <= 0) {
                    selection = i;
                    break;
                }
            }
            if (selection == -1) {
                tabbedPane.add(this, this.title);
            } else {
                tabbedPane.add(this, selection);
                tabbedPane.setTitleAt(selection, title);
            }
        }
    }




    /** Retrieves a user's chat font color from the userMap */
    private Color getUserColor(String user) {
        if (userMap.containsKey(user)) return userMap.get(user);
        else {
            int random = (int) (Math.random() * 8);
            userMap.put(user, GUI.chatColorMap.get(random));
            return userMap.get(user);
        }
    }




    /** Updates the active component with info such as population, number of ops, etc */
    public void updateTabInfo() {
        if (tabbedPane.getTabCount() == 0) {
            GUI.frame.setTitle(GUI.appName);
            tabInfo.setText("Disconnected    ");
            return;
        }
        if (connection.socket.isClosed()) {
            tabInfo.setText("Disconnected    ");
            return;
        }
        ChannelPanel cc = (ChannelPanel) tabbedPane.getSelectedComponent();
        int sel = tabbedPane.getSelectedIndex();
        tabbedPane.setIconAt(sel, null);

        if (this.isShowing()) {
            String text = "";
            if (ops != 1) text = " ops) ";
            if (ops == 1) text = " op) ";

            if (!cc.name.startsWith("#")) {
                GUI.frame.setTitle(name + " - " + GUI.appName);
                tabInfo.setText(name + "  ");
            } else tabInfo.setText(name + " - " + population + " nicks (" + ops + text + server + "  ");
        }
        GUI.frame.setTitle(cc.title + " - " + GUI.appName);
    }




    /** Inserts str to chatPane at given offset using AttributeSet */
      public void insertString(String[] line, Style givenStyle, boolean isCTCP) throws BadLocationException, IOException {
        if (ignoreList.contains(line[0])) return; //change ignorelist to just hold strings and not users?
        if (line[1].contains("http://") || line[1].contains("https://") || line[1].contains("www.") || line[1].contains("#")) {
            line[1] = line[1].replaceAll("http://", Connection.HTTP_DELIM + "http://");
            line[1] = line[1].replaceAll("https://", Connection.HTTP_DELIM + "https://");
            line[1] = line[1].replaceAll("www.", Connection.HTTP_DELIM + "www.");
            if (line[1].startsWith("#")) line[1] = line[1].replace("#", Connection.HTTP_DELIM + "#");
            line[1] = line[1].replaceAll(" #", " " + Connection.HTTP_DELIM + "#");
            insertCTCPColoredString(line, givenStyle);
            return;
        }
        if (isCTCP) {
            insertCTCPColoredString(line, givenStyle);
            return;
        }
        if (GUI.showTimestamp) {
            String timestamp = makeTimestamp();
            this.insertString(doc.getLength(), "\n[" + timestamp + "] ", GUI.timestampStyle);
        } else this.insertString(doc.getLength(), "\n", GUI.style);
        if (!GUI.chatNameColors && line[0] != null) this.insertString(doc.getLength(), "<" + line[0] + ">: ", GUI.chatStyle);
        if (GUI.chatNameColors && line[0] != null) {
            Color c = getUserColor(line[0]);
            this.insertString(doc.getLength(), "<", GUI.style);
            StyleConstants.setForeground(GUI.userNameStyle, c);
            this.insertString(doc.getLength(), line[0], GUI.userNameStyle);
            this.insertString(doc.getLength(), ">: ", GUI.style);
        }
        this.insertString(doc.getLength(), line[1], givenStyle);
        checkForActiveTab();
    }




    /** Called internally by the other insertString method. Controls the scrolling behavior */
    private void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        if (doc.getLength() == 0) str = str.trim() + " "; //removes \n at start of document

        int extent = chatScrollPane.getVerticalScrollBar().getModel().getExtent();
        int max = chatScrollPane.getVerticalScrollBar().getModel().getMaximum();
        int val = chatScrollPane.getVerticalScrollBar().getModel().getValue();
        boolean end = ((val + extent == max) || val + extent > max - 50);

        doc.insertString(offset, str, a);


        //controls autoscrolling -- if scrolled to bottom then scroll to end, otherwise don't
        if (end) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (chatPane.getDocument().getLength() == 0) return;
                            try {
                                chatPane.getCaret().setDot(doc.getLength());
                            } catch (Exception ignored) {}
                        }
                    });
                }
            });
        }
    }




    /** Inserts a CTCP_Action when a CTCP Action command is sent from the server */
    public void insertCTCPAction(String[] line) throws BadLocationException {
        String nick = line[0];
        String msg = line[1].trim();
        String timestamp = makeTimestamp();
        if (GUI.showTimestamp) this.insertString(doc.getLength(), "\n[" + timestamp + "] ", GUI.timestampStyle);
        else this.insertString(doc.getLength(), "\n", GUI.style);
        this.insertString(doc.getLength(), "* ", GUI.actionStyle);
        if (!GUI.chatNameColors) this.insertString(doc.getLength(), nick + " ", GUI.style);
        else {
            Color c = getUserColor(nick);
            StyleConstants.setForeground(GUI.userNameStyle, c);
            this.insertString(doc.getLength(), nick + " ", GUI.userNameStyle);
        }
        this.insertString(doc.getLength(), msg, GUI.actionStyle);
        checkForActiveTab();
    }




    /** Inserts a CTCP colored string when a CTCP delimeter is found */
    public void insertCTCPColoredString(String[] line, Style givenStyle) throws BadLocationException {
        GUI.ctcpStyle = GUI.sc.addStyle("Defaultstyle", givenStyle);
        if (GUI.showTimestamp) {
            String timestamp = makeTimestamp();
            this.insertString(doc.getLength(), "\n[" + timestamp + "] ", GUI.timestampStyle);
        } else this.insertString(doc.getLength(), "\n", GUI.style);
        if (!GUI.chatNameColors && line[0] != null) this.insertString(doc.getLength(), "<" + line[0] + ">: ", GUI.chatStyle);
        if (GUI.chatNameColors && line[0] != null) {
            Color c = getUserColor(line[0]);
            this.insertString(doc.getLength(), "<", GUI.style);
            StyleConstants.setForeground(GUI.userNameStyle, c);
            this.insertString(doc.getLength(), line[0], GUI.userNameStyle);
            this.insertString(doc.getLength(), ">: ", GUI.style);
        }

        Pattern pattern;
        Matcher matcher;

        StringTokenizer st = new StringTokenizer(line[1], Connection.HTTP_DELIM + Connection.CTCP_DELIM + Connection.CTCP_COLOR_DELIM + Connection.CTCP_UNDERLINE_DELIM + Connection.CTCP_BOLD_DELIM + Connection.CTCP_RESET_DELIM, true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(Connection.CTCP_DELIM)) {
                continue;
            }
            if (token.equals(Connection.CTCP_BOLD_DELIM)) {
                StyleConstants.setBold(GUI.ctcpStyle, !StyleConstants.isBold(GUI.ctcpStyle));
                continue;
            }
            if (token.equals(Connection.CTCP_UNDERLINE_DELIM)) {
                StyleConstants.setUnderline(GUI.ctcpStyle, !StyleConstants.isUnderline(GUI.ctcpStyle));
                continue;
            }
            if (token.equals(Connection.CTCP_RESET_DELIM)) {
                GUI.ctcpStyle = GUI.sc.addStyle("Defaultstyle", givenStyle);
                continue;
            }
            if (token.equals(Connection.HTTP_DELIM)) {
                if (st.hasMoreTokens()) token = st.nextToken();
                if (token.startsWith(" #")) {
                    token = token.substring(1);
                }
                if (token.startsWith("#")) //hyperlinking a channel
                {
                    int[] ind = {token.indexOf('.'), //disallowed irc channel characters
                            token.indexOf(','),
                            token.indexOf(':'),
                            token.indexOf(';'),
                            token.indexOf('<'),
                            token.indexOf('>'),
                            token.indexOf('?'),
                            token.indexOf('/'),
                            token.indexOf('\''),
                            token.indexOf('"'),
                            token.indexOf(' ')
                    };

                    int min = Integer.MAX_VALUE;
                    for (int anInd : ind) {
                        if (anInd == -1) continue;
                        min = Math.min(anInd, min);
                    }
                    if (min == Integer.MAX_VALUE) {
                        this.insertString(doc.getLength(), token, GUI.hyperlinkUnclickedStyle);
                        continue;
                    }
                    String channel = token.substring(0, min);
                    this.insertString(doc.getLength(), channel, GUI.hyperlinkUnclickedStyle);
                    String rest = token.substring(min);
                    this.insertString(doc.getLength(), rest, GUI.ctcpStyle);
                    continue;
                } else //hyperlinking a URL
                {
                    int[] ind = {token.indexOf('"'), //disallowed URL characters
                            token.indexOf('`'),
                            token.indexOf(']'),
                            token.indexOf('['),
                            token.indexOf('>'),
                            token.indexOf('<'),
                            token.indexOf(','),
                            token.indexOf('('),
                            token.indexOf(')'),
                            token.indexOf('{'),
                            token.indexOf('}'),
                            token.indexOf(' ')
                    };

                    int min = Integer.MAX_VALUE;
                    for (int anInd : ind) {
                        if (anInd == -1) continue;
                        min = Math.min(anInd, min);
                    }
                    if (min == Integer.MAX_VALUE) {
                        this.insertString(doc.getLength(), token, GUI.hyperlinkUnclickedStyle);
                        continue;
                    }
                    String url = token.substring(0, min);
                    this.insertString(doc.getLength(), url, GUI.hyperlinkUnclickedStyle);
                    String rest = token.substring(min);
                    this.insertString(doc.getLength(), rest, GUI.ctcpStyle);
                    continue;
                }
            }
            if (token.equals(Connection.CTCP_COLOR_DELIM)) {
                if (st.hasMoreTokens()) {
                    token = st.nextToken();
                    pattern = Pattern.compile("(\\d{1,2}+)(,?+)(\\d{1,2}+)(.+)|(\\d{1,2})(,)(.*)|(,?+)(\\d{1,2}+)(.+)");
                    matcher = pattern.matcher(token);
                    String foreground;
                    String background;
                    String message;

                    if (!StringUtils.isNumeric(token.substring(0, 1)) && !token.startsWith(",")) {
                        this.insertString(doc.getLength(), token, GUI.chatStyle);
                        continue;
                    }
                    while (matcher.find()) {
                        if (matcher.group(8) != null && matcher.group(8).equals(",")) //invalid (ex. ,5this is a message) prints plain
                        {
                            message = matcher.group(10);
                            this.insertString(doc.getLength(), message, GUI.ctcpStyle);
                            continue;
                        }
                        if (matcher.group(6) != null && matcher.group(6).equals(",")) //foreground color, no bg color (ex. 5,this is a message)
                        {
                            foreground = matcher.group(5);
                            message = matcher.group(7);
                            Color f = GUI.CTCPMap.get(Integer.valueOf(foreground));
                            StyleConstants.setForeground(GUI.ctcpStyle, f);
                            this.insertString(doc.getLength(), message, GUI.ctcpStyle);
                            continue;
                        }
                        if (matcher.group(8) != null && matcher.group(8).equals("")) //foreground color, no bg color (ex. 5this is a message)
                        {
                            foreground = matcher.group(9);
                            message = matcher.group(10);
                            Color f = GUI.CTCPMap.get(Integer.valueOf(foreground));
                            StyleConstants.setForeground(GUI.ctcpStyle, f);
                            this.insertString(doc.getLength(), message, GUI.ctcpStyle);
                            continue;
                        }
                        if (matcher.group(2) != null && matcher.group(2).equals("")) //foreground color, no bg color, but followed by a number, ie 53 blind mice
                        {
                            foreground = matcher.group(1);
                            message = matcher.group(3) + matcher.group(4);
                            Color f = GUI.CTCPMap.get(Integer.valueOf(foreground));
                            StyleConstants.setForeground(GUI.ctcpStyle, f);
                            this.insertString(doc.getLength(), message, GUI.ctcpStyle);
                            continue;
                        }
                        if (matcher.group(2) != null && matcher.group(2).equals(",")) //foreground and background (ex. 5,5this is a message)
                        {
                            foreground = matcher.group(1);
                            background = matcher.group(3);
                            message = matcher.group(4);
                            Color f = GUI.CTCPMap.get(Integer.valueOf(foreground));
                            Color b = GUI.CTCPMap.get(Integer.valueOf(background));
                            StyleConstants.setForeground(GUI.ctcpStyle, f);
                            StyleConstants.setBackground(GUI.ctcpStyle, b);
                            this.insertString(doc.getLength(), message, GUI.ctcpStyle);
                        }
                    }
                }
            } else {
                this.insertString(doc.getLength(), token, GUI.ctcpStyle);
            }
        }
        checkForActiveTab();
    }




    /** Returns an ArrayList of users beginning with the search String for use by tab nick completion */
    public ArrayList getMatchingUsers(String text) {
        SortedListModel slm = (SortedListModel) userListPane.getModel();
        return slm.getMatchingUsers(text);
    }




    /** Updates tab icon based on channel status (unselected new message, disconnected, etc) */
    public void checkForActiveTab() {
        if (!this.connection.isConnected) {
            int totalTabs = tabbedPane.getTabCount();
            int indexOfTab = -1;
            for (int i = 0; i < totalTabs; i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);

                String channelName = channel.name;
                if (channelName.equalsIgnoreCase(this.name)) {
                    indexOfTab = i;
                    break;
                }
            }
            final int ind = indexOfTab;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tabbedPane.setIconAt(ind, GUI.notConnectedIcon);
                }
            });
            return;
        }
        if (!this.isShowing() && GUI.disableTabNotificationsGlobally && enableNotifications) {
            int totalTabs = tabbedPane.getTabCount();
            int indexOfTab = -1;
            for (int i = 0; i < totalTabs; i++) {
                ChannelPanel channel = (ChannelPanel) tabbedPane.getComponentAt(i);

                String channelName = channel.name;
                if (channelName.equalsIgnoreCase(this.name)) {
                    indexOfTab = i;
                    break;
                }
            }
            final int ind = indexOfTab;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tabbedPane.setIconAt(ind, GUI.newMessageIcon);
                }
            });
        } else {
            ChannelPanel channel = (ChannelPanel) tabbedPane.getSelectedComponent();
            if (channel == null) return;
            final int index = Connection.findTab(channel.name, connection);
            if (index == -1) return;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tabbedPane.setIconAt(index, null);

                }
            });
        }
    }




    /** Adds user to the user list while preserving selection */
    public void addToUserList(String nick) throws BadLocationException, IOException {
        listSelectedIndex = userListPane.getSelectedIndex();
        String s2;
        nick = nick.trim();
        char first = nick.charAt(0);
        SortedListModel<User> model = (SortedListModel<User>) this.userListPane.getModel();

        if (first == '+' || first == '@' || first == '&' || first == '%' || first == '~') {
            if (first != '+') ops++;
            s2 = nick.substring(1);
            User u = new User(first + nick.substring(1));
            model.addElement(u);
        } else {
            s2 = nick;
            model.addElement(new User(" " + nick));
        }


        population = model.getSize();

        if (listSelectedIndex > -1 && listSelectedIndex < model.getSize()) {
            User u1 = userListPane.getModel().getElementAt(listSelectedIndex);
            String s1 = u1.getText();
            int result = ChannelPanel.compareNicks(s1, s2);
            if (result == 0) userListPane.clearSelection();
            if (result > 0) {
                listSelectedIndex++;
                userListPane.setSelectedIndex(listSelectedIndex);
            } else userListPane.setSelectedIndex(listSelectedIndex);

        }
        updateTabInfo();
    }


    /** This is used over addToUserList at channel join to avoid firing intervalAdded
     *  for every user in the channel in short succession
     */
    public void addManyToUserList(String nick) {
        if (nick.charAt(0) != ' ' && nick.charAt(0) != '+') ops++;
        SortedListModel<User> model = (SortedListModel<User>) this.userListPane.getModel();
        model.addManyElements(new User(nick));
        updateTabInfo();
    }



    public boolean contains(Object o) {
        SortedListModel<User> model = (SortedListModel<User>) this.userListPane.getModel();
        String user = (String) o;
        User u = new User(user);
        return model.contains(u);
    }



    /** Used in conjunction with addManyToUserList */
    public void fireIntervalAdded() {
        SortedListModel<String> model = (SortedListModel<String>) this.userListPane.getModel();
        model.fireIntervalAdded();
        population = model.getSize();
        updateTabInfo();
    }



    /** Removes a user from user list while maintaining selection */
    public boolean removeFromUserList(String nick) throws BadLocationException, IOException {
        listSelectedIndex = userListPane.getSelectedIndex();

        String[] prefix = new String[]{" ", "+", "@", "~", "&"};
        SortedListModel<User> model = (SortedListModel<User>) this.userListPane.getModel();
        int oldPop = model.getSize();
        boolean success;

        for (String aPrefix : prefix) {
            User u = new User(aPrefix + nick);
            success = model.removeElement(u);
            if (success) {
                if (!aPrefix.equals(" ") && aPrefix.equals("+")) ops--;
                break;
            }
        }
        population = model.getSize();
        if (listSelectedIndex > -1 && listSelectedIndex < model.getSize()) {
            User u1 = userListPane.getModel().getElementAt(listSelectedIndex);
            String s1 = u1.getText();
            int result = ChannelPanel.compareNicks(s1, nick);
            if (result == 0) userListPane.clearSelection();
            if (result > 0) {
                userListPane.setSelectedIndex(--listSelectedIndex);
            } else userListPane.setSelectedIndex(listSelectedIndex);

        }
        updateTabInfo();

        return !(oldPop == population);
    }



    /** Clears user list and related fields */
    public void clear() {
        SortedListModel<User> model = (SortedListModel<User>) userListPane.getModel();
        ops = 0;
        population = 0;
        model.removeAll();
    }




    /** The model used for the JList of users in a chat room */
    public class SortedListModel<User> extends AbstractListModel {
        private SortedSet<User> set;
        private NickComparator comparator;

        public SortedListModel() {
            comparator = new NickComparator();
            set = new TreeSet<User>(comparator);
        }

        @Override
        public synchronized int getSize() {
            return set.size();
        }

        @Override
        public synchronized User getElementAt(int index) {
            return (User) set.toArray()[index];
        }

        public synchronized boolean contains(Object o) {
            User u = (User) o;
            return set.contains(u);
        }

        public synchronized boolean addElement(User x) {
            boolean success = set.add(x);
            fireIntervalAdded(this, 0, set.size() - 1);
            return success;
        }

        public synchronized void addManyElements(User x) {
            set.add(x);
        }

        public synchronized void fireIntervalAdded() {
            fireIntervalAdded(this, 0, 0);
        }

        public synchronized boolean removeElement(User x) {
            boolean success = set.remove(x);
            if (success) fireIntervalRemoved(this, 0, 0);
            return success;
        }

        public void removeAll() {
            set.clear();
            fireIntervalRemoved(this, 0, set.size());
        }

        public ArrayList<String> getMatchingUsers(String text) {
            if (text.isEmpty()) return new ArrayList<>();

            ArrayList<String> users = new ArrayList<>();
            for (User u : set) {
                JLabel l = (JLabel) u;
                String str = l.getText();
                if (StringUtils.startsWithIgnoreCase(str, text)) users.add(str);
                if (str.charAt(0) > text.charAt(0)) break;
            }
            return users;
        }
    }



    public class NickComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            String p1 = ((User) o1).getText().toLowerCase();
            String p2 = ((User) o2).getText().toLowerCase();
            return p1.compareTo(p2);
        }
    }
}




/** The renderer for the JList of users in a chat room */
class CustomRenderer extends JLabel implements ListCellRenderer {
    final static ImageIcon iconWhite = new ImageIcon("src/icons/user-white.png");
    final static ImageIcon iconGreen = new ImageIcon("src/icons/user.png");
    final static ImageIcon iconOrange = new ImageIcon("src/icons/user-female.png");
    final static ImageIcon iconPurple = new ImageIcon("src/icons/user-red.png");
    final static ImageIcon iconRed = new ImageIcon("src/icons/user-green.png");
    final static ImageIcon iconBlue = new ImageIcon("src/icons/user-gray.png");
    Color color = Color.decode("#8FAE70");

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ImageIcon icon = iconWhite;
        User user = (User) value;
        if (user.mode == '@') icon = iconGreen;
        if (user.mode == '%') icon = iconOrange;
        if (user.mode == '&') icon = iconPurple;
        if (user.mode == '~') icon = iconRed;
        if (user.mode == '+') icon = iconBlue;

        if (isSelected) {
            user.setBackground(color);
            user.setForeground(list.getBackground());
        } else {
            user.setBackground(list.getBackground());
            user.setForeground(Color.black);
        }
        user.setIcon(icon);
        return user;
    }
}



/** each channel has a JList of Users */
class User extends JLabel implements Comparable {
    public static Font font = Font.decode(GUI.userListFont+"-"+GUI.userListFontStyle+"-"+GUI.userListFontSize);
    final char mode;
    Color foreground = Color.black;

    public User(String nick) {
        super(nick.substring(1));
        setOpaque(true);
        mode = nick.charAt(0);
        setFont(font);
        setForeground(foreground);
    }

    @Override
    public int compareTo(Object o) {
        User u = (User) o;
        return this.getText().compareTo(u.getText());
    }
    public static void changeFont(Font f)
    {
        if (ChannelPanel.tabbedPane == null) return;
        User.font = f;
    }

}




/** Hyperlink support for the chat pane */
class PlainTextHyperlinkListener implements HyperlinkListener {
    JTextPane textPane;

    public PlainTextHyperlinkListener(JTextPane textPane) {
        this.textPane = textPane;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        HyperlinkEvent.EventType type = evt.getEventType();
        final URL url = evt.getURL();
        if (type == HyperlinkEvent.EventType.ENTERED) System.out.println("URL: " + url);
        if (type == HyperlinkEvent.EventType.ACTIVATED) System.out.println("Activated");
    }
}




/* Changes cursor on mouseover of the hyperlink style */
class TextMotionListener extends MouseInputAdapter {
    JTextPane textPane;

    public TextMotionListener(JTextPane textPane) {
        this.textPane = textPane;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Element elem = textPane.getStyledDocument().getCharacterElement(textPane.viewToModel(e.getPoint()));
        AttributeSet as = elem.getAttributes();
        if (StyleConstants.isUnderline(as))
            textPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
        else
            textPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}




/** Extracts the hyperlink text when clicking a hyperlink */
class TextClickListener extends MouseAdapter {
    JTextPane textPane;
    Connection connection;

    public TextClickListener(JTextPane textPane, Connection connection) {
        this.textPane = textPane;
        this.connection = connection;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            Element elem = textPane.getStyledDocument().getCharacterElement(textPane.viewToModel(e.getPoint()));
            boolean contains = elem.getAttributes().containsAttributes(GUI.hyperlinkUnclickedStyle);
            if (contains) {
                StyledDocument document = textPane.getStyledDocument();
                Point pt = new Point(e.getX(), e.getY());
                int originalClickPoint = textPane.viewToModel(pt);
                boolean isURL = true;
                String URLString = "";
                int backwardsClickPoint = originalClickPoint - 1;
                while (true) {
                    elem = document.getCharacterElement(originalClickPoint);
                    if (elem.getAttributes().containsAttributes(GUI.hyperlinkUnclickedStyle)) {
                        URLString = URLString + (document.getText(originalClickPoint, 1));
                    } else break;
                    originalClickPoint++;
                }

                while (isURL) {
                    elem = document.getCharacterElement(backwardsClickPoint);
                    if (elem.getAttributes().containsAttributes(GUI.hyperlinkUnclickedStyle)) {
                        URLString = document.getText(backwardsClickPoint, 1) + URLString;
                    } else isURL = false;
                    backwardsClickPoint--;
                }
                URLLinkAction linkAction = new URLLinkAction(URLString, connection);
                linkAction.execute();
            }
        } catch (BadLocationException | IOException ignored) {
        }
    }
}



/** Handles the action to take when a hyperlink is clicked. i.e. open a new channel, open browser, etc */
class URLLinkAction extends AbstractAction {
    private String url;
    private Connection connection;

    URLLinkAction(String bac, Connection connection) {
        url = bac;
        this.connection = connection;
    }

    protected void execute() throws IOException {
        if (url.startsWith("#"))
            try {
                connection.send("JOIN " + url);
            } catch (IOException | BadLocationException ex) {
                Logger.getLogger(URLLinkAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        else {
            String osName = System.getProperty("os.name").toLowerCase();
            Runtime rt = Runtime.getRuntime();
            if (osName.contains("win")) rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            else if (osName.contains("mac")) rt.exec("open " + url);
            else if (osName.contains("ix") || osName.contains("ux") || osName.contains("sun")) {
                String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx"};

                // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++)
                    cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");

                rt.exec(new String[]{"sh", "-c", cmd.toString()});
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            execute();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}