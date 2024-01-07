//class to elminate need for 2d array
public class tlbEntry
{
    int virtualAdd;
    int physAdd;

    public tlbEntry(int virtualAdd, int physAdd)
    {
        this.virtualAdd = virtualAdd;
        this.physAdd = physAdd;
    }

    public int getPhysAdd()
    {
        return physAdd;
    }

    public int getVirtualAdd()
    {
        return virtualAdd;
    }

    public void setPhysAdd(int physAdd)
    {
        this.physAdd = physAdd;
    }

    public void setVirtualAdd(int virtualAdd)
    {
        this.virtualAdd = virtualAdd;
    }
}
