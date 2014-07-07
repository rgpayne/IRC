public class ListChannel {
    private String name;
    private String info;
    private int pop;
    
    public ListChannel(String name, String info, int pop )
    {
        this.name = name;
        this.info = info;
        this.pop = pop;
    }
    public String getName() 
    {
        return name;
    }
    public String getInfo()
    {
        return info;
    }
    public int getPop()
    {
        return pop;
    }
    @Override
    public String toString()
    {
        return name+" "+pop+" "+info;
    }
            
}