import java.awt.Component;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
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
        String topic="", signOnTime, topicAuthor, server;
        int population, ops = 0;
        
        Connection connection;
        
        final JTextField chatInputPane = new JTextField();       
        final JTextPane chatPane = new JTextPane();
        final JList<User> userListPane;
        
        
        final JScrollPane jScrollPane1 = new JScrollPane(), jScrollPane2 = new JScrollPane();
        static JTabbedPane tabbedPane;
        static JLabel tabInfo;
        
        SortedListModel<User> model = new SortedListModel<User>();
        ArrayList<String> list = new ArrayList<String>();
        
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument doc;    
        
        static String errorColor = "#FF0000", chatColor="Black", serverColor="#990099", connectColor="#993300", timestampColor="#909090";
        static String font = "Sans Serif";
        boolean showTimestamp = true;
        
        ArrayList<String> history;
        int historyCounter = 0;

           
        public ChannelPanel(String name, String nick, Connection c) throws BadLocationException, IOException
        {
            this.name = name;
            this.connection = c;
                    
            doc = (HTMLDocument)htmlKit.createDefaultDocument();
            chatPane.setContentType("text/html");
            chatPane.setDocument(doc);
            chatPane.setText(topic);
            userListPane = new JList(model);
            if (showTimestamp == true) history = new ArrayList<String>();
            makePanel();
                       
            tabbedPane.add(this, this.name);
        }

        private void makePanel() throws BadLocationException, IOException
        {      
            
        userListPane.setModel(model);
        userListPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userListPane.setLayoutOrientation(JList.VERTICAL);
        userListPane.setCellRenderer(new CustomRenderer());   
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new java.awt.Dimension(25, 25));
        
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
        
        
        ChangeListener changeListener = new ChangeListener(){ //use this.population ??
            public void stateChanged(ChangeEvent changeEvent){
                //javax.swing.JTabbedPane tabbedPane = (javax.swing.JTabbedPane)changeEvent.getSource();
                //int index = tabbedPane.getSelectedIndex();
                //ChannelPanel c = (ChannelPanel)tabbedPane.getComponentAt(index);
                updateTabInfo();
            }          
        };
        tabbedPane.addChangeListener(changeListener);   
        }
     
        
        
        public String makeTimestamp()
        {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            String formattedDate = sdf.format(date);
            return formattedDate;
        }
        public void updateTabInfo()
        {
            
            if (this.isShowing())
            {
                String text ="";
                if (ops != 1) text = " ops) ";
                if (ops == 1) text = " op) ";
                        
                if (server == null)
                {
                    tabInfo.setText(name+"  ");
                }
                else tabInfo.setText(name+" - "+population+" nicks ("+ops+text+server+"  ");
            }
        }
        public void insertString(String line, String color) throws BadLocationException, IOException
        { 
                line = escapeHtml4(line);
                String timestamp="";
                if (showTimestamp == true) timestamp = "<font color="+timestampColor+">["+makeTimestamp()+"]</font>";
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
                doc.insertAfterEnd(body,"<div align='left'><font face="+ChannelPanel.font+" color="+color+">"+timestamp+" "+line+"</font></div>");
                return;
        }
                
        public void addToUserList(String nick) throws BadLocationException, IOException
        {
            nick = nick.trim();
            char first = nick.charAt(0);
            SortedListModel<User> model = (SortedListModel<User>) this.userListPane.getModel();

            if (first == '+' || first == '@' || first == '&' || first == '%' || first == '~')
            {
                if (first != '+') ops++;
                model.addElement(new User(first+nick.substring(1)));
            }
            else model.addElement(new User(" "+nick));
            
            population = model.getSize();
            updateTabInfo();
            return;
        }
        
        public void addManyToUserList(String nick)
        {
            if (nick.charAt(0) != ' ' && nick.charAt(0) != '+') ops++;
            SortedListModel<User> model = (SortedListModel<User>) this.userListPane.getModel();
            model.addManyElements(new User(nick));
            updateTabInfo();
            return;
        }
        public boolean contains(Object o)
        {
            SortedListModel<User> model = (SortedListModel<User>)this.userListPane.getModel();
            String user = (String)o;
            User u = new User(user);
            return model.contains(u);
        }
        
        public void fireIntervalAdded()
        {
            SortedListModel<String> model = (SortedListModel<String>) this.userListPane.getModel();            
            model.fireIntervalAdded();
            population = model.getSize();
            updateTabInfo();
            return;
        }
        
        public boolean removeFromUserList(String nick) throws BadLocationException, IOException
        {
            String[] prefix = new String[] {" ","+","@","~","&"};
            SortedListModel<User>  model = (SortedListModel<User>) this.userListPane.getModel();
            int oldPop = model.getSize();
            boolean success = false;
            
            for (int i = 0; i < prefix.length; i++)
            {
                success = model.removeElement(new User(prefix[i]+nick));
                if (success == true){
                    if (!prefix[i].equals(" ") && prefix[i].equals("+")) ops--;
                    break;
                }
            }
            
            population = model.getSize();
            updateTabInfo();
            return !(oldPop == population);
        }     
        
        public void clear()
        {
            SortedListModel<User> model = (SortedListModel<User>) userListPane.getModel();
            ops = 0;
            population = 0;
            model.removeAll();
            return;
        }
        
        public class SortedListModel<User> extends AbstractListModel
        {
            SortedSet<User> set;
            NickComparator comparator;
            public SortedListModel()
            {
                comparator = new NickComparator();
                set = new TreeSet<User>(comparator);
            }
            @Override
            public int getSize()
            {
                return set.size();
            }
            @Override
            public User getElementAt(int index)
            {
                return (User) set.toArray()[index];
            }
            public boolean contains(Object o)
            {
                User u = (User)o;
                return set.contains(u);
            }
            public boolean addElement(User x)
            {
                boolean success = set.add(x);
                fireIntervalAdded(this, 0, set.size()-1);
                return success;
            }
            public void addManyElements(User x)
            {
                set.add(x);
                return;
            }
            public void fireIntervalAdded()
            {
                fireIntervalAdded(this, 0,0);
            }
            public boolean removeElement(User x)
            {  
                boolean success = set.remove(x);
                fireIntervalRemoved(this, 0, 0);
                return success;
            }
            public void removeAll()
            {
                set.clear();
                fireIntervalRemoved(this,0,set.size());
            }
        }
            public class NickComparator implements java.util.Comparator{
                public int compare(Object o1, Object o2)
                {
                    String p1 = ((User)o1).getText().toLowerCase();
                    String p2 = ((User)o2).getText().toLowerCase();
                    return p1.compareTo(p2);
                }
            }
    }
    class CustomRenderer extends JLabel implements ListCellRenderer
    {
        final static ImageIcon iconWhite = new ImageIcon("src/icons/chatPaneWhite.png");
        final static ImageIcon iconGreen = new ImageIcon("src/icons/chatPaneGreen.png");
        final static ImageIcon iconOrange = new ImageIcon("src/icons/chatPaneOrange.png");
        final static ImageIcon iconPurple = new ImageIcon("src/icons/chatPanePurple.png");
        final static ImageIcon iconRed = new ImageIcon("src/icons/chatPaneRed.png");
        final static ImageIcon iconBlue = new ImageIcon("src/icons/chatPaneBlue.png");

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            ImageIcon icon = iconWhite;
            User user = (User)value;
            if (user.mode == '@') icon = iconGreen;
            if (user.mode == '%') icon = iconOrange;
            if (user.mode == '&') icon = iconPurple;
            if (user.mode == '~') icon = iconRed;
            if (user.mode == '+') icon = iconBlue;


            //label.setBackground(isSelected?Color.red: Color.blue);
            //label.setForeground(isSelected?Color.orange:Color.green);
            //label.setText(label.getText().substring(1));
            user.setIcon(icon);
            return user;
        }
    }
    class User extends JLabel implements Comparable
    {
        final char mode;
        public User(String nick)
        {
            super(nick.substring(1));
            mode = nick.charAt(0);
        }
        @Override
        public int compareTo(Object o)
        {
            System.out.println("CHECKHECK");
            User u = (User)o;
            return (this.getText().compareTo(u.getText()));
        }

    }