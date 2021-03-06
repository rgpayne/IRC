import java.io.Serializable;
import java.util.ArrayList;

class SavedConnection implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String server;
    private String password;
    private ArrayList<String> channels;
    private boolean autoConnect;
    private int port;

    SavedConnection(String name, String server, String password, ArrayList<String> channels, boolean autoConnect, int port) {
        this.name = name;
        this.server = server;
        this.password = password;
        this.channels = channels;
        this.autoConnect = autoConnect;
        this.port = port;
    }

    SavedConnection() {

    }

    /**
     * @return the name -- used exclusively for BeanTableModel
     */
    public String getNetwork() {
        return name;
    }

    /**
     * @return the server -- used exclusively for BeanTableModel
     */
    public String getServer() {
        return server;
    }


    /**
     * @return the channels -- used exclusively for BeanTableModel
     */
    public ArrayList<String> getChannels() {
        return channels;
    }


    public String retrieveName() {
        return name;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public String retrieveServer() {
        return server;
    }

    public void changeServer(String server) {
        this.server = server;
    }

    public String retrievePassword() {
        return password;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public ArrayList<String> retrieveChannels() {
        return channels;
    }

    public void changeChannels(ArrayList<String> channels) {
        this.channels = channels;
    }

    public int retrievePort() {
        return port;
    }

    public void changePort(int port) {
        this.port = port;
    }

    public boolean retrieveAutoConnect() {
        return autoConnect;
    }

    public void changeAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }
}