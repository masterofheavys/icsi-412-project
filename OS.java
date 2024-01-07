import java.io.FileNotFoundException;
import java.io.IOException;

public class OS {
    private static Kernel osKernel;
    private static FakeFileSystem osFFS = new FakeFileSystem();
    public static int swap;
    public static int pageNumber = 1;


    //start initial process
    public static void Startup(UserlandProcess init) throws InterruptedException, IOException {
        osKernel = new Kernel();
        swap = osFFS.Open("file src/swap.txt");
        CreateProcess(init);
    }

    //method to add processes after os has started
    public static int CreateProcess(UserlandProcess up) throws InterruptedException, IOException {
        return osKernel.createProcess(up,osKernel);
    }

    public static int CreateProcess(UserlandProcess up, priority prio) throws InterruptedException, IOException {
        return osKernel.createProcess(up,prio,osKernel);
    }

    public static void sleep(int milliseconds) throws InterruptedException, IOException {
        osKernel.sleep(milliseconds);
    }
    //OS methods for message handling, just pass to kernel
    public static int getPID()
    {
        return osKernel.getPID();
    }
    public static int getPIDByName(String name)
    {
        return osKernel.getPIDByName(name);
    }

    public static void SendMessage(KernelMessage km) throws IOException, InterruptedException {
        osKernel.sendMessage(km);
    }

    public static KernelMessage waitForMessage() throws IOException, InterruptedException
    {

        return osKernel.waitForMessage();
    }

    //OS methods dealing with devices
    public static int Open(String s) throws FileNotFoundException {
        return osKernel.Open(s);
    }

    public static void Close(int id) throws IOException {
        osKernel.Close(id);
    }

    public static byte[] Read(int id, int size) throws IOException {
        return osKernel.Read(id,size);
    }

    public static int Write(int id, byte[] data) throws IOException {
        return osKernel.Write(id,data);
    }

    public static void Seek(int id, int to) throws IOException {
        osKernel.Seek(id,to);
    }

    //OS methods dealing with memory allocation
    public static void GetMapping(int virtualPageNumber) throws IOException, InterruptedException {
        osKernel.GetMapping(virtualPageNumber);
    }

    public static int allocateMemory(int size)
    {
        return osKernel.allocateMemory(size);
    }

    public static void invalidMemoryAccess() throws IOException, InterruptedException {
        osKernel.invalidMemoryAccess();
    }

    public static boolean freeMemory(int pointer, int size)
    {
        return osKernel.freeMemory(pointer,size);
    }


}
