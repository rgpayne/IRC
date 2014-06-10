import java.awt.Component;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DefaultCaret;

public class GUI1 extends javax.swing.JFrame {
    EditorKit editorKit;
    DefaultStyledDocument doc, userList;
    Connection c;

    public GUI1(){
        doc = new DefaultStyledDocument();
        userList = new DefaultStyledDocument();
        initComponents();
        ChannelPanel.tabbedPane = tabbedPane;
        ChannelPanel.tabInfo = tabInfo;
        
        c = new Connection("irc.rizon.net", 6667, doc, userList, tabbedPane, tabInfo);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        chatInputPane = new javax.swing.JTextField();
        tabbedPane = new javax.swing.JTabbedPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        chatPane = new javax.swing.JTextPane(doc);
        jScrollPane1 = new javax.swing.JScrollPane();
        userListPane = new javax.swing.JTextPane(userList);
        tabInfo = new javax.swing.JLabel();
        jMenuBar2 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        editMenu = new javax.swing.JMenu();
        
        copyAction = new javax.swing.JMenuItem(new javax.swing.text.DefaultEditorKit.CopyAction());
        copyAction.setText("Copy");
        editMenu.add(copyAction);
        cutAction = new javax.swing.JMenuItem((new javax.swing.text.DefaultEditorKit.CutAction()));
        cutAction.setText("Cut");
        editMenu.add(cutAction);
        pasteAction = new javax.swing.JMenuItem(new javax.swing.text.DefaultEditorKit.PasteAction());
        pasteAction.setText("Paste");       
        editMenu.add(pasteAction);
        
        quickConnect = new javax.swing.JMenuItem("Quick Connect");
        fileMenu.add(quickConnect);
        quickConnect.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                final javax.swing.JPanel panel = new javax.swing.JPanel();
                final javax.swing.JDialog window = new javax.swing.JDialog();
                //javax.swing.BoxLayout bl = new javax.swing.BoxLayout(window, javax.swing.BoxLayout.Y_AXIS);
                //window.setLayout(new javax.swing.BoxLayout(window, javax.swing.BoxLayout.Y_AXIS));
                //window.setLayout(new javax.swing.BoxLayout(window, javax.swing.BoxLayout.Y_AXIS));
                window.setTitle("Quick Connect");
                window.setSize(new java.awt.Dimension(200,235));
                window.setResizable(false);
                window.setVisible(true);
                window.setLocationRelativeTo(tabbedPane);
                //window.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                //window.setModal(true);

                
                javax.swing.JLabel channelNameLabel = new javax.swing.JLabel("Server");
                final javax.swing.JTextField channelName = new javax.swing.JTextField(15);
                channelName.setMaximumSize(channelName.getPreferredSize());
                javax.swing.JLabel portLabel = new javax.swing.JLabel("Port");
                final javax.swing.JTextField port = new javax.swing.JTextField(15);
                port.setMaximumSize(port.getPreferredSize());
                javax.swing.JLabel nickLabel = new javax.swing.JLabel("Nick");
                final javax.swing.JTextField nick = new javax.swing.JTextField(15);
                nick.setMaximumSize(nick.getPreferredSize());
                javax.swing.JLabel passwordLabel = new javax.swing.JLabel("Password");
                final javax.swing.JTextField password = new javax.swing.JTextField(15);
                password.setMaximumSize(password.getPreferredSize());
                java.awt.Button connectButton = new java.awt.Button("Connect");
                connectButton.setSize(new java.awt.Dimension(85,30));
                connectButton.setMaximumSize(connectButton.getPreferredSize());
                connectButton.addKeyListener(new java.awt.event.KeyAdapter(){
                public void keyPressed(java.awt.event.KeyEvent evt)
                {
                    if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER){
                        String chan = channelName.getText().trim();
                        String p = port.getText().trim();
                        String n = nick.getText().trim();
                        String pass = password.getText().trim();
                        
                        if (chan.isEmpty() || p.isEmpty() || n.isEmpty())
                        {
                            javax.swing.JOptionPane.showMessageDialog(window, "Server, port and nick are required");
                        }
                        else
                        {
                        window.dispose();
                        c = new Connection(chan, Integer.valueOf(p), doc, userList, tabbedPane, tabInfo);
                        c.nick = n;
                        c.password = pass;
                        }
                    }
                }
                 });
                
                window.add(panel);
                panel.add(channelNameLabel);
                panel.add(channelName);
                panel.add(portLabel);
                panel.add(port);
                panel.add(nickLabel);
                panel.add(nick);
                panel.add(passwordLabel);
                panel.add(password);
                panel.add(connectButton);                
                
                panel.setVisible(true);
                //panel.setModal(true);
                
                connectButton.addActionListener(new java.awt.event.ActionListener(){
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                        String chan = channelName.getText().trim();
                        String p = port.getText().trim();
                        String n = nick.getText().trim();
                        String pass = password.getText().trim();
                        
                        if (chan.isEmpty() || p.isEmpty() || n.isEmpty())
                        {
                            javax.swing.JOptionPane.showMessageDialog(window, "Server, port and nick are required");
                        }
                        else{
                        window.dispose();
                        c = new Connection(chan, Integer.valueOf(p), doc, userList, tabbedPane, tabInfo);
                        c.nick = n;
                        c.password = pass;
                        }
                    }
                    
                });
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

        tabbedPane.getAccessibleContext().setAccessibleName("Main");

        pack();
    }

    private void chatInputPaneKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER){
            try{
            
            String msg = chatInputPane.getText();
            Component aComponent = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            ChannelPanel channel = (ChannelPanel)aComponent;
            String target = channel.name;            
            String output = "PRIVMSG "+target+" :"+msg;
            
            if (msg.charAt(0) != '/'){            
            channel.connection.writer.write(output+"\r\n");
            channel.insertString((Connection.formatNickname("<" + channel.nick + ">:").trim() +" "+ msg.trim()), "doc");   
            c.writer.flush();
            chatInputPane.setText(null);
            evt.consume();
            return;
            }
            else
            {
                channel.connection.writer.write(msg.substring(1)+"\r\n");
                channel.connection.writer.flush();
                chatInputPane.setText(null);
                evt.consume();
            }
                       
            } catch(IOException e){
                System.out.println("IOException chatInputPane");
            } catch (BadLocationException ex) {
                Logger.getLogger(GUI1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI1().setVisible(true);
            }
        });
        
    }

    private javax.swing.JTextField chatInputPane;
    private javax.swing.JTextPane chatPane;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu editMenu;
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
}