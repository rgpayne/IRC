
import java.io.Serializable;

public class ListChannel implements Serializable{
    private String Name;
    private String Topic;
    private int Users;
    
    public ListChannel()
    {
        
    }
    public ListChannel(String name, String info, int pop )
    {
        this.Name = name;
        this.Topic = info;
        this.Users = pop;
    }
    public String getName() 
    {
        return Name;
    }
    public String get_Topic()
    {
        return Topic;
    }
    public int getPop()
    {
        return Users;
    }
    @Override
    public String toString()
    {
        return Name+" "+Topic+" "+Users;
    }
            
}