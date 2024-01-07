import java.io.FileNotFoundException;
import java.io.IOException;

public class fileTest extends UserlandProcess{

    @Override
    public void run() {
        try {
           int id = OS.Open("file src/test.txt");
           byte [] data = OS.Read(id, 100);
           id = OS.Open("file src/test2.txt");
           Thread.sleep(50);
           OS.Write(id, data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
