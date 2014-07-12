import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

public class IRCTabbedPane extends JTabbedPane{
 
  protected void processMouseEvent(MouseEvent evt)
  {
    if (evt.getID() == MouseEvent.MOUSE_PRESSED
        && evt.getButton() == MouseEvent.BUTTON3)
    {
      int index = indexAtLocation(evt.getX(), evt.getY());
      if (index != -1)
      {
        final ChannelPanel channel = (ChannelPanel)this.getComponentAt(index);
        {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem closeButton = new JMenuItem("Close");                   
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    channel.closeTab();
                }
            });
        popupMenu.add(closeButton);
        popupMenu.show(evt.getComponent(), evt.getX(), evt.getY()-20); 

        }
      }
    }
    else
    {
      super.processMouseEvent(evt);
    }
  }
}
