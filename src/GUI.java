import java.io.IOException;
import javax.swing.text.EditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DefaultCaret;

public class GUI extends javax.swing.JFrame {
    EditorKit editorKit;
    DefaultStyledDocument doc, userList;
    Connection c;

    public GUI() {
        doc = new DefaultStyledDocument();
        userList = new DefaultStyledDocument();
        initComponents();
        tabbedPane.removeAll();
        c = new Connection("irc.rizon.net",6667, doc, userList, tabbedPane, tabInfo);
        //tabbedPane.addTab("Main", );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();

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

        jMenuBar2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jMenuBar2.setFocusable(false);

        jMenu3.setText("File");
        jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");
        jMenuBar2.add(jMenu4);

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
    }// </editor-fold>//GEN-END:initComponents

    private void chatInputPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chatInputPaneKeyPressed
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
    }//GEN-LAST:event_chatInputPaneKeyPressed

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField chatInputPane;
    private javax.swing.JTextPane chatPane;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel tabInfo;
    public javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextPane userListPane;
    // End of variables declaration//GEN-END:variables
}