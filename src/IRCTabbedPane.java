import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

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
                    if (thisIndex <= 0) return;
                    ChannelPanel moved = (ChannelPanel)getComponentAt(thisIndex);
                    String label = getTitleAt(thisIndex);
                    add(moved,thisIndex-1);
                    setTitleAt(thisIndex-1, label);
                    return;             
                }
            });
            moveTabRight.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int thisIndex = index+1;
                    if (thisIndex >= getTabCount()) return;
                    ChannelPanel moved = (ChannelPanel)getComponentAt(thisIndex);
                    String label = getTitleAt(thisIndex);
                    add(moved,thisIndex-1);
                    setTitleAt(thisIndex-1, label);
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
            JMenuItem reconnectButton = new JMenuItem("Reconnect", GUI.reconnectIcon);
            JMenuItem joinChannelButton = new JMenuItem("Join Channel", GUI.joinChannelIcon);
            popupMenu.add(disconnectButton);
            popupMenu.add(reconnectButton);
            popupMenu.add(joinChannelButton);
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
