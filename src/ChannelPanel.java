import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import org.apache.commons.lang3.StringUtils;

    public class ChannelPanel extends JSplitPane{
           
        final String name, title;
        String topic="", signOnTime, topicAuthor, server;
        int population, ops = 0;
        static boolean awayStatus = false;
        static String awayMessage = "Reason";
        static String CTCPFingerMessage = "this is the finger message", CTCPUserInfo = "user info string";
        int listSelectedIndex = -1;
        boolean rem = false;
        boolean add = false;
        
        Connection connection;
        
        final JTextField chatInputPane = new JTextField();       
        final JTextPane chatPane = new JTextPane();
        final JList<User> userListPane;
        
        DefaultCaret caret;
        final JScrollPane userListScrollPane = new JScrollPane(), chatScrollPane = new JScrollPane();
        static JTabbedPane tabbedPane;
        static JLabel tabInfo;
        
        SortedListModel<User> model = new SortedListModel<User>();
        ArrayList<String> list = new ArrayList<String>();
        
        StyledDocument doc;  
        StyleContext sc = StyleContext.getDefaultStyleContext();
        static Style style, chatStyle, timestampStyle, actionStyle, errorStyle, serverStyle, connectStyle, ctcpStyle, userNameStyle, disconnectStyle, joinStyle;
        final static String errorColor = "#FF0000", chatColor="#000000", serverColor="#960096", connectColor="#993300", timestampColor="#909090";
        final static String actionColor = "#0000FF", disconnectColor = "#CAA234", joinColor = "#D46942";
        final static String font = "courier";
        final static Color CTCP0 = Color.WHITE, CTCP1 = Color.BLACK, CTCP2 = Color.decode("#000080"), CTCP3 = Color.decode("#008000"), CTCP4 = Color.decode("#FF0000"),
                           CTCP5 = Color.decode("#A52A2A"), CTCP6 = Color.decode("#800080"), CTCP7 = Color.decode("#FF8000"), CTCP8 = Color.decode("#808000"),
                           CTCP9 = Color.decode("#00FF00"), CTCP10 = Color.decode("#008080"), CTCP11 = Color.decode("#00FFFF"), CTCP12 = Color.decode("#0000FF"),
                           CTCP13 = Color.decode("#FFC0CB"), CTCP14 = Color.decode("#A0A0A0"), CTCP15 = Color.decode("#C0C0C0");
        final static Map CTCPMap = new HashMap(), chatColorMap = new HashMap(), userMap = new HashMap();
        boolean showTimestamp = true, chatNameColors = true;
        
        ArrayList<String> history;
        int historyCounter = 0;
           
        public ChannelPanel(String title, String name, String nick, Connection c) throws BadLocationException, IOException
        {
            this.title = title; //this is what is shown on a tab
            this.name = name;
            this.connection = c;
                                
            doc = chatPane.getStyledDocument();
                    
            setStyles();
            
            chatPane.setDocument(doc);
            userListPane = new JList(model);
            if (showTimestamp == true) history = new ArrayList<String>();
            
            
            makePanel();
            makeHashMaps();
                       
            tabbedPane.add(this, this.title);
            
                
        }

        private void makePanel() throws BadLocationException, IOException
        {      
            
        userListPane.setModel(model);
        userListPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userListPane.setLayoutOrientation(JList.VERTICAL);
        userListPane.setCellRenderer(new CustomRenderer());   
        userListPane.setAutoscrolls(false);
        userListPane.setFocusable(false);
        userListPane.setMaximumSize(new Dimension(25, 25));
        userListPane.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean adjust = e.getValueIsAdjusting();
                if (!adjust)
                {
                    JList list = (JList)e.getSource();
                    int min = list.getMinSelectionIndex();
                    System.out.println(min+" "+add+" "+rem);
                    if (min > -1 && min < list.getModel().getSize()-1)
                    {
                        if (listSelectedIndex == -1)
                        {
                            listSelectedIndex = min;
                        }
                    }
                    if (add == true) list.setSelectedIndex(listSelectedIndex);
                    if (rem == true)
                    {
                        rem = false;
                        //listSelectedIndex++;
                        list.setSelectedIndex(listSelectedIndex);
                    }
                    
                    add = false;
                    rem = false;
                }
            }
        });
        
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setDividerLocation(540);
        setResizeWeight(1.0);
        setDividerSize(5);
        setVerifyInputWhenFocusTarget(false);
        
        chatPane.setEditable(false);
        chatPane.setAutoscrolls(true);
        userListScrollPane.setViewportView(userListPane);
        chatScrollPane.setViewportView(chatPane);


        setLeftComponent(chatScrollPane);
        if (name.startsWith("#")) setRightComponent(userListScrollPane);
        else{
            setRightComponent(null);
            setDividerSize(0);
        }
        ChangeListener changeListener = new ChangeListener(){
            public void stateChanged(ChangeEvent changeEvent)
            {
                JTabbedPane pane = (JTabbedPane)changeEvent.getSource();
                int index = pane.getSelectedIndex();
                pane.setForegroundAt(index, Color.BLACK);
                updateTabInfo();
            }          
        };
        tabbedPane.addChangeListener(changeListener);   
        
        caret = (DefaultCaret)chatPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }
     
        
        public void setStyles() //TODO: static styles so we dont have to decode on every insertString
        {
        style = sc.addStyle("DefaultStyle", null);
        chatStyle = sc.addStyle("DefaultStyle", style);
        timestampStyle = sc.addStyle("DefaultStyle", style);
        actionStyle = sc.addStyle("Defaultstyle", style);
        errorStyle = sc.addStyle("DefaultStyle", style);
        serverStyle = sc.addStyle("Defaultstyle", style);
        connectStyle = sc.addStyle("Defaultstyle", style);
        ctcpStyle = sc.addStyle("Defaultstyle", style);
        userNameStyle = sc.addStyle("Defaultstyle", style);
        disconnectStyle = sc.addStyle("Defaultstyle", style);
        joinStyle = sc.addStyle("Defaultstyle", style);
        
        StyleConstants.setFontFamily(style, font);
        StyleConstants.setFontSize(style, 12);
        StyleConstants.setForeground(chatStyle, Color.decode(chatColor));
        StyleConstants.setForeground(timestampStyle, Color.decode(timestampColor) );
        StyleConstants.setForeground(actionStyle, Color.decode(actionColor));
        StyleConstants.setForeground(errorStyle, Color.decode(errorColor));
        StyleConstants.setForeground(serverStyle, Color.decode(serverColor));
        StyleConstants.setForeground(connectStyle, Color.decode(connectColor));  
        StyleConstants.setForeground(disconnectStyle, Color.decode(disconnectColor));
        StyleConstants.setForeground(joinStyle, Color.decode(joinColor));
        }
        public void makeHashMaps()
        {
            CTCPMap.put(0, CTCP0);
            CTCPMap.put(1, CTCP1);
            CTCPMap.put(2, CTCP2);
            CTCPMap.put(3, CTCP3);
            CTCPMap.put(4, CTCP4);
            CTCPMap.put(5, CTCP5);
            CTCPMap.put(6, CTCP6);
            CTCPMap.put(7, CTCP7);
            CTCPMap.put(8, CTCP8);
            CTCPMap.put(9, CTCP9);
            CTCPMap.put(10, CTCP10);
            CTCPMap.put(11, CTCP11);
            CTCPMap.put(12, CTCP12);
            CTCPMap.put(13, CTCP13);
            CTCPMap.put(14, CTCP14);
            CTCPMap.put(15, CTCP15);
            
            chatColorMap.put(0, Color.decode("#E90E7F"));
            chatColorMap.put(1, Color.decode("#B30E0E"));
            chatColorMap.put(2, Color.decode("#8E55E9"));
            chatColorMap.put(3, Color.decode("#18B33C"));
            chatColorMap.put(4, Color.decode("#58ADB3"));
            chatColorMap.put(5, Color.decode("#9E54B3"));
            chatColorMap.put(6, Color.decode("#B39875"));
            chatColorMap.put(7, Color.decode("#3176B3"));
        }
        public String makeTimestamp()
        {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            String formattedDate = sdf.format(date);
            return formattedDate;
        }
        public Color getUserColor(String user)
        {
            if (userMap.containsKey(user)) return (Color)userMap.get(user);
            else
            {
                int random = (int)(Math.random() * 8);
                userMap.put(user, chatColorMap.get(random));
                return (Color)userMap.get(user);
            }
        }
        public void updateTabInfo()
        {
            if (tabbedPane.getTabCount() == 0)
            {
                tabInfo.setText("Disconnected    ");
                return;
            }
            if (connection.socket.isClosed())
            {
                tabInfo.setText("Disconnected    ");
                return;
            }
            
            if (this.isShowing())
            {
                String text ="";
                if (ops != 1) text = " ops) ";
                if (ops == 1) text = " op) ";
                    
                ChannelPanel cc = (ChannelPanel)tabbedPane.getSelectedComponent();
                if (!cc.name.startsWith("#"))
                {
                    tabInfo.setText(name+"  ");
                }
                else tabInfo.setText(name+" - "+population+" nicks ("+ops+text+server+"  ");
            }
        }
        public void insertString(String[] line, Style givenStyle, boolean isCTCP) throws BadLocationException, IOException
        {
            if (isCTCP == true)
            {
                insertCTCPColoredString(line, givenStyle);
                return;
            }
            if (showTimestamp == true)
            {
                String timestamp = makeTimestamp();
                doc.insertString(doc.getLength(), "["+timestamp+"] ", timestampStyle);
            }
            if (chatNameColors == false && line[0] != null) doc.insertString(doc.getLength(), "<"+line[0]+">: ", chatStyle);
            if (chatNameColors == true && line[0] != null)
            {
                Color c = getUserColor(line[0]);
                doc.insertString(doc.getLength(), "<", style);
                StyleConstants.setForeground(userNameStyle, c);
                doc.insertString(doc.getLength(), line[0], userNameStyle);
                doc.insertString(doc.getLength(), ">: ", style);
            }
            doc.insertString(doc.getLength(), line[1]+"\n", givenStyle);
            checkForActiveTab();
        }
        public void insertCTCPAction (String[] line) throws BadLocationException
        {
            String nick = line[0];
            String msg = line[1].trim();
            String timestamp = makeTimestamp();
            if (showTimestamp == true) doc.insertString(doc.getLength(), "["+timestamp+"] " ,timestampStyle);
            doc.insertString(doc.getLength(),"* " , actionStyle);
            if (chatNameColors == false) doc.insertString(doc.getLength(),nick+" ", style);
            else
            {
                Color c = getUserColor(nick);
                StyleConstants.setForeground(userNameStyle, c);
                doc.insertString(doc.getLength(), nick+" ", userNameStyle);
            }
            doc.insertString(doc.getLength(), msg+"\n", actionStyle);
            checkForActiveTab();
        }
        public void insertCTCPColoredString(String[] line, Style givenStyle) throws BadLocationException
        {
            ctcpStyle = sc.addStyle("Defaultstyle", givenStyle);
            String timestamp = makeTimestamp();
            if (showTimestamp == true) doc.insertString(doc.getLength(), "["+timestamp+"] " ,timestampStyle);
            if (chatNameColors == false && line[0] != null) doc.insertString(doc.getLength(), "<"+line[0]+">: ", chatStyle);
            if (chatNameColors == true && line[0] != null)
            {
                Color c = getUserColor(line[0]);
                doc.insertString(doc.getLength(), "<", style);
                StyleConstants.setForeground(userNameStyle, c);
                doc.insertString(doc.getLength(), line[0], userNameStyle);
                doc.insertString(doc.getLength(), ">: ", style);
            }
            
            
            Pattern pattern;
            Matcher matcher;
            
            StringTokenizer st = new StringTokenizer(line[1],Connection.CTCP_COLOR_DELIM + Connection.CTCP_UNDERLINE_DELIM + Connection.CTCP_BOLD_DELIM + Connection.CTCP_RESET_DELIM, true);
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                if (token.equals(Connection.CTCP_BOLD_DELIM))
                {
                    StyleConstants.setBold(ctcpStyle, !StyleConstants.isBold(ctcpStyle));
                    continue;
                }
                if (token.equals(Connection.CTCP_UNDERLINE_DELIM))
                {
                    StyleConstants.setUnderline(ctcpStyle, !StyleConstants.isUnderline(ctcpStyle));
                    continue;
                }
                if (token.equals(Connection.CTCP_RESET_DELIM))
                {
                    ctcpStyle = sc.addStyle("Defaultstyle", givenStyle);
                    continue;
                }
                if (token.equals(Connection.CTCP_COLOR_DELIM))
                {
                    if (st.hasMoreTokens())
                    {
                        token = st.nextToken();
                        pattern = Pattern.compile("(\\d{1,2}+)(,?+)(\\d{1,2}+)(.+)|(\\d{1,2})(,)(.*)|(,?+)(\\d{1,2}+)(.+)");
                        matcher = pattern.matcher(token);
                        String foreground;
                        String background;
                        String message;                      

                        if (!StringUtils.isNumeric(token.substring(0,1)) && !token.startsWith(","))
                        {
                            doc.insertString(doc.getLength(), token, chatStyle);
                            continue;
                        }
                        while (matcher.find())
                        {
                            if (matcher.group(8) != null && matcher.group(8).equals(",")) //invalid (ex. ,5this is a message) prints plain
                            {
                                message = matcher.group(10);
                                doc.insertString(doc.getLength(), message, ctcpStyle);
                                continue;
                            }
                            if (matcher.group(6) != null && matcher.group(6).equals(",")) //foreground color, no bg color (ex. 5,this is a message)
                            {
                                foreground = matcher.group(5);
                                message = matcher.group(7);
                                Color f = (Color)CTCPMap.get(Integer.valueOf(foreground));
                                StyleConstants.setForeground(ctcpStyle, f);
                                doc.insertString(doc.getLength(), message, ctcpStyle);
                                continue;
                            }
                            if (matcher.group(8) != null && matcher.group(8).equals("")) //foregrond color, no bg color (ex. 5this is a message)
                            {
                                foreground = matcher.group(9);
                                message = matcher.group(10);
                                Color f = (Color)CTCPMap.get(Integer.valueOf(foreground));
                                StyleConstants.setBackground(ctcpStyle, Color.WHITE);
                                StyleConstants.setForeground(ctcpStyle, f);
                                doc.insertString(doc.getLength(), message, ctcpStyle);
                                continue;
                            }
                            if (matcher.group(2) != null && matcher.group(2).equals("")) //foreground color, no bg color, but followed by a number, ie 53 blind mice
                            {
                                foreground = matcher.group(1);
                                message = matcher.group(3)+matcher.group(4);
                                Color f = (Color)CTCPMap.get(Integer.valueOf(foreground));
                                StyleConstants.setForeground(ctcpStyle, f);
                                doc.insertString(doc.getLength(), message, ctcpStyle);
                                continue;
                            }
                            if (matcher.group(2) != null && matcher.group(2).equals(",")) //foreground and background (ex. 5,5this is a message)
                            {
                                foreground = matcher.group(1);
                                background = matcher.group(3);
                                message = matcher.group(4);
                                Color f = (Color)CTCPMap.get(Integer.valueOf(foreground));
                                Color b = (Color)CTCPMap.get(Integer.valueOf(background));
                                StyleConstants.setForeground(ctcpStyle, f);
                                StyleConstants.setBackground(ctcpStyle, b);
                                doc.insertString(doc.getLength(), message, ctcpStyle);
                                continue;
                            }                               
                        } 
                    }
                }
                else
                {
                    doc.insertString(doc.getLength(), token, ctcpStyle);
                }
            }
            doc.insertString(doc.getLength(), "\n", ctcpStyle);
            checkForActiveTab();
        }
        private void checkForActiveTab()
        {
            if (!this.isShowing()) //check for foreground color?
            { 
                int totalTabs = tabbedPane.getTabCount();
                int indexOfTab = -1;
                for (int i = 0; i < totalTabs; i++)
                {
                    ChannelPanel channel = (ChannelPanel)tabbedPane.getComponentAt(i);

                    String channelName = channel.name;
                    if (channelName.equalsIgnoreCase(this.name))
                    {
                        indexOfTab = i;
                        break;
                    }
                }
                final int ind = indexOfTab;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        tabbedPane.setForegroundAt(ind, Color.blue);
                    }
                });
            }
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
                User u = new User(first+nick.substring(1));
                model.addElement(u);
            }
            else model.addElement(new User(" "+nick));
            
            add = true;
            population = model.getSize();
            if (listSelectedIndex >= 0 && listSelectedIndex < model.getSize()-1) userListPane.setSelectedIndex(listSelectedIndex);
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
                User u = new User(prefix[i]+nick);
                success = model.removeElement(u);
                rem = true;
                if (success == true)
                {
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
            public synchronized int getSize()
            {
                return set.size();
            }
            @Override
            public synchronized User getElementAt(int index)
            {
                return (User) set.toArray()[index];
            }
            public synchronized boolean contains(Object o)
            {
                User u = (User)o;
                return set.contains(u);
            }
            public synchronized boolean addElement(User x)
            {
                boolean success = set.add(x);
                fireIntervalAdded(this, 0, set.size()-1);
                return success;
            }
            public synchronized void addManyElements(User x)
            {
                set.add(x);
                return;
            }
            public synchronized void fireIntervalAdded()
            {
                fireIntervalAdded(this, 0,0);
            }
            public synchronized boolean removeElement(User x)
            { 
                boolean success = set.remove(x);
                if (success)
                fireIntervalRemoved(this, 0, 0);
                return success;
            }
            public void removeAll()
            {
                set.clear();
                fireIntervalRemoved(this,0,set.size());
            }
        }
            public class NickComparator implements Comparator{
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
        final static ImageIcon iconWhite = new ImageIcon("src/icons/user-white.png");
        final static ImageIcon iconGreen = new ImageIcon("src/icons/user.png");
        final static ImageIcon iconOrange = new ImageIcon("src/icons/user-female.png");
        final static ImageIcon iconPurple = new ImageIcon("src/icons/user-red.png");
        final static ImageIcon iconRed = new ImageIcon("src/icons/user-green.png");
        final static ImageIcon iconBlue = new ImageIcon("src/icons/user-gray.png");

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

            if (isSelected)
            {
                Color color = (Color)ChannelPanel.chatColorMap.get(3);
                user.setBackground(color);
                user.setForeground(Color.WHITE);
            }
            else
            {
                user.setBackground(Color.WHITE);
                user.setForeground(Color.BLACK);
            }
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
            setOpaque(true);
            mode = nick.charAt(0);
            setFont(new Font("sans serif", Font.PLAIN, 12));
        }
        @Override
        public int compareTo(Object o)
        {
            User u = (User)o;
            return (this.getText().compareTo(u.getText()));
        }

    }