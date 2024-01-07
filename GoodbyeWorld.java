import java.io.IOException;

/*
Basic test process, just an infinite loop that prints goodbye world
 */
public class GoodbyeWorld  extends UserlandProcess {
    @Override
    public void run() {
        int start = OS.allocateMemory(10240);
        while (true)
        {
            try {
                //using os.sleep to test functionality
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            safePrintln("bye");
            try {
                Write(start+5, (byte) 4);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                System.out.println(Read(start+5));
            } catch (IOException e) {
                throw new RuntimeException(e);
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
