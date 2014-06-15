import java.io.IOException;
import java.io.PrintWriter;
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
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

    public class ChannelPanel extends JSplitPane{
           
        final String name; 
        String topic, signOnTime, topicAuthor;
        int population, ops;
        HTMLDocument doc, userList;
        Connection connection;
        final JTextField chatInputPane = new JTextField();
        final JTextPane chatPane = new JTextPane(), userListPane = new JTextPane();
        final JScrollPane jScrollPane1 = new JScrollPane(), jScrollPane2 = new JScrollPane();
        static JTabbedPane tabbedPane;
        static JLabel tabInfo;
        SortedSet<String> userSet = new TreeSet<String>(new NickComparator());
        ArrayList<String> list = new ArrayList<String>();
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        
        static String errorColor = "#FF0000", chatColor="Black", serverColor="#6600CC";
        
    
        public ChannelPanel(String name, String nick, Connection c) throws BadLocationException, IOException
        {
            this.name = name;
            this.connection = c;
                    
            doc = (HTMLDocument) htmlKit.createDefaultDocument();
            userList = (HTMLDocument)htmlKit.createDefaultDocument();
            chatPane.setContentType("text/html");
            chatPane.setDocument(doc);
            userListPane.setContentType("text/html");
            userListPane.setDocument(userList);
            makePanel();
            tabbedPane.add(this, this.name);
        }

        private void makePanel() throws BadLocationException, IOException
        {      
   
        //why is all this tabbed pane stuff in here
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
     
        
        public void insertString(String line, String target, String color) throws BadLocationException, IOException
        { 
            if (target.equals("doc"))
            {
                line = escapeHtml4(line);
                Element[] roots = doc.getRootElements(); // #0 is the HTML element, #1 the bidi-root
                Element body = null;
                for(int i = 0; i < roots[0].getElementCount(); i++) 
                {
                    Element element = roots[0].getElement(i);
                    if(element.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY)
                    {
                    body = element;
                    break;
                    }
                }
                doc.insertAfterEnd(body,"<div align='left'><font color="+color+">"+line+"</font></div>");
                return;
            }
            
            if (target.equals("userList"))
            {
                Element[] roots = userList.getRootElements(); // #0 is the HTML element, #1 the bidi-root
                Element body = null;
                for( int i = 0; i < roots[0].getElementCount(); i++ )
                {
                    Element element = roots[0].getElement( i );
                    if( element.getAttributes().getAttribute( StyleConstants.NameAttribute ) == HTML.Tag.BODY )
                    {
                        body = element;
                        break;
                    }
                }
                userList.insertAfterEnd(body,"<div align='left'><font color="+color+">"+line+"</font></div>");
                return;
            }
            else System.out.println("_____________________insertString broken_____________________");
        }
                
        public void addToUserList(String nick) throws BadLocationException, IOException
        {
            nick = nick.trim();
            userList.remove(0, userList.getLength());
            char first = nick.charAt(0);
            
            if (first == '+' || first == '@' || first == '&' || first == '%' || first == '~') userSet.add(nick);
            else userSet.add(" "+nick);
            
            population = userSet.size();
            Iterator<String> iterator = userSet.iterator();
            while (iterator.hasNext()){
                String nextElement = iterator.next();
                insertString(nextElement, "userList", "black");
            }
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return;
        }
        public boolean removeFromUserList(String nick) throws BadLocationException, IOException
        {
            String[] prefix = new String[] {" ","+","@","~","&"};
            int oldPop = this.userSet.size();
            userList.remove(0, userList.getLength());
            boolean success = false;
            
            for (int i = 0; i < prefix.length; i++)
            {
                success = this.userSet.remove(prefix[i]+nick);
                if (success == true) break;
            }
            
            population = this.userSet.size();
            Iterator<String> iterator = this.userSet.iterator();
            while (iterator.hasNext())
            {
                String nextElement = iterator.next();
                insertString(nextElement, "userList", "black");
            }
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return !(oldPop == population);
         }     
        public void removeAllFromuserList() throws BadLocationException{
            userList.remove(0, userList.getLength());
        }

    public class NickComparator implements java.util.Comparator{
        public int compare(Object o1, Object o2)
        {
            String s1 = o1.toString().substring(1).toLowerCase();
            String s2 = o2.toString().substring(1).toLowerCase();
            return s1.compareTo(s2);
        }
    }
        }
