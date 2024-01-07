import java.io.IOException;

/*
Basic test process, just an infinite loop that prints hello world
Sleep code taken from project description
 */
public class HelloWorld extends UserlandProcess {
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
            safePrintln("hi");
            try {
                Write(start, (byte) 3);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
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
