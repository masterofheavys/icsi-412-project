/*
Basic test process, just an infinite loop that prints goodbye world
Sleep code taken from project description
 */
public class NotEepy  extends UserlandProcess {
    @Override
    public void run() {
        while (true)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("im not sleepy");
        }
    }

}
