public class KernelMessage
{
    private int senderPID;
    private int targetPID;
    private int msgType;
    private byte[] data;

    public KernelMessage(int senderPID,int targetPID,int msgType,byte[] data)
    {
        this.senderPID= senderPID;
        this.targetPID = targetPID;
        this.msgType = msgType;
        this.data = data;
    }
    //copy constructor
    public KernelMessage(KernelMessage toCopy)
    {
        senderPID = toCopy.senderPID;
        targetPID =toCopy.targetPID;
        data = toCopy.getData();
        msgType = toCopy.getMsgType();
        data = toCopy.getData();
    }

    public int getSenderPID()
    {
        return senderPID;
    }

    public byte[] getData()
    {
        return data;
    }

    public int getMsgType()
    {
        return msgType;
    }

    public int getTargetPID()
    {
        return targetPID;
    }

    public void setSenderPID(int senderPID)
    {
        this.senderPID = senderPID;
    }

    public String toString()
    {
        String toRet = "senderPID " + senderPID + " targetPID "  + targetPID + " message type " + msgType + " data " + new String(data);
        return toRet;
    }
}
