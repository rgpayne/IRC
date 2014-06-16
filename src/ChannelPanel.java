import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
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
        final JList<User> userListPane;
        
        
        final JScrollPane jScrollPane1 = new JScrollPane(), jScrollPane2 = new JScrollPane();
        static JTabbedPane tabbedPane;
        static JLabel tabInfo;
        
        SortedListModel<User> model;
        Set<String> userSet = new TreeSet<String>();
        ArrayList<String> list = new ArrayList<String>();
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        
        static String errorColor = "#FF0000", chatColor="Black", serverColor="#990099", connectColor="#993300", timestampColor="#909090";
        static String font = "Sans Serif";
        
        ArrayList<String> history = new ArrayList<String>();
        int historyCounter = 0;
        boolean showTimestamp = true;
        
        
        

    
        public ChannelPanel(String name, String nick, Connection c) throws BadLocationException, IOException
        {
            this.name = name;
            this.connection = c;
                    
            doc = (HTMLDocument)htmlKit.createDefaultDocument();
            chatPane.setContentType("text/html");
            chatPane.setDocument(doc);
            chatPane.setText(topic);
            model = new SortedListModel<User>();
            userListPane = new JList(model);
            userListPane.setModel(model);
            userListPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            userListPane.setLayoutOrientation(JList.VERTICAL);
            userListPane.setCellRenderer(new CustomRenderer());
            
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
     
        public String makeTimestamp()
        {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            String formattedDate = sdf.format(date);
            return formattedDate;
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
                model.addElement(new User(first+nick.substring(1)));
            }
            else model.addElement(new User(" "+nick));
            
            population = model.getSize();
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return;
        }
        public void addManyToUserList(String nick)
        {
            SortedListModel<User> model = (SortedListModel<User>) this.userListPane.getModel();
            model.addManyElements(new User(nick));
        }
        public void fireIntervalAdded()
        {
            SortedListModel<String> model = (SortedListModel<String>) this.userListPane.getModel();            
            model.fireIntervalAdded();
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
                if (success == true) break;
            }
            
            population = this.model.getSize();
            if (this.isShowing()) tabInfo.setText(Integer.toString(this.population)+" nicks     ");
            return !(oldPop == population);
         }     
        
        public void clear()
        {
            SortedListModel<User>  model = (SortedListModel<User>) userListPane.getModel();
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
class CustomRenderer extends JLabel implements ListCellRenderer{
    final static ImageIcon iconWhite = new ImageIcon("/home/ross/NetBeansProjects/IRC/src/icons/chatPaneWhite.png");
    final static ImageIcon iconGreen = new ImageIcon("/home/ross/NetBeansProjects/IRC/src/icons/chatPaneGreen.png");
    final static ImageIcon iconOrange = new ImageIcon("/home/ross/NetBeansProjects/IRC/src/icons/chatPaneOrange.png");
    final static ImageIcon iconPurple = new ImageIcon("/home/ross/NetBeansProjects/IRC/src/icons/chatPanePurple.png");
    final static ImageIcon iconRed = new ImageIcon("/home/ross/NetBeansProjects/IRC/src/icons/chatPaneRed.png");
    final static ImageIcon iconBlue = new ImageIcon("/home/ross/NetBeansProjects/IRC/src/icons/chatPaneBlue.png");
    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        //Font theFont = null;
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

    public int compareTo(Object o)
    {
        User u = (User)o;
        return (this.getText().compareTo(u.getText()));

    }

}