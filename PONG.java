import java.io.IOException;

public class PONG extends UserlandProcess
{
    @Override
    public void run()
    {
        //pong message to test out message handling
        while(true)
        {
            //recieve message from ping
            String pong = "PONG";
            KernelMessage rcv = null;
            try {
                rcv = OS.waitForMessage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(rcv != null)
            {
                //print message from ping
                safePrintln("FROM " + rcv.getSenderPID() + " TO " + rcv.getTargetPID() + " "  + "what: " + rcv.getMsgType() + " " +  new String(rcv.getData()));
                try {
                    //send message to ping
                    OS.SendMessage(new KernelMessage(OS.getPID(),OS.getPIDByName("PING"), rcv.getMsgType(), pong.getBytes()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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
