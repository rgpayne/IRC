import java.io.IOException;
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
        tabbedPane.removeAll();
        c = new Connection("irc.rizon.net", "Rizon", 6667, doc, userList, tabbedPane, tabInfo);
       
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
        cutAction = new javax.swing.JMenuItem((new javax.swing.text.DefaultEditorKit.CutAction()));
        cutAction.setText("Cut");
        pasteAction = new javax.swing.JMenuItem(new javax.swing.text.DefaultEditorKit.PasteAction());
        pasteAction.setText("Paste");
        
        
        editMenu.add(copyAction);
        editMenu.add(cutAction);
        editMenu.add(pasteAction);
        
        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        chatInputPane.addKeyListener(new java.awt.event.KeyAdapter() {
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

        tabbedPane.addTab("tab1", jSplitPane1);

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
            c.writer.write(chatInputPane.getText()+"\r\n");
            c.writer.flush();
            chatInputPane.setText(null);
            evt.consume();
            } catch(IOException e){
                System.out.println("IOException chatInputPane");
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
    private javax.swing.JLabel tabInfo;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextPane userListPane;
    
    private javax.swing.JMenuItem copyAction;
    private javax.swing.JMenuItem cutAction;
    private javax.swing.JMenuItem pasteAction;
}