import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;

public class IRCTabbedPane extends JTabbedPane{
 
  protected void processMouseEvent(MouseEvent evt)
  {
    if (evt.getID() == MouseEvent.MOUSE_PRESSED
        && evt.getButton() == MouseEvent.BUTTON3)
    {
      final int index = indexAtLocation(evt.getX(), evt.getY());
      if (index != -1)
      {
        final ChannelPanel channel = (ChannelPanel)this.getComponentAt(index);
        {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem enableNotificationsButton = new JMenuItem("Enable Notifications");
            if (channel.enableNotifications) enableNotificationsButton.setIcon(GUI.checkedBoxIcon);
            JMenuItem closeButton = new JMenuItem("Close", GUI.closeTabIcon);
            JMenuItem moveTabLeft = new JMenuItem("Move Tab Left", GUI.moveTabLeftIcon);
            if (index == 0) moveTabLeft.setForeground(Color.gray);
            JMenuItem moveTabRight = new JMenuItem("Move Tab Right", GUI.moveTabRightIcon);
            if (index == getTabCount()-1) moveTabRight.setForeground(Color.gray);
                        
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    channel.closeTab();
                }
            });
            moveTabLeft.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int thisIndex = index;
                    int selectedIndex = getSelectedIndex();
                    if (thisIndex <= 0) return;
                    ChannelPanel moved = (ChannelPanel)getComponentAt(thisIndex);
                    Color c = getForegroundAt(thisIndex);
                    String label = getTitleAt(thisIndex);
                    add(moved,thisIndex-1);
                    setTitleAt(thisIndex-1, label);
                    setForegroundAt(thisIndex-1, c);
                    if (selectedIndex == thisIndex) setSelectedIndex(thisIndex-1);
                    //broken
                    return;             
                }
            });
            moveTabRight.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int thisIndex = index+1;
                    int selectedIndex = getSelectedIndex();
                    if (thisIndex >= getTabCount()) return;
                    ChannelPanel moved = (ChannelPanel)getComponentAt(thisIndex);
                    Color c = getForegroundAt(thisIndex);
                    String label = getTitleAt(thisIndex);
                    add(moved,thisIndex-1);
                    setTitleAt(thisIndex-1, label);
                    setForegroundAt(thisIndex-1, c);
                    if (selectedIndex == thisIndex) setSelectedIndex(thisIndex-1);
                    //broken
                    return;                
                }
            });
            enableNotificationsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    channel.enableNotifications = !channel.enableNotifications;
                }
            });
            popupMenu.add(enableNotificationsButton);
            if (channel.name.equals(channel.server))
            {
                
                
                JMenuItem disconnectButton = new JMenuItem("Disconnect", GUI.disconnectIcon);
                disconnectButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    channel.model.removeAll();
                    channel.connection.disconnect();
                    for (int i = 0; i < getTabCount(); i++)
                    {
                        ChannelPanel otherChannel = (ChannelPanel)getComponentAt(i);

                        if (otherChannel.name.equals(channel.server))
                        {
                            try {
                                String[] msg = {null, "[Info] Disconnected from "+otherChannel.server+" (port "+otherChannel.connection.port+")"};
                                otherChannel.insertString(msg, ChannelPanel.serverStyle, false);
                                continue;
                            } catch (BadLocationException ex) {
                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        if (otherChannel.connection == channel.connection)
                        {
                            setForegroundAt(i, Color.gray);
                            otherChannel.model.removeAll();
                        }

                    }
                    channel.updateTabInfo();
                    return;
                    }
                });
                
                
                JMenuItem reconnectButton = new JMenuItem("Reconnect", GUI.reconnectIcon); 
                reconnectButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ChannelPanel selected = (ChannelPanel)getSelectedComponent();
                        if (selected.connection.isConnected)
                        {
                            selected.connection.disconnect();
                        }
                        for (int j = 0; j < getTabCount(); j++)
                        {
                            ChannelPanel otherChannel = (ChannelPanel)getComponentAt(j);
                            if (selected.connection == otherChannel.connection)
                            {
                                selected.connection.thread = new Thread(selected.connection);
                                selected.connection.thread.start();
                                break;
                            }
                        }
                        selected.connection.autoconnect = false;

                        for (int i = 0; i < getTabCount(); i++)
                        {
                            ChannelPanel channel = (ChannelPanel)getComponentAt(i);
                            String title = getTitleAt(i);
                            if (channel.connection == selected.connection)
                            {
                                if (title.startsWith("#"))
                                {
                                    try {
                                        selected.connection.send("JOIN "+title);
                                    } catch (IOException ex) {
                                        //do nothing
                                    } catch (BadLocationException ex) {
                                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                    }
                }); 
                
            popupMenu.add(disconnectButton);
            popupMenu.add(reconnectButton);
            
        }
        popupMenu.add(new JSeparator());
        popupMenu.add(moveTabLeft);
        popupMenu.add(moveTabRight);
        popupMenu.add(closeButton);
        popupMenu.show(evt.getComponent(), evt.getX(), evt.getY()-88); 

        }
      }
    }
    else
    {
      super.processMouseEvent(evt);
    }
  }
}
