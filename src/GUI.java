import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang3.StringUtils;

public class GUI extends JFrame {
    final static ImageIcon mainIcon = new ImageIcon("src/icons/weather-sun.png");
    final static ImageIcon quickConnectIcon = new ImageIcon("src/icons/connect.png");
    final static ImageIcon identitiesIcon = new ImageIcon("src/icons/edit-group.png");
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
    
    EditorKit editorKit;
    Connection c;
    final static Properties prop = new Properties();
    static ArrayList<SavedConnection> savedConnections = new ArrayList<SavedConnection>();
    OutputStream output = null;

    public GUI() {
        super("Alpha IRC 0.1");
        setIconImage(mainIcon.getImage());
        
        loadProperties();
        initComponents();
        ChannelPanel.tabbedPane = tabbedPane;
        Connection.tabbedPane = tabbedPane;
        ChannelPanel.tabInfo = tabInfo;
        Connection.tabInfo = tabInfo;
        autoConnect();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        
        
        chatInputPane = new JTextField();
        tabbedPane = new DnDTabbedPane();
        jSplitPane1 = new JSplitPane();
        jScrollPane2 = new JScrollPane();
        jScrollPane1 = new JScrollPane();
        userListPane = new JTextPane();
        tabInfo = new JLabel();
        jMenuBar2 = new JMenuBar();
        fileMenu = new JMenu("File");
	fileMenu.setMnemonic('F');
        editMenu = new JMenu("Edit");
	editMenu.setMnemonic('E');
        windowMenu = new JMenu("Window");
	windowMenu.setMnemonic('W');
        settingsMenu = new JMenu("Settings");
	settingsMenu.setMnemonic('S');
        
        copyAction = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyAction.setText("Copy");
        copyAction.setIcon(copyIcon);
        editMenu.add(copyAction);
        cutAction = new JMenuItem((new DefaultEditorKit.CutAction()));
        cutAction.setText("Cut");
        cutAction.setIcon(cutIcon);
        editMenu.add(cutAction);
        pasteAction = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteAction.setText("Paste");
        pasteAction.setIcon(pasteIcon);
        editMenu.add(pasteAction);
        editMenu.add(new JSeparator());
        clearWindow = new JMenuItem();
        editMenu.add (clearWindow);
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
        identities = new JMenuItem();
        showNickList = new JMenuItem();
        settingsMenu.add(showNickList);
        settingsMenu.add(identities);        
        reconnect = new JMenuItem();
        fileMenu.add(reconnect);
        joinChannel = new JMenuItem();
        fileMenu.add(joinChannel);
        fileMenu.add(new JSeparator());
        globalAway = new JMenuItem();
        fileMenu.add(globalAway);
        fileMenu.add(new JSeparator());
        quitProgram = new JMenuItem();
        fileMenu.add(quitProgram);
	

	quitProgram.setAction(new QuitProgramAction("Quit", quitProgramIcon, null, KeyEvent.VK_Q));
	joinChannel.setAction(new JoinChannelAction("Join Channel", joinChannelIcon, null, KeyEvent.VK_J));
	clearWindow.setAction(new ClearWindowAction("Clear Window", null, null, KeyEvent.VK_C));
	clearAllWindows.setAction(new ClearAllWindowsAction("Clear All Windows", null, null, KeyEvent.VK_A));
	clearAllWindows.setDisplayedMnemonicIndex(6);	
	previousTab.setAction(new PreviousTabAction("PreviousTab", prevTabIcon, null, KeyEvent.VK_P));
	nextTab.setAction(new NextTabAction("Next Tab", nextTabIcon, null, KeyEvent.VK_N));
	moveTabLeft.setAction(new MoveTabLeftAction("Move Tab Left",moveTabLeftIcon, null, KeyEvent.VK_L));
	moveTabRight.setAction(new MoveTabRightAction("Move Tab Right", moveTabRightIcon, null, KeyEvent.VK_R));
	closeTab.setAction(new CloseTabAction("Close Tab", closeTabIcon, null, KeyEvent.VK_T));
	globalAway.setAction(new GlobalAwayAction("Global Away", globalAwayIcon, null, KeyEvent.VK_A));
	globalAway.setDisplayedMnemonicIndex(7);      
	disconnect.setAction(new DisconnectAction("Disconnect", disconnectIcon, null, KeyEvent.VK_D));
	reconnect.setAction(new ReconnectAction("Reconnect", reconnectIcon, null, KeyEvent.VK_R));
	quickConnect.setAction(new QuickConnectAction("Quick Connect", quickConnectIcon, null, KeyEvent.VK_Q));
	showNickList.setAction(new ShowNickListAction("Show/Hide Nicklist", showNicklistIcon, null, KeyEvent.VK_N));
	identities.setAction(new IdentitiesAction("Identities", identitiesIcon, null, KeyEvent.VK_I));
	serverList.setAction(new ServerListAction("Server List", serverListIcon, null, KeyEvent.VK_L));
	channelList.setAction(new ChannelListAction("Channel List", channelListIcon, null, KeyEvent.VK_L));
	channelList.setDisplayedMnemonicIndex(8);

	
	chatInputPane.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                chatInputPaneKeyPressed(evt);
            }
        });  
	
        ChangeListener changeListener = new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent changeEvent)
            {
                JTabbedPane pane = (JTabbedPane)changeEvent.getSource();
                int index = pane.getSelectedIndex();
                if (pane == null || index == -1) return;
                ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(index);
                if (!channel.connection.isConnected) pane.setForegroundAt(index, Color.gray);
                else pane.setForegroundAt(index, Color.BLACK);
                if (index == 0)
                {
                    moveTabLeft.setForeground(Color.gray);
                    previousTab.setForeground(Color.gray);
                }
                else
                {
                    moveTabLeft.setForeground(Color.black);
                    previousTab.setForeground(Color.black);                     
                }
                if (index == tabbedPane.getTabCount()-1)
                {
                    moveTabRight.setForeground(Color.gray);
                    nextTab.setForeground(Color.gray);
                }
                else 
                {
                    moveTabRight.setForeground(Color.black);
                    nextTab.setForeground(Color.black);
                }
                channel.updateTabInfo();
            }          
        };
        tabbedPane.addChangeListener(changeListener);
	
	ContainerListener containerListener = new ContainerListener() {
	    @Override
	    public void componentAdded(ContainerEvent e) {
		int index = tabbedPane.getSelectedIndex();
		if (index == -1) return;
                ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(index);

                if (index == 0)
                {
                    moveTabLeft.setForeground(Color.gray);
                    previousTab.setForeground(Color.gray);
                }
                else
                {
                    moveTabLeft.setForeground(Color.black);
                    previousTab.setForeground(Color.black);                     
                }
                if (index == tabbedPane.getTabCount()-1)
                {
                    moveTabRight.setForeground(Color.gray);
                    nextTab.setForeground(Color.gray);
                }
                else 
                {
                    moveTabRight.setForeground(Color.black);
                    nextTab.setForeground(Color.black);
                }
                channel.updateTabInfo();	    
	    }

	    @Override
	    public void componentRemoved(ContainerEvent e) {
		int index = tabbedPane.getSelectedIndex();
		if (index == -1) return;
                ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(index);

                if (index == 0)
                {
                    moveTabLeft.setForeground(Color.gray);
                    previousTab.setForeground(Color.gray);
                }
                else
                {
                    moveTabLeft.setForeground(Color.black);
                    previousTab.setForeground(Color.black);                     
                }
                if (index == tabbedPane.getTabCount()-1)
                {
                    moveTabRight.setForeground(Color.gray);
                    nextTab.setForeground(Color.gray);
                }
                else 
                {
                    moveTabRight.setForeground(Color.black);
                    nextTab.setForeground(Color.black);
                }
                channel.updateTabInfo();		
	    }
	};
		
	
	
	ActionMap amap = chatInputPane.getActionMap();
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
	amap.put("identities", identities.getAction());
	amap.put("serverList", serverList.getAction());
	
	InputMap imap = chatInputPane.getInputMap();
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK), "showNickList");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK | Event.SHIFT_MASK), "globalAway");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK), "quitProgram");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, Event.CTRL_MASK), "joinChannel");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK), "clearWindow");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK | Event.SHIFT_MASK), "clearAllWindows");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.SHIFT_MASK), "previousTab");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.SHIFT_MASK), "nextTab");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK), "closeTab");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.CTRL_MASK), "moveTabLeft");
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.CTRL_MASK), "moveTabRight");
	imap.put(KeyStroke.getKeyStroke("F5"), "channelList");
	imap.put(KeyStroke.getKeyStroke("F7"), "quickConnect");
	imap.put(KeyStroke.getKeyStroke("F8"), "identities");
	imap.put(KeyStroke.getKeyStroke("F2"), "serverList");

	tabbedPane.getInputMap().setParent(imap);
	tabbedPane.getActionMap().setParent(amap);
	
	
	
	tabbedPane.addContainerListener(containerListener);
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        tabbedPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabbedPane.setToolTipText("");
        tabbedPane.setCursor(new java.awt.Cursor(Cursor.DEFAULT_CURSOR)); //cursor necessary?
        tabbedPane.setFocusable(false);
        tabbedPane.setPreferredSize(new Dimension(600, 450));

        jSplitPane1.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSplitPane1.setDividerLocation(480);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setVerifyInputWhenFocusTarget(false);

        
        jSplitPane1.setLeftComponent(jScrollPane2);
        userListPane.setEditable(false);
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new Dimension(25, 25));
        jScrollPane1.setViewportView(userListPane);

        jSplitPane1.setRightComponent(jScrollPane1);

        tabInfo.setHorizontalAlignment(SwingConstants.RIGHT);

        jMenuBar2.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jMenuBar2.setFocusable(false);

        jMenuBar2.add(fileMenu);
        jMenuBar2.add(editMenu);
        jMenuBar2.add(settingsMenu);
        jMenuBar2.add(windowMenu);
        

        setJMenuBar(jMenuBar2);

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
    private void loadProperties()
    {
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
		ex.printStackTrace();
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}        
    }
    private void autoConnect()
    {
        for (int i = 0; i < savedConnections.size(); i++)
        {
            SavedConnection conn = savedConnections.get(i);
            if (conn.retrieveAutoConnect() == true)
            {
                new Connection(conn.retrieveName(), conn.retrieveServer(), conn.retrievePort(), true);
            }
        }
    }
    public static void serializeSavedConnections()
    {
        String filename = "servers.ser";
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(savedConnections);
            
            out.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static void deserializeSavedConnections()
    {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try{
        fis = new FileInputStream("servers.ser");
        in = new ObjectInputStream(fis);
        savedConnections = (ArrayList<SavedConnection>)in.readObject();
        } catch (Exception ex){
            System.out.println("Deserialization exception");
        }
    }
    private void joinOKButtonFunctionality(JDialog dialog, String choice, String server)
    {
        if (!server.startsWith("#")) server = "#"+server;
        if (choice == null){
            JOptionPane.showConfirmDialog(dialog, "Please choose a connection.","No connection chosen", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ChannelPanel channel = null;
        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            channel = ((ChannelPanel)tabbedPane.getComponentAt(i));
            if (channel.server.equals(choice)){
                break;
            }
        }
        try {
            channel.connection.send("JOIN "+server);
            dialog.dispose();
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadLocationException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void quickConnectButtonFunctionality(JTextField[] panes, JDialog dialog)
    {
        String name = panes[0].getText().trim();
        String chan = panes[1].getText().trim();
        String p = panes[2].getText().trim();
        String n = panes[3].getText().trim();
        String pass = panes[4].getText().trim();

        if (name.isEmpty() || chan.isEmpty() || p.isEmpty() || n.isEmpty())
        {
            JOptionPane.showMessageDialog(dialog, "Network name, server, port and nick are required");
        }
        else
        {
            dialog.dispose();
            Connection.nicks[0] = n;

            for (int i = 0; i < tabbedPane.getTabCount(); i++)
            {
                ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
                if (name.equals(channel.title))
                {
                    tabbedPane.setSelectedComponent(channel);
                    String[] msg = {null, "You are already connected to "+name+"."};
                    try {
                        channel.insertString(msg, ChannelPanel.errorStyle, false);
                    } catch (BadLocationException | IOException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return;
                }
            }

            new Connection(name, chan, Integer.valueOf(p), false);
            if (!pass.isEmpty()) c.password = pass;
        }
    }
    private void identityButtonFunctionality(JTextField[] panes, JDialog dialog)
    {
        try {
            prop.load(new FileInputStream("config.properties"));
            prop.setProperty("Real", panes[0].getText().trim());
            prop.setProperty("Nick", panes[1].getText().trim());
            prop.setProperty("Second", panes[2].getText().trim() );
            prop.setProperty("Third", panes[3].getText().trim());
            prop.store(new FileOutputStream("config.properties"), null);

            } catch (IOException io) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, io);
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        dialog.dispose();

    }
    private void chatInputPaneKeyPressed(KeyEvent evt)
    {
        ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
        if (channel == null) return;
        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            if (chatInputPane.getText().equals("")) return;
            try
            {
            String msg = chatInputPane.getText();
            String target = channel.name;
            channel.history.add(msg);
            String output = "PRIVMSG "+target+" :"+msg;
            channel.historyCounter = channel.history.size();
            
            if (msg.charAt(0) != '/')
            {          
                channel.connection.send(output);
                String[] inp = {Connection.currentNick,msg.trim()};
                channel.insertString(inp,ChannelPanel.chatStyle, false);   
                chatInputPane.setText(null);
                evt.consume();
                return;
            }
            else
            {
                channel.connection.send(msg.substring(1));
                chatInputPane.setText(null);
                evt.consume();
            }
                       
            } catch(IOException e){
                try {   
                    String[] msg = {null, "[Error] *** You are not connected to the server."};
                    channel.insertString(msg,ChannelPanel.errorStyle, false);
                    chatInputPane.setText(null);
                    evt.consume();
                } catch (BadLocationException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (evt.getKeyCode() == KeyEvent.VK_UP)
        {
            if (channel.history.size() <= 0) return;
            String msg = "";
            if (channel.historyCounter > 0) msg = channel.history.get(channel.historyCounter-1);
            chatInputPane.setText(msg);
            if (channel.historyCounter > 0) channel.historyCounter--;
            return;
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN)
        {
            if (channel.historyCounter == channel.history.size()) return;
            String msg = "";
            if (channel.historyCounter < channel.history.size()) msg = channel.history.get(channel.historyCounter);
            chatInputPane.setText(msg);
            if (channel.historyCounter < channel.history.size()) channel.historyCounter++;
            return;
        }
    }
    
    private void serverListAddButtonFunctionality(JDialog dialog, BeanTableModel model)
    {
        JTextField name = new JTextField();
        JTextField server = new JTextField();
        JTextField port = new JTextField();
        JTextField password = new JTextField();
        JTextField channels = new JTextField();
        JCheckBox autoconnect = new JCheckBox();
        JCheckBox secure = new JCheckBox();

        Object[] fields = {
            "Name", name,
            "Server", server,
            "Port", port,
            "Password", password,
            "Auto Join Channels (#c1 #c2)", channels,
            "Connect on startup", autoconnect,
            "Secure connection (SSL)", secure
        };
        int x = JOptionPane.showConfirmDialog(dialog, fields, "Add Server", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.CLOSED_OPTION || x == JOptionPane.CANCEL_OPTION) return;
        
        String[] s = channels.getText().split(" ");
        ArrayList<String> channelList = new ArrayList<String>(Arrays.asList(s));
        if (!StringUtils.isNumeric(port.getText())){
            JOptionPane.showMessageDialog(dialog, "Port must be an integer.");
            return;
        }
        SavedConnection connection = new SavedConnection(name.getText(), server.getText(), password.getText(), channelList, autoconnect.isSelected(), secure.isSelected(), Integer.valueOf(port.getText().trim()));
        model.addRow(connection);
        savedConnections.add(connection);
        serializeSavedConnections();
        return;
    }

    
    
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
        
    }

    private JTextField chatInputPane;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu settingsMenu;
    private JMenu windowMenu;
    private JMenuBar jMenuBar2;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JSplitPane jSplitPane1;
    private static JLabel tabInfo;
    private static DnDTabbedPane tabbedPane;
    private JTextPane userListPane;
    
    private JMenuItem copyAction;
    private JMenuItem cutAction;
    private JMenuItem pasteAction;
    private JMenuItem quickConnect;
    private JMenuItem identities;
    private JMenuItem serverList;
    private JMenuItem clearWindow;
    private JMenuItem clearAllWindows;
    private JMenuItem showNickList;
    private JMenuItem previousTab;
    private JMenuItem nextTab;
    private JMenuItem moveTabLeft;
    private JMenuItem moveTabRight;
    private JMenuItem closeTab;
    private JMenuItem channelList;
    private JMenuItem disconnect;
    private JMenuItem reconnect;
    private JMenuItem globalAway;
    private JMenuItem joinChannel;
    private JMenuItem quitProgram;
    
    
    
    class ChannelListAction extends AbstractAction{
	public ChannelListAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {

	    final ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();

	    JDialog dialog = new JDialog(GUI.this, "Channel List");
	    SpringLayout layout = new SpringLayout();
	    JPanel panel = new JPanel(layout);

	    dialog.add(panel);
	    dialog.setPreferredSize(new Dimension(600,400));
	    dialog.pack();
	    dialog.setLocationRelativeTo(tabbedPane);
	    dialog.setVisible(true);

	    Container contentpane = dialog.getContentPane();
	    contentpane.setLayout(layout);
	    final BeanTableModel model = new BeanTableModel(ListChannel.class);
	    model.sortColumnNames();
	    for (int i = 0; i < channel.connection.channelList.size(); i++) model.addRow(channel.connection.channelList.get(i));

	    JButton joinButton = new JButton("Join Channel");
	    JButton refreshList = new JButton("Refresh List");
	    final JTextArea filterArea = new JTextArea();
	    filterArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);
	    final JTable table = new JTable(model){
		public boolean isCellEditable(int row, int column){  
		    return false;  
		}  
	    };

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
	    table.getColumnModel().getColumn(1).setMaxWidth(160);
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

	    layout.putConstraint(SpringLayout.WEST, scrollPane , 5, SpringLayout.WEST, contentpane);
	    layout.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, contentpane);
	    layout.putConstraint(SpringLayout.NORTH, scrollPane, 5, SpringLayout.NORTH, contentpane);
	    layout.putConstraint(SpringLayout.SOUTH, scrollPane, -40, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.EAST, joinButton, -7, SpringLayout.EAST, contentpane);
	    layout.putConstraint(SpringLayout.SOUTH, joinButton , -8, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.SOUTH, refreshList , -8, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.EAST, refreshList, -7, SpringLayout.WEST, joinButton);
	    layout.putConstraint(SpringLayout.SOUTH, filterArea , -10, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.WEST, filterArea, 7, SpringLayout.WEST, contentpane);
	    layout.putConstraint(SpringLayout.EAST, filterArea, -10, SpringLayout.WEST, refreshList);
	    layout.putConstraint(SpringLayout.NORTH, filterArea, 10, SpringLayout.SOUTH, scrollPane);



	    final RowFilter<Object, Object> filter = new RowFilter<Object, Object>(){
		public boolean include(RowFilter.Entry entry){
		    String filter = filterArea.getText();
		    for (int i = entry.getValueCount() - 1; i >= 0; i--){
			if (entry.getStringValue(i).contains(filter)){
			    return true;
			}
		    }
		    return false;
		}
	    };


	    refreshList.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		     try {
			channel.connection.send("LIST");
			Thread.sleep(100);
			while (channel.connection.currentlyUpdating)
			{
			    Thread.sleep(100);
			}

			if (channel.connection.doneUpdating)
			{
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
		    String chan = (String)model.getValueAt(row, 1);

		    int index = Connection.findTab(chan, channel.connection); //if already in channel
		    if (index != -1)
		    {
			tabbedPane.setSelectedIndex(index);
			return;
		    }

		    try {
			channel.connection.send("JOIN "+chan);
		    } catch (IOException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		    } catch (BadLocationException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
	    });


	    filterArea.addKeyListener(new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e)
		{
		    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
		    sorter.setRowFilter(filter);
		    table.setRowSorter(sorter);
		}
	    });
	}
    }
    
    class ClearAllWindowsAction extends AbstractAction{
	public ClearAllWindowsAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}	
	@Override
	public void actionPerformed(ActionEvent e) {
	    for (int i = 0; i < tabbedPane.getTabCount(); i++)
	    {
		ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
		try {
		   channel.chatPane.getDocument().remove(0, channel.chatPane.getDocument().getLength());
		} catch (BadLocationException ex) {
		   Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }                  
	}
    }
    class ClearWindowAction extends AbstractAction{
	public ClearWindowAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override    
	public void actionPerformed(ActionEvent e) {
	    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
	    try {
		channel.chatPane.getDocument().remove(0, channel.chatPane.getDocument().getLength());
	    } catch (BadLocationException ex) {
		Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }
    class CloseTabAction extends AbstractAction{
	public CloseTabAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    int index = tabbedPane.getSelectedIndex();
	    if (index == -1) return;
	    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
	    if (channel == null) return;
	    channel.closeTab();
	}
    }
    
    class DisconnectAction extends AbstractAction{
	public DisconnectAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    int index = tabbedPane.getSelectedIndex();
	    if (index == -1) return;
	    ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
	    if (!channel.connection.isConnected) return;
	    channel.model.removeAll();
	    channel.connection.disconnect();
	    for (int i = 0; i < tabbedPane.getTabCount(); i++)
	    {
		ChannelPanel otherChannel = (ChannelPanel)tabbedPane.getComponentAt(i);

		if (otherChannel.name.equals(channel.server))
		{
		    try {
			String[] msg = {null, "[Info] Disconnected from "+otherChannel.server+" (port "+otherChannel.connection.port+")"};
			otherChannel.insertString(msg, ChannelPanel.serverStyle, false);
			continue;
		    } catch (BadLocationException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		    } catch (IOException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
		if (otherChannel.connection == channel.connection)
		{
		    tabbedPane.setForegroundAt(i, Color.gray);
		    otherChannel.model.removeAll();
		}

	    }
	    channel.updateTabInfo();
	    return;
	}

    }
    
    class GlobalAwayAction extends AbstractAction{
	public GlobalAwayAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	    
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    for (int i = 0; i < tabbedPane.getTabCount(); i++)
	    {
		ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
		String name = channel.name;
		String server = channel.server;
		String message = "";
		if (name.equals(server))
		{
		    if (ChannelPanel.awayStatus == true){
			message = "AWAY";
			ChannelPanel.awayStatus = false;
		    }
		    else
		    {
			message = "AWAY "+ChannelPanel.awayMessage;
			ChannelPanel.awayStatus = true;
		    }
		    try {
			channel.connection.send(message);
		    } catch (IOException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		    } catch (BadLocationException ex) {
			Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
	    }                

	}
    }   
    class IdentitiesAction extends AbstractAction{
	public IdentitiesAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    final JDialog dialog = new JDialog(GUI.this);
	    SpringLayout layout = new SpringLayout();
	    JPanel panel = new JPanel(layout);
	    String[] labelVal = {Connection.real, Connection.nicks[0], Connection.nicks[1], Connection.nicks[2]};
	    String[] labels = {"Real Name", "Nick", "Second choice", "Third choice"};
	    int numPairs = labels.length;
	    final JTextField[] panes = new JTextField[numPairs];

	    for (int i = 0; i < numPairs; i++)
	    {
		JLabel l = new JLabel(labels[i], JLabel.TRAILING);
		panel.add(l);
		JTextField textField = new JTextField(10);
		textField.setText(labelVal[i]);
		panes[i] = textField;
		textField.setMaximumSize(new Dimension(10,10));
		l.setLabelFor(textField);
		panel.add(textField);
	    }

	    dialog.setTitle("Identities");
	    dialog.setSize(new Dimension(220,180));
	    dialog.setResizable(false);
	    dialog.setLocationRelativeTo(tabbedPane);                              
	    dialog.add(panel);
	    dialog.setVisible(true);
	    JButton saveButton = new JButton("Save");
	    saveButton.setPreferredSize(new Dimension(80,24));
	    panel.add(saveButton);
	    layout.putConstraint(SpringLayout.SOUTH, saveButton, 0, SpringLayout.SOUTH, dialog);
	    layout.putConstraint(SpringLayout.EAST, saveButton, -39, SpringLayout.EAST, dialog);
	    SpringUtilities.makeCompactGrid(panel, numPairs, 2, 6, 6, 10, 10); //rows, cols, initX, initY, xPad, yPad

	    saveButton.addKeyListener(new KeyAdapter(){
	    public void keyPressed(KeyEvent evt)
	    {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER)
		{
		    identityButtonFunctionality(panes, dialog);
		}
	    }
	     });

	    saveButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e)
		{
		    identityButtonFunctionality(panes,dialog);
		}

	    });
	}
    }
    class JoinChannelAction extends AbstractAction{
	public JoinChannelAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}	
	@Override
	public void actionPerformed(ActionEvent e) {

	    SortedSet set = new TreeSet();
	    for (int i = 0; i < tabbedPane.getTabCount(); i++)
	    {
		ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
		String srv = channel.server;
		set.add(srv);
	    }
	    Object[] things = set.toArray();


	    final JDialog dialog = new JDialog(GUI.this);
	    dialog.setTitle("Join Channel");
	    dialog.setSize(new Dimension(330,220));
	    SpringLayout layout = new SpringLayout();
	    JPanel panel = new JPanel(layout);
	    dialog.setResizable(false);
	    dialog.setLocationRelativeTo(tabbedPane);  
	    dialog.add(panel);
	    dialog.setVisible(true);
	    final JComboBox combobox = new JComboBox(things);
	    JLabel serverLabel = new JLabel("Connection");
	    JLabel chanLabel = new JLabel("Channel");
	    JLabel pwLabel = new JLabel("Password");
	    final JTextField channelField = new JTextField();
	    channelField.setPreferredSize(new Dimension(180,20));
	    JTextField pwField = new JTextField();
	    pwField.setPreferredSize(new Dimension(180,20));
	    combobox.setPreferredSize(new Dimension(180,20));
	    JButton cancel = new JButton("Cancel");
	    JButton ok = new JButton("Join");
	    panel.add(serverLabel);
	    panel.add(combobox);
	    panel.add(chanLabel);
	    panel.add(channelField);
	    panel.add(pwLabel);
	    panel.add(pwField);
	    panel.add(cancel);
	    panel.add(ok);


	    layout.putConstraint(SpringLayout.NORTH, serverLabel, 25, SpringLayout.NORTH, dialog);
	    layout.putConstraint(SpringLayout.WEST, serverLabel, 25, SpringLayout.WEST, dialog);
	    layout.putConstraint(SpringLayout.NORTH, combobox, 23, SpringLayout.NORTH, dialog);
	    layout.putConstraint(SpringLayout.WEST, combobox, 10, SpringLayout.EAST, serverLabel);
	    layout.putConstraint(SpringLayout.NORTH, chanLabel, 16, SpringLayout.SOUTH, combobox);
	    layout.putConstraint(SpringLayout.WEST, chanLabel, 0, SpringLayout.WEST, serverLabel);
	    layout.putConstraint(SpringLayout.NORTH, channelField, 15, SpringLayout.SOUTH, combobox);
	    layout.putConstraint(SpringLayout.WEST, channelField, 0, SpringLayout.WEST, combobox);
	    layout.putConstraint(SpringLayout.NORTH, pwLabel, 18, SpringLayout.SOUTH, chanLabel);
	    layout.putConstraint(SpringLayout.WEST, pwLabel, 0, SpringLayout.WEST, serverLabel);
	    layout.putConstraint(SpringLayout.NORTH, pwField, 13, SpringLayout.SOUTH, channelField);
	    layout.putConstraint(SpringLayout.WEST, pwField, 0, SpringLayout.WEST, combobox);
	    layout.putConstraint(SpringLayout.WEST, cancel, 122, SpringLayout.WEST, dialog);
	    layout.putConstraint(SpringLayout.SOUTH, cancel, 55, SpringLayout.SOUTH, pwLabel);
	    layout.putConstraint(SpringLayout.WEST, ok, 25, SpringLayout.EAST, cancel);
	    layout.putConstraint(SpringLayout.SOUTH, ok, 55, SpringLayout.SOUTH, pwLabel);

	    cancel.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    dialog.dispose();
		}
	    });
	    cancel.addKeyListener(new KeyAdapter() {
		 public void keyPressed(KeyEvent evt)
		 {
		     if (evt.getKeyCode() == KeyEvent.VK_ENTER) dialog.dispose();  
		 }  
	    });
	    ok.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    String choice = (String)combobox.getSelectedItem();
		    String server = channelField.getText();
		    joinOKButtonFunctionality(dialog, choice, server);
		}
	    });
	    ok.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent evt)
		{
		    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
		    {
			String choice = (String)combobox.getSelectedItem();
			String server = channelField.getText();
			joinOKButtonFunctionality(dialog, choice, server);
		    }
		}
	    });
	}
    }
    class MoveTabLeftAction extends AbstractAction{
	public MoveTabLeftAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    int index = tabbedPane.getSelectedIndex();
	    if (index <= 0) return;
	    ChannelPanel moved;
	    try{
	    moved = (ChannelPanel)tabbedPane.getComponentAt(index);
	    }catch (ClassCastException cce){
		return;
	    }
	    Color c = tabbedPane.getForegroundAt(index);
	    String label = tabbedPane.getTitleAt(index);
	    tabbedPane.add(moved,index-1);
	    tabbedPane.setTitleAt(index-1, label);
	    tabbedPane.setSelectedComponent(moved);
	    tabbedPane.setForegroundAt(index-1, c);
	    return;
	}  
    }    
    
    
    class MoveTabRightAction extends AbstractAction{
	public MoveTabRightAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    int index = tabbedPane.getSelectedIndex()+1;
	    if (index >= tabbedPane.getTabCount()) return;
	    ChannelPanel moved;
	    try{
	    moved = (ChannelPanel)tabbedPane.getComponentAt(index);
	    }catch (ClassCastException cce){
		return;
	    }
	    
	    Color c = tabbedPane.getForegroundAt(index);
	    String label = tabbedPane.getTitleAt(index);
	    tabbedPane.add(moved,index-1);
	    tabbedPane.setTitleAt(index-1, label);
	    tabbedPane.setSelectedIndex(index);
	    tabbedPane.setForegroundAt(index-1, c);
	    return;               
	}   
    }
    
    class NextTabAction extends AbstractAction{
	public NextTabAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    int indexOfChannel = tabbedPane.getSelectedIndex();
	    if (indexOfChannel < tabbedPane.getTabCount()-1) tabbedPane.setSelectedIndex(indexOfChannel+1);
	    return;
	}
    }
    
    class PreviousTabAction extends AbstractAction{
	public PreviousTabAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    int indexOfChannel = tabbedPane.getSelectedIndex();
	    if (indexOfChannel > 0) tabbedPane.setSelectedIndex(indexOfChannel-1);
	    return;
	}
    }    
    
    class QuickConnectAction extends AbstractAction{
	public QuickConnectAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{

	    String[] labels = {"Network Name","Server", "Port", "Nick", "Password"};
	    int numPairs = labels.length;
	    final JDialog dialog = new JDialog(GUI.this);
	    SpringLayout layout = new SpringLayout();
	    JPanel panel = new JPanel(layout);
	    final JTextField[] panes = new JTextField[numPairs];

	    for (int i = 0; i < numPairs; i++)
	    {
		JLabel l = new JLabel(labels[i], JLabel.TRAILING);
		panel.add(l);
		JTextField textField = new JTextField(10);
		if (i == 2) textField.setText("6667");
		if (i == 3) textField.setText(Connection.currentNick);
		panes[i] = textField;
		textField.setMaximumSize(new Dimension(10,10));
		l.setLabelFor(textField);
		panel.add(textField);
	    }
	    dialog.setTitle("Quick Connect");
	    dialog.setSize(new Dimension(250,220));
	    dialog.setResizable(false);
	    dialog.setLocationRelativeTo(tabbedPane);
	    dialog.add(panel);
	    dialog.setVisible(true);
	    JButton connectButton = new JButton("Connect");
	    connectButton.setPreferredSize(new Dimension(92,30));
	    panel.add(connectButton);
	    layout.putConstraint(SpringLayout.SOUTH, connectButton, 0, SpringLayout.SOUTH, dialog);
	    layout.putConstraint(SpringLayout.EAST, connectButton, -30, SpringLayout.EAST, dialog);
	    SpringUtilities.makeCompactGrid(panel, numPairs, 2, 6, 6, 10, 10); //rows, cols, initX, initY, xPad, yPad

	    connectButton.addKeyListener(new KeyAdapter(){
	    public void keyPressed(KeyEvent evt)
	    {
		if (evt.getKeyCode() == KeyEvent.VK_ENTER)
		{
		    quickConnectButtonFunctionality(panes, dialog);
		}
	    }
	     });

	    connectButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e)
		{
		    quickConnectButtonFunctionality(panes, dialog);
		}

	    });
	}
    }
    
    class QuitProgramAction extends AbstractAction{
	public QuitProgramAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    System.exit(0);
	}
    }
    class ReconnectAction extends AbstractAction{
	public ReconnectAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    ChannelPanel selected = (ChannelPanel)tabbedPane.getSelectedComponent();
	    if (selected.connection.isConnected)
	    {
		selected.connection.disconnect();
	    }


	    for (int j = 0; j < tabbedPane.getTabCount(); j++)
	    {
		ChannelPanel otherChannel = (ChannelPanel)tabbedPane.getComponentAt(j);
		if (selected.connection == otherChannel.connection)
		{
		    selected.connection.thread = new Thread(selected.connection);
		    selected.connection.thread.start();
		    break;
		}
	    }
	    selected.connection.autoconnect = false;

	    for (int i = 0; i < tabbedPane.getTabCount(); i++)
	    {
		ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
		String title = tabbedPane.getTitleAt(i);
		if (channel.connection == selected.connection)
		{
		    if (title.startsWith("#"))
		    {
			try {
			    selected.connection.send("JOIN "+title);
			} catch (IOException ex) {
			    //do nothing
			} catch (BadLocationException ex) {
			    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
			}
		    }
		}
	    }
	}    
    }
    class ServerListAction extends AbstractAction {
	public ServerListAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    final JDialog dialog = new JDialog(GUI.this);
	    Container contentpane = dialog.getContentPane();
	    SpringLayout layout = new SpringLayout();
	    contentpane.setLayout(layout);
	    contentpane.setPreferredSize(new Dimension(550,200));
	    final BeanTableModel model = new BeanTableModel(SavedConnection.class);
	    model.sortColumnNames();
	    for (int i = 0; i < savedConnections.size(); i++) model.addRow(savedConnections.get(i));


	    final JTable table = new JTable(model){
		public boolean isCellEditable(int row, int column){  
		    return false;  
		}  
	    };

	    table.setShowGrid(false);
	    table.getColumnModel().getColumn(0).setPreferredWidth(150);
	    table.getColumnModel().getColumn(1).setPreferredWidth(120);
	    table.getColumnModel().getColumn(2).setPreferredWidth(272);

	    JScrollPane scrollPane = new JScrollPane(table);
	    scrollPane.setPreferredSize(new Dimension(542,150));
	    scrollPane.setViewportView(table);
	    JButton add = new JButton("Add");
	    JButton edit = new JButton ("Edit");
	    JButton remove = new JButton("Remove");
	    JButton connect = new JButton("Connect");
	    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scrollPane.getViewport().setBackground(Color.white);
	    dialog.setResizable(false);
	    dialog.setTitle("Server List");
	    dialog.setLocationRelativeTo(tabbedPane);   
	    contentpane.add(scrollPane);
	    contentpane.add(add);
	    contentpane.add(edit);
	    contentpane.add(remove);
	    contentpane.add(connect);
	    layout.putConstraint(SpringLayout.WEST, scrollPane , 5, SpringLayout.WEST, contentpane); 
	    layout.putConstraint(SpringLayout.SOUTH, add, -10, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.WEST, add, 10, SpringLayout.WEST, contentpane);                
	    layout.putConstraint(SpringLayout.SOUTH, edit, -10, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.WEST, edit, 10, SpringLayout.EAST, add);                
	    layout.putConstraint(SpringLayout.SOUTH, remove, -10, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.WEST, remove, 10, SpringLayout.EAST, edit);                
	    layout.putConstraint(SpringLayout.SOUTH, connect, -10, SpringLayout.SOUTH, contentpane);
	    layout.putConstraint(SpringLayout.EAST, connect, -10 , SpringLayout.EAST, contentpane);


	    dialog.pack();
	    dialog.setVisible(true);

	    add.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e) 
		{
		    serverListAddButtonFunctionality(dialog, model);
		}
	    });

	    add.addKeyListener(new KeyAdapter()
	    {
		public void keyPressed(KeyEvent evt)
		{
		    if (evt.getKeyCode() == KeyEvent.VK_ENTER)
		    {
			serverListAddButtonFunctionality(dialog, model);
		    }
		}
	    });
	    edit.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e) 
		{
		    int rowIndex = table.getSelectedRow();
		    if (rowIndex == -1) return;
		    int modelIndex = table.convertRowIndexToModel(rowIndex);
		    SavedConnection conn = (SavedConnection)model.modelData.get(modelIndex);

		    JTextField ename = new JTextField(conn.retrieveName());
		    JTextField eserver = new JTextField(conn.retrieveServer());
		    JTextField eport = new JTextField(Integer.toString(conn.retrievePort()));
		    JTextField epassword = new JTextField(conn.retrievePassword());
		    String c = conn.retrieveChannels().toString();
		    JTextField echannels = new JTextField(c.substring(1, c.length()-1));
		    JCheckBox eautoconnect = new JCheckBox();
		    if (conn.retrieveAutoConnect() == true) eautoconnect.setSelected(true);
		    else eautoconnect.setSelected(false);
		    JCheckBox esecure = new JCheckBox();
		    if (conn.retrieveUseSSL() == true) esecure.setSelected(true);
		    else esecure.setSelected(false);

		    Object[] efields = {
			"Name", ename,
			"Server", eserver,
			"Port", eport,
			"Password", epassword,
			"Auto Join Channels (#c1 #c2)", echannels,
			"Connect on startup", eautoconnect,
			"Secure connection (SSL)", esecure
		    };
		    int ex = JOptionPane.showConfirmDialog(dialog, efields, "Edit Server", JOptionPane.OK_CANCEL_OPTION);
		    if (ex == JOptionPane.CLOSED_OPTION || ex == JOptionPane.CANCEL_OPTION) return;

		    String[] es = echannels.getText().split(" ");
		    ArrayList<String> echannelList = new ArrayList<String>(Arrays.asList(es));
		    if (!StringUtils.isNumeric(eport.getText())){
			JOptionPane.showMessageDialog(dialog, "Port must be an integer.");
			return;
		    }
		    model.removeRowRange(modelIndex, modelIndex);
		    savedConnections.remove(conn);
		    SavedConnection connection = new SavedConnection(ename.getText(), eserver.getText(), epassword.getText(), echannelList, eautoconnect.isSelected(), esecure.isSelected(), Integer.valueOf(eport.getText().trim()));
		    model.addRow(connection);
		    savedConnections.add(connection);
		    serializeSavedConnections();
		    return;
		}              
	    });

	    remove.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e) 
		{
		    int rowIndex = table.getSelectedRow();
		    if (rowIndex == -1) return;
		    int modelIndex = table.convertRowIndexToModel(rowIndex);
		    SavedConnection conn = (SavedConnection)model.modelData.get(modelIndex);
		    savedConnections.remove(conn);
		    serializeSavedConnections();
		    model.removeRowRange(modelIndex, modelIndex);
		}
	    });     

	    connect.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e) 
		{
		    int rowIndex = table.getSelectedRow();
		    if (rowIndex == -1) return;
		    SavedConnection conn = (SavedConnection)model.modelData.get(table.convertRowIndexToModel(rowIndex));

		    String network = conn.getNetwork(), server = conn.getServer();
		    int port = conn.retrievePort();
		    for (int i = 0; i < tabbedPane.getTabCount(); i++)
		    {
			ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
			if (network.equals(channel.title))
			{
			    tabbedPane.setSelectedComponent(channel);
			    String[] msg = {null, "You are already connected to "+network+"."};
			    try {
				channel.insertString(msg, ChannelPanel.errorStyle, false);
			    } catch (BadLocationException ex) {
				Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
			    } catch (IOException ex) {
				Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
			    }
			    dialog.dispose();
			    return;
			}
		    }
		    new Connection(network, server, port);
		    dialog.dispose();
		    return;
		}
	    });
	}
    }
    
    class ShowNickListAction extends AbstractAction{
	public ShowNickListAction(String text, ImageIcon icon, String desc, Integer mnemonic)
	{
	    super(text, icon);
	    putValue(SHORT_DESCRIPTION, desc);
	    putValue(MNEMONIC_KEY, mnemonic);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	    for (int i = 0; i < tabbedPane.getTabCount(); i++){
		ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
		if (channel.title.equals(channel.name))
		{
		    if (channel.getRightComponent() != null)
		    {
			channel.setRightComponent(null);
			channel.setDividerSize(0);
			channel.setDividerLocation(0);
		    }
		    else
		    { 
			channel.setRightComponent(channel.userListScrollPane);
			channel.setDividerSize(5);
			channel.setDividerLocation(540);
		    }
		}                    
	    }
	}
    }
    
}




