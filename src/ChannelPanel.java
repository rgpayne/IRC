import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

    public class ChannelPanel extends JSplitPane{
           
        final String name; 
        String topic="", signOnTime, topicAuthor;
        int population, ops;
        HTMLDocument doc;
        Connection connection;
        final JTextField chatInputPane = new JTextField();
        
        final JTextPane chatPane = new JTextPane();
        final JList userListPane;
        
        
        final JScrollPane jScrollPane1 = new JScrollPane(), jScrollPane2 = new JScrollPane();
        static JTabbedPane tabbedPane;
        static JLabel tabInfo;
        
        SortedListModel<String> model;
        SortedSet<String> userSet = new TreeSet<String>(new NickComparator());
        ArrayList<String> list = new ArrayList<String>();
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        
        static String errorColor = "#FF0000", chatColor="Black", serverColor="#6600CC";
        
        
        

    
        public ChannelPanel(String name, String nick, Connection c) throws BadLocationException, IOException
        {
            this.name = name;
            this.connection = c;
                    
            doc = (HTMLDocument)htmlKit.createDefaultDocument();
            chatPane.setContentType("text/html");
            chatPane.setDocument(doc);
            chatPane.setText(topic);
            model = new SortedListModel();
            userListPane = new JList(model);
            userListPane.setModel(model);
            userListPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            userListPane.setLayoutOrientation(JList.VERTICAL);
            
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
     
        
        public void insertString(String line, String color) throws BadLocationException, IOException
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
                
        public void addToUserList(String nick) throws BadLocationException, IOException
        {
            nick = nick.trim();
            char first = nick.charAt(0);
            SortedListModel<String> model = (SortedListModel<String>) this.userListPane.getModel();

            if (first == '+' || first == '@' || first == '&' || first == '%' || first == '~')
            {
                model.addElement(first+nick.substring(1));
            }
            else model.addElement(" "+nick);
            
            population = model.getSize();
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return;
        }
        public void addManyToUserList(String nick)
        {
            SortedListModel<String> model = (SortedListModel<String>) this.userListPane.getModel();
            model.addManyElements(nick);
        }
        public void fireIntervalAdded()
        {
            SortedListModel<String> model = (SortedListModel<String>) this.userListPane.getModel();            
            model.fireIntervalAdded();
        }
        public boolean removeFromUserList(String nick) throws BadLocationException, IOException
        {
            String[] prefix = new String[] {" ","+","@","~","&"};
            SortedListModel<String>  model = (SortedListModel<String>) this.userListPane.getModel();
            int oldPop = model.getSize();
            boolean success = false;
            
            for (int i = 0; i < prefix.length; i++)
            {
                success = model.removeElement(prefix[i]+nick);
                if (success == true) break;
            }
            
            population = this.model.getSize();
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return !(oldPop == population);
         }     
        
        public void clear()
        {
            SortedListModel<String>  model = (SortedListModel<String>) userListPane.getModel();
            model.removeAll();
            return;
        }
        public class SortedListModel<String> extends AbstractListModel
        {
            SortedSet<String> set;
            NickComparator comparator;
            public SortedListModel()
            {
                comparator = new NickComparator();
                set = new TreeSet<String>(comparator);
            }
            @Override
            public int getSize()
            {
                return set.size();
            }

            @Override
            public String getElementAt(int index)
            {
                return (String) set.toArray()[index];
            }
            public boolean addElement(String x)
            {
                boolean success = set.add(x);
                fireIntervalAdded(this, 0, set.size()-1);
                return success;
            }
            public void addManyElements(String x)
            {
                set.add(x);
                return;
            }
            public void fireIntervalAdded()
            {
                fireIntervalAdded(this, 0, set.size()-1);
            }
            public boolean removeElement(String x)
            {
                boolean success = set.remove(x);
                fireIntervalRemoved(this, 0, set.size()-1);
                return success;
            }
            public void removeAll()
            {
                set.clear();
            }
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
