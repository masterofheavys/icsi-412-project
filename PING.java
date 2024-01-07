import java.io.IOException;

public class PING extends UserlandProcess
{
    //ping process to test message handling
    public void run()
    {
        int what = 0;
        while(true)
        {
            String ping = "PING";
            try {
                //send message with ping string to process pong getting the pid by name
                what++;
                OS.SendMessage(new KernelMessage(OS.getPID(),OS.getPIDByName("PONG"),what,ping.getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            KernelMessage rcv = null;
            try {
                //recive message
                rcv = OS.waitForMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(rcv != null)
            {
                //print out message
                safePrintln("FROM " + rcv.getSenderPID() + " TO " + rcv.getTargetPID() + " "  + "what: " + rcv.getMsgType() + " " +  new String(rcv.getData()));
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }
}
