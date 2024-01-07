import java.io.FileNotFoundException;
import java.io.IOException;

public class fileTest2 extends UserlandProcess{
    @Override
    public void run() {
        try {
            int id = OS.Open("file src/test3.txt");
            byte [] data = OS.Read(id, 1000);
            id = OS.Open("file src/test2.txt");
            OS.Write(id, data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
