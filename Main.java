import java.io.IOException;

public class Main {
    //code to startup the OS
    public static void main(String[]args) throws InterruptedException, IOException {
        OS.Startup(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld());
        OS.CreateProcess(new AttemptToAcessInvalid());
        //OS.CreateProcess(new NotEepy(),priority.background);
    }
}
