import java.io.FileNotFoundException;
import java.io.IOException;

public class fileTest3 extends UserlandProcess{

    @Override
    public void run() {
        int id = 0;
        try {
            id = OS.Open("random 100");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        byte [] data = new byte[0];
        try {
            data = OS.Read(id, 100);
            System.out.println(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            OS.Write(id, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
