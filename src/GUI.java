import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;
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


    
    EditorKit editorKit;
    Connection c;
    final static Properties prop = new Properties();
    static ArrayList<String> savedServers;
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
        tabbedPane = new JTabbedPane();
        jSplitPane1 = new JSplitPane();
        jScrollPane2 = new JScrollPane();
        chatPane = new JTextPane();
        jScrollPane1 = new JScrollPane();
        userListPane = new JTextPane();
        tabInfo = new JLabel();
        jMenuBar2 = new JMenuBar();
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        windowMenu = new JMenu("Window");
        settingsMenu = new JMenu("Settings");
        
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
        clearWindow = new JMenuItem("Clear Window");
        editMenu.add (clearWindow);
        clearAllWindows = new JMenuItem("Clear All Windows");
        editMenu.add(clearAllWindows);        
        previousTab = new JMenuItem("Previous Tab", prevTabIcon);
        windowMenu.add(previousTab);       
        nextTab = new JMenuItem("Next Tab", nextTabIcon);
        windowMenu.add(nextTab);
        windowMenu.add(new JSeparator()); 
        moveTabLeft = new JMenuItem("Move Tab Left", moveTabLeftIcon);
        windowMenu.add(moveTabLeft);    
        moveTabRight = new JMenuItem("Move Tab Right", moveTabRightIcon);
        windowMenu.add(moveTabRight);
        closeTab = new JMenuItem("Close Tab", closeTabIcon);
        windowMenu.add(closeTab);
        serverList = new JMenuItem("Server List", serverListIcon);
        fileMenu.add(serverList);
        quickConnect = new JMenuItem("Quick Connect", quickConnectIcon);
        fileMenu.add(quickConnect);
        fileMenu.add(new JSeparator());
        disconnect = new JMenuItem("Disconnect", disconnectIcon);
        fileMenu.add(disconnect);
        identities = new JMenuItem("Identities", identitiesIcon);
        settingsMenu.add(identities);        
        reconnect = new JMenuItem("Reconnect", reconnectIcon);
        fileMenu.add(reconnect);
        joinChannel = new JMenuItem("Join Channel", joinChannelIcon);
        fileMenu.add(joinChannel);
        fileMenu.add(new JSeparator());
        globalAway = new JMenuItem("Global Away", globalAwayIcon);
        fileMenu.add(globalAway);
        fileMenu.add(new JSeparator());
        quitProgram = new JMenuItem("Quit", quitProgramIcon);
        fileMenu.add(quitProgram);
        
        joinChannel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        clearWindow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                try {
                    channel.doc.remove(0, channel.doc.getLength());
                } catch (BadLocationException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        clearAllWindows.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);
                    try {
                       channel.doc.remove(0, channel.doc.getLength());
                    } catch (BadLocationException ex) {
                       Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                  
            }
        });
        previousTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int indexOfChannel = tabbedPane.getSelectedIndex();
                if (indexOfChannel > 0) tabbedPane.setSelectedIndex(indexOfChannel-1);
                return;
            }
        });
        nextTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int indexOfChannel = tabbedPane.getSelectedIndex();
                if (indexOfChannel < tabbedPane.getTabCount()-1) tabbedPane.setSelectedIndex(indexOfChannel+1);
                return;
            }
        });
        moveTabLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index <= 0) return;
                ChannelPanel moved = (ChannelPanel)tabbedPane.getComponentAt(index);
                String label = tabbedPane.getTitleAt(index);
                tabbedPane.add(moved,index-1);
                tabbedPane.setTitleAt(index-1, label);
                tabbedPane.setSelectedComponent(moved);
                return;
            }
        });
        moveTabRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex()+1;
                if (index >= tabbedPane.getTabCount()) return;
                ChannelPanel moved = (ChannelPanel)tabbedPane.getComponentAt(index);
                String label = tabbedPane.getTitleAt(index);
                tabbedPane.add(moved,index-1);
                tabbedPane.setTitleAt(index-1, label);
                tabbedPane.setSelectedIndex(index);
                return;               
            }
        });
        closeTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index == -1) return;
                ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                String title = tabbedPane.getTitleAt(index);            
 
                if (!title.startsWith("#") && !title.equals(channel.server)){ //closing IM
                    tabbedPane.remove(channel);
                }
                
                if (title.startsWith("#")) try { //closing channel
                    channel.connection.send("PART "+title);
                    return;
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BadLocationException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                if (title.equals(channel.server)){ //closing server
                    int warning = JOptionPane.showConfirmDialog(null, "Do you wish to disconnect from "+channel.server+"? All tabs will be closed.", "Are you sure?", JOptionPane.WARNING_MESSAGE);
                    if (warning == JOptionPane.CANCEL_OPTION || warning == JOptionPane.CLOSED_OPTION) return;
                    for (int i = 0; i < tabbedPane.getTabCount(); i++)
                    {
                        ChannelPanel c = (ChannelPanel)tabbedPane.getComponentAt(i);
                        if (c.server.equals(channel.server)){
                            tabbedPane.remove(i);
                            i--;
                        }
                    }       
                    tabbedPane.remove(channel);
                    channel.connection.disconnect();
                    return;
                }
            }
        });
        disconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index == -1) return;
                ChannelPanel channel = (ChannelPanel)tabbedPane.getSelectedComponent();
                channel.model.removeAll();
                channel.connection.disconnect();
                for (int i = 0; i < tabbedPane.getTabCount(); i++)
                {
                    ChannelPanel otherChannel = (ChannelPanel)tabbedPane.getComponentAt(i);
                    System.out.println("+"+otherChannel.server);

                    if (otherChannel.name.equals(channel.server))
                    {
                        try {
                            otherChannel.insertString("[Info] Disconnected from "+otherChannel.server+" (port "+otherChannel.connection.port+")", ChannelPanel.serverColor);
                            continue;
                        } catch (BadLocationException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (otherChannel.server.equals(channel.server))
                    {
                        tabbedPane.setForegroundAt(i, Color.gray);
                        otherChannel.model.removeAll();
                    }

                }
                channel.updateTabInfo();
                return;
            }
        });
        reconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChannelPanel selected = (ChannelPanel)tabbedPane.getSelectedComponent();
                
                for (int j = 0; j < tabbedPane.getTabCount(); j++)
                {
                    String title = tabbedPane.getTitleAt(j);
                    if (title.equals(selected.server)){
                        System.out.println("uuuuuguuuuuu");
                        selected.connection.thread = new Thread(selected.connection);
                        selected.connection.thread.start();
                        break;
                    }
                }
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
        });
        
        //QUICK CONNECT
        quickConnect.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                
                String[] labels = {"Server", "Port", "Nick", "Password"};
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
                    if (i == 1) textField.setText("6667");
                    if (i == 2) textField.setText(Connection.currentNick);
                    panes[i] = textField;
                    textField.setMaximumSize(new Dimension(10,10));
                    l.setLabelFor(textField);
                    panel.add(textField);
                }
                dialog.setTitle("Quick Connect");
                dialog.setSize(new Dimension(200,180));
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
        });
        //IDENTITIES
        identities.addActionListener(new ActionListener(){
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
        });
        //SERVER LIST
        serverList.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                
                final JDialog dialog = new JDialog(GUI.this);
                Container contentpane = dialog.getContentPane();
                SpringLayout layout = new SpringLayout();
                contentpane.setLayout(layout);
                contentpane.setPreferredSize(new Dimension(388,200));
                final DefaultListModel model = new DefaultListModel();
                final JList list = new JList(model);
                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setPreferredSize(new Dimension(380,150));
                scrollPane.setViewportView(list);
                JButton add = new JButton("Add");
                JButton edit = new JButton ("Edit");
                JButton remove = new JButton("Remove");
                JButton connect = new JButton("Connect");
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
                
                for (int i = 0; i < savedServers.size(); i++) model.addElement(savedServers.get(i));

                dialog.pack();
                dialog.setVisible(true);
                
                add.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e) 
                    {
                        serverListAddButtonFunctionality(dialog, list);
                    }
                });
                
                add.addKeyListener(new KeyAdapter()
                {
                    public void keyPressed(KeyEvent evt)
                    {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                        {
                            serverListAddButtonFunctionality(dialog, list);
                        }
                    }
                });
                edit.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e) 
                    {
                        int index = list.getSelectedIndex();
                        DefaultListModel model = (DefaultListModel)list.getModel();
                        if (index == -1){
                            JOptionPane.showMessageDialog(dialog,"Please select a server to edit.","Error",JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        String settings = (String)model.getElementAt(index);
                        String[] s = settings.split(",");
                        String chans = "";
                        for (String str: s) if (str.startsWith("#"))chans += " "+str;
                        
                        JTextField server = new JTextField(s[1]);
                        JTextField port = new JTextField(s[2]);
                        JTextField channels = new JTextField(chans.trim());
                        JCheckBox autoconnect = new JCheckBox();
                        if ((s[s.length-2]).equals("true")) autoconnect.setSelected(true);
                        else autoconnect.setSelected(false);
                        JCheckBox secure = new JCheckBox(s[s.length-1]);
                        if ((s[s.length-1]).equals("true")) secure.setSelected(true);
                        else secure.setSelected(false);        

                        Object[] fields = {
                            "Server", server,
                            "Port", port,
                            "Auto-join channels (#c1 #c2)", channels,
                            "Connect on startup", autoconnect,
                            "Secure connection (SSL)", secure
                        };
                        int x = JOptionPane.showConfirmDialog(dialog, fields, "Edit Server",JOptionPane.OK_CANCEL_OPTION);
                        if (x == JOptionPane.CLOSED_OPTION || x == JOptionPane.CANCEL_OPTION) return;

                            String entry = s[0]+ ","+server.getText().trim() + "," + port.getText().trim() + ","+ 
                                           channels.getText().trim() + "," + autoconnect.isSelected() + "," + secure.isSelected();

                            try {
                                prop.load(new FileInputStream("config.properties"));
                                prop.setProperty("ss"+s[0], entry);
                                prop.store(new FileOutputStream("config.properties"), null);
                                model.remove(index);
                                model.addElement(entry);
                                } catch (IOException io) {
                                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, io);
                                } finally {
                                    if (output != null) {
                                        try {
                                            output.close();
                                        } catch (IOException f) {
                                            f.printStackTrace();
                                        }
                                    }
                                }
                            return;
                    }                    
                });
                
                remove.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e) 
                    {
                        int index = list.getSelectedIndex();
                        if (index == -1){
                            JOptionPane.showMessageDialog(dialog,"Please select a server to remove.","Error",JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        String entry = (String)model.getElementAt(index);
                        String[] s = entry.split(",");
                        int choice = Integer.valueOf(s[0]);
                        try{
                        prop.load(new FileInputStream("config.properties"));
                        prop.remove("ss"+choice);
                        prop.store(new FileOutputStream("config.properties"), null);
                        savedServers.remove(index);
                        model.removeElement(entry);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        
                    }
                });
                
                connect.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e) 
                    {
                        serverListConnectButtonFunctionality(list, dialog);
                    }
                });
                
                connect.addKeyListener(new KeyAdapter()
                {
                    public void keyPressed(KeyEvent evt)
                    {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                        {
                            serverListConnectButtonFunctionality(list, dialog);
                        }
                    }
                });
            }
        });
        chatInputPane.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                chatInputPaneKeyPressed(evt);
            }
        });
        
        
        
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

        chatPane.setEditable(false);
        jScrollPane2.setViewportView(chatPane);
        DefaultCaret caret = (DefaultCaret)chatPane.getCaret(); //caret necessary?
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

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
        savedServers = new ArrayList<String>();
	InputStream input = null;
 
	try {
		input = new FileInputStream("config.properties");
		prop.load(input);
 
                Connection.real = prop.getProperty("Real");
                Connection.nicks[0] = prop.getProperty("Nick");
                Connection.currentNick = Connection.nicks[0];
                Connection.nicks[1] = prop.getProperty("Second");
                Connection.nicks[2] = prop.getProperty("Third");
                boolean isMore = true;
                int i = 0;
                while (isMore == true)
                {
                    String srv = prop.getProperty("ss"+i, "nosrv");
                    if (srv.equals("nosrv"))
                    {
                        isMore = false;
                        break;
                    } 
                    savedServers.add(srv);
                    i++;
                }
 
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
        for (int i = 0; i < savedServers.size(); i++)
        {
            String[] s = savedServers.get(i).split(",");
            if (s[s.length-2].equals("true"))
            {
                String srv = s[1];
                int port = Integer.valueOf(s[2]);
                new Connection(srv, port, true);
            }
        }

    }
    private void quickConnectButtonFunctionality(JTextField[] panes, JDialog dialog)
    {
        String chan = panes[0].getText().trim();
        String p = panes[1].getText().trim();
        String n = panes[2].getText().trim();
        String pass = panes[3].getText().trim();

        if (chan.isEmpty() || p.isEmpty() || n.isEmpty())
        {
            JOptionPane.showMessageDialog(dialog, "Server, port and nick are required");
        }
        else
        {
            dialog.dispose();
            c.nicks[0] = n;
            new Connection(chan, Integer.valueOf(p), false);
            if (!pass.isEmpty()) c.password = pass;
            return;
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
        Component aComponent = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
        ChannelPanel channel = (ChannelPanel)aComponent;
        
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
                channel.insertString(("<" + Connection.currentNick + ">:".trim() +" "+ msg.trim()),ChannelPanel.chatColor);   
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
                    channel.insertString("[Error] *** You are not connected to the server.",ChannelPanel.errorColor);
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
    
    private void serverListAddButtonFunctionality(JDialog dialog, JList list)
    {
        JTextField server = new JTextField();
        JTextField port = new JTextField();
        JTextField channels = new JTextField();
        JCheckBox autoconnect = new JCheckBox();
        JCheckBox secure = new JCheckBox();

        Object[] fields = {
            "Server", server,
            "Port", port,
            "Auto-join channels (#c1 #c2)", channels,
            "Connect on startup", autoconnect,
            "Secure connection (SSL)", secure
        };
        int savedServerCount = list.getModel().getSize();
        int x = JOptionPane.showConfirmDialog(dialog, fields, "Add Server", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.CLOSED_OPTION || x == JOptionPane.CANCEL_OPTION) return;
        String entry = savedServerCount+","+server.getText().trim() + "," + port.getText().trim() + ","+ 
                       channels.getText().trim() + "," + autoconnect.isSelected() + "," + secure.isSelected();
        server.requestFocusInWindow();
        try {
            prop.load(new FileInputStream("config.properties"));
            prop.setProperty("ss"+savedServerCount, entry);
            prop.store(new FileOutputStream("config.properties"), null);
            DefaultListModel model = (DefaultListModel)list.getModel();
            model.addElement(entry);
            savedServers.add(entry);
            } catch (IOException io) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, io);
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException f) {
                        f.printStackTrace();
                    }
                }
            }
        return;
    }
    private void serverListConnectButtonFunctionality(JList list, JDialog dialog)
    {
        int index = list.getSelectedIndex();
        if (index == -1)
        {
            JOptionPane.showMessageDialog(dialog, "Please select a server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try{
        prop.load(new FileInputStream("config.properties"));
        String selection = prop.getProperty("ss"+index);
        prop.store(new FileOutputStream("config.properties"), null);
        String[] s = selection.split(",");
        if (((!StringUtils.isNumeric(s[2])) || s[2].equals("") || s[1].equals("") || s.length < 5))
        {
            JOptionPane.showMessageDialog(dialog, "Invalid server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String srv = s[1];
        int port = Integer.valueOf(s[2]);
        dialog.dispose();
        new Connection(srv, port);
        return;


        } catch (FileNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
        
    }

    private JTextField chatInputPane;
    private JTextPane chatPane;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu settingsMenu;
    private JMenu windowMenu;
    private JMenuBar jMenuBar2;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JSplitPane jSplitPane1;
    private static JLabel tabInfo;
    private static JTabbedPane tabbedPane;
    private JTextPane userListPane;
    
    private JMenuItem copyAction;
    private JMenuItem cutAction;
    private JMenuItem pasteAction;
    private JMenuItem quickConnect;
    private JMenuItem identities;
    private JMenuItem serverList;
    private JMenuItem clearWindow;
    private JMenuItem clearAllWindows;
    private JMenuItem previousTab;
    private JMenuItem nextTab;
    private JMenuItem moveTabLeft;
    private JMenuItem moveTabRight;
    private JMenuItem closeTab;
    private JMenuItem disconnect;
    private JMenuItem reconnect;
    private JMenuItem globalAway;
    private JMenuItem joinChannel;
    private JMenuItem quitProgram;
}