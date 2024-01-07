import java.io.IOException;

//process that's trying to get invalid memory, will be killed
public class AttemptToAcessInvalid extends UserlandProcess {
    @Override
    public void run() {
        int start = OS.allocateMemory(1024);
        while (true)
        {
            try {
                //using os.sleep to test functionality
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                Write(2048, (byte) 99);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                System.out.println(Read(start));
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
