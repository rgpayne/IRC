import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.logging.*;
import java.io.*;

public class GUI extends javax.swing.JFrame {
    final static ImageIcon mainIcon = new ImageIcon("src/icons/weather-sun.png");
    final static ImageIcon quickConnectIcon = new ImageIcon("src/icons/connect.png");
    final static ImageIcon identitiesIcon = new ImageIcon("src/icons/edit-group.png");
    final static ImageIcon serverListIcon = new ImageIcon("src/icons/unsortedlist1.png");

    
    EditorKit editorKit;
    Connection c;
    final static Properties prop = new Properties();
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
        
       new Connection("irc.rizon.net", 6667);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        
        
        chatInputPane = new javax.swing.JTextField();
        tabbedPane = new javax.swing.JTabbedPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        chatPane = new javax.swing.JTextPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        userListPane = new javax.swing.JTextPane();
        tabInfo = new javax.swing.JLabel();
        jMenuBar2 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        editMenu = new javax.swing.JMenu();
        settingsMenu = new javax.swing.JMenu();
        
        copyAction = new javax.swing.JMenuItem(new javax.swing.text.DefaultEditorKit.CopyAction());
        copyAction.setText("Copy");
        editMenu.add(copyAction);
        cutAction = new javax.swing.JMenuItem((new javax.swing.text.DefaultEditorKit.CutAction()));
        cutAction.setText("Cut");
        editMenu.add(cutAction);
        pasteAction = new javax.swing.JMenuItem(new javax.swing.text.DefaultEditorKit.PasteAction());
        pasteAction.setText("Paste");       
        editMenu.add(pasteAction);
        
        quickConnect = new javax.swing.JMenuItem("Quick Connect", quickConnectIcon);
        
        fileMenu.add(quickConnect);
        quickConnect.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e)
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
                    textField.setMaximumSize(new java.awt.Dimension(10,10));
                    l.setLabelFor(textField);
                    panel.add(textField);
                }
                dialog.setTitle("Quick Connect");
                dialog.setSize(new java.awt.Dimension(200,180));
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(tabbedPane);
                dialog.add(panel);
                dialog.setVisible(true);
                java.awt.Button connectButton = new java.awt.Button("Connect");
                connectButton.setPreferredSize(new java.awt.Dimension(80,30));
                panel.add(connectButton);
                layout.putConstraint(SpringLayout.SOUTH, connectButton, 0, SpringLayout.SOUTH, dialog);
                layout.putConstraint(SpringLayout.EAST, connectButton, -30, SpringLayout.EAST, dialog);
                SpringUtilities.makeCompactGrid(panel, numPairs, 2, 6, 6, 10, 10); //rows, cols, initX, initY, xPad, yPad

                connectButton.addKeyListener(new java.awt.event.KeyAdapter(){
                public void keyPressed(java.awt.event.KeyEvent evt)
                {
                    if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
                    {
                        quickConnectButtonFunctionality(panes, dialog);
                    }
                }
                 });
                                
                connectButton.addActionListener(new java.awt.event.ActionListener(){
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                        quickConnectButtonFunctionality(panes, dialog);
                    }
                    
                });
            }
        });
        
        //IDENTITIES
        identities = new javax.swing.JMenuItem("Identities", identitiesIcon);
        settingsMenu.add(identities);
        identities.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e)
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
                    textField.setMaximumSize(new java.awt.Dimension(10,10));
                    l.setLabelFor(textField);
                    panel.add(textField);
                }

                dialog.setTitle("Identities");
                dialog.setSize(new java.awt.Dimension(220,180));
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(tabbedPane);                              
                dialog.add(panel);
                dialog.setVisible(true);
                Button saveButton = new Button("Save");
                saveButton.setPreferredSize(new java.awt.Dimension(80,30));
                panel.add(saveButton);
                layout.putConstraint(SpringLayout.SOUTH, saveButton, 0, SpringLayout.SOUTH, dialog);
                layout.putConstraint(SpringLayout.EAST, saveButton, -35, SpringLayout.EAST, dialog);
                SpringUtilities.makeCompactGrid(panel, numPairs, 2, 6, 6, 10, 10); //rows, cols, initX, initY, xPad, yPad
                
                //JLabel warning = new JLabel("Changes take place on next restart");
                //panel.add(warning);
                
                saveButton.addKeyListener(new java.awt.event.KeyAdapter(){
                public void keyPressed(java.awt.event.KeyEvent evt)
                {
                    if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
                    {
                        identityButtonFunctionality(panes, dialog);
                    }
                }
                 });
                
                saveButton.addActionListener(new java.awt.event.ActionListener(){
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                        identityButtonFunctionality(panes,dialog);
                    }
                    
                });
            }
        });
        
        //SERVER LIST
        serverList = new javax.swing.JMenuItem("Server List", serverListIcon);
        settingsMenu.add(serverList);
        serverList.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                
                final JDialog dialog = new JDialog(GUI.this);
                Container contentpane = dialog.getContentPane();
                SpringLayout layout = new SpringLayout();
                contentpane.setLayout(layout);
                contentpane.setPreferredSize(new Dimension(388,200));
                final JList list = new JList();
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
                
                dialog.setVisible(true);
                dialog.pack();
                
            }
        });
                

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        chatInputPane.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                chatInputPaneKeyPressed(evt);
            }
        });

        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        tabbedPane.setToolTipText("");
        tabbedPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tabbedPane.setFocusable(false);
        tabbedPane.setPreferredSize(new java.awt.Dimension(600, 450));

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jSplitPane1.setDividerLocation(480);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setVerifyInputWhenFocusTarget(false);

        chatPane.setEditable(false);
        jScrollPane2.setViewportView(chatPane);
        DefaultCaret caret = (DefaultCaret)chatPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        jSplitPane1.setLeftComponent(jScrollPane2);

        userListPane.setEditable(false);
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new java.awt.Dimension(25, 25));
        jScrollPane1.setViewportView(userListPane);

        jSplitPane1.setRightComponent(jScrollPane1);

        tabInfo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jMenuBar2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jMenuBar2.setFocusable(false);

        fileMenu.setText("File");
        jMenuBar2.add(fileMenu);

        editMenu.setText("Edit");
        jMenuBar2.add(editMenu);
        
        settingsMenu.setText("Settings");
        jMenuBar2.add(settingsMenu);

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
    private void quickConnectButtonFunctionality(JTextField[] panes, JDialog dialog)
    {
        String chan = panes[0].getText().trim();
        String p = panes[1].getText().trim();
        String n = panes[2].getText().trim();
        String pass = panes[3].getText().trim();

        if (chan.isEmpty() || p.isEmpty() || n.isEmpty())
        {
            javax.swing.JOptionPane.showMessageDialog(dialog, "Server, port and nick are required");
        }
        else
        {
        dialog.dispose();
        c = new Connection(chan, Integer.valueOf(p));
        c.nicks[0] = n;
        c.password = pass;
        return;
        }
    }
    private void identityButtonFunctionality(JTextField[] panes, JDialog dialog)
    {
        try {
            output = new FileOutputStream("config.properties");
            prop.setProperty("Real", panes[0].getText().trim());
            prop.setProperty("Nick", panes[1].getText().trim());
            prop.setProperty("Second", panes[2].getText().trim() );
            prop.setProperty("Third", panes[3].getText().trim());
            prop.store(output, null);

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
    private void chatInputPaneKeyPressed(java.awt.event.KeyEvent evt)
    {
        Component aComponent = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
        ChannelPanel channel = (ChannelPanel)aComponent;
        
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
        {
            try
            {
            
            String msg = chatInputPane.getText();
            String target = channel.name;
            channel.history.add(msg);
            String output = "PRIVMSG "+target+" :"+msg;
            channel.historyCounter = channel.history.size()-1;
            
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
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP)
        {
            String msg = "";
            if (channel.historyCounter >= 0){
                msg = channel.history.get(channel.historyCounter);
                if (channel.historyCounter > 0) channel.historyCounter--;
            }            
            chatInputPane.setText(msg);
            return;
        }
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN)
        {
            String msg = "";
            if (channel.historyCounter < channel.history.size()-1){
                msg = channel.history.get(channel.historyCounter);
                channel.historyCounter++;
            }
            chatInputPane.setText(msg);
            return;
        }
    }
    
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
        
    }

    private javax.swing.JTextField chatInputPane;
    private javax.swing.JTextPane chatPane;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private static javax.swing.JLabel tabInfo;
    private static javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextPane userListPane;
    
    private javax.swing.JMenuItem copyAction;
    private javax.swing.JMenuItem cutAction;
    private javax.swing.JMenuItem pasteAction;
    private javax.swing.JMenuItem quickConnect;
    private javax.swing.JMenuItem identities;
    private javax.swing.JMenuItem serverList;
}