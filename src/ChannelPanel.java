



/*TODO:  continue with chatInputPaneKeyPressed (account for error messages, commands, etc)
close connections on /close 
*/


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;

       public class ChannelPanel extends JSplitPane implements ActionListener{
           
        final String name; 
        String nick;
        String topic, time, topicAuthor;
        int population, ops;
        DefaultStyledDocument userList = new DefaultStyledDocument(), doc = new DefaultStyledDocument();
        Connection connection;
        final JTextField chatInputPane = new JTextField();
        final JTextPane chatPane = new JTextPane(doc), userListPane = new JTextPane(userList);
        final JScrollPane jScrollPane1 = new JScrollPane(), jScrollPane2 = new JScrollPane();
        static JTabbedPane tabbedPane;
        static JLabel tabInfo;
        SortedSet<String> userSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        ArrayList<String> list = new ArrayList<String>();
    
        public ChannelPanel(String name, String nick, Connection c)
        {
            this.name = name;
            this.nick = nick;
            this.connection = c;
            makePanel();
            tabbedPane.add(this, this.name);
        }
        public void setThings(javax.swing.JTabbedPane tp, javax.swing.JLabel ti){
            this.tabbedPane = tp;
            this.tabInfo = ti;
        }
        private void makePanel()
        {
        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        tabbedPane.setToolTipText("");
        tabbedPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tabbedPane.setFocusable(false);
        tabbedPane.setPreferredSize(new java.awt.Dimension(600, 450));

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setDividerLocation(480);
        setResizeWeight(1.0);
        setVerifyInputWhenFocusTarget(false);

        
        chatPane.setEditable(false);
        jScrollPane1.setViewportView(userListPane);
        jScrollPane2.setViewportView(chatPane);
        DefaultCaret caret = (DefaultCaret)chatPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        setLeftComponent(jScrollPane2);
        setRightComponent(jScrollPane1);
        
        userListPane.setEditable(false);
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new java.awt.Dimension(25, 25));
        
        ChangeListener changeListener = new ChangeListener(){
            public void stateChanged(ChangeEvent changeEvent){
                javax.swing.JTabbedPane tabbedPane = (javax.swing.JTabbedPane)changeEvent.getSource();
                int index = tabbedPane.getSelectedIndex();
                ChannelPanel c = (ChannelPanel)tabbedPane.getComponentAt(index);
                tabInfo.setText(Integer.toString(c.population)+" nicks     ");
            }          
        };
        tabbedPane.addChangeListener(changeListener);   
        
        }
        public void insertString(String line, String target) throws BadLocationException
        { 
            if (target.equals("doc")){
                doc.insertString(doc.getLength(), line+"\n", null);
                return;
            }
            if (target.equals("userList")){
                userList.insertString(userList.getLength(), line+"\n", null);
                return;
            }
            else System.out.println("_____________________insertString broken_____________________");
        }
        public void addToUserList(String nick) throws BadLocationException
        {
            userList.remove(0, userList.getLength());
            userSet.add(nick);
            population = userSet.size();
            Iterator<String> iterator = userSet.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, "userList");
            }
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return;
        }
        public void removeFromUserList(String nick) throws BadLocationException
        {
            userList.remove(0, userList.getLength());
            userSet.remove(nick);
            population = userSet.size();
            Iterator<String> iterator = userSet.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, "userList");
            }
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return;
         }     
        public void removeAllFromuserList() throws BadLocationException{
            userList.remove(0, userList.getLength());
        }
        public void actionPerformed(ActionEvent e)
        {
            //unimplemented
            
        }
        
    }