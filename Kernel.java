import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

/*
Kernel simulation, right now just creates a scheduler and uses it to call create process
 */
public class Kernel implements Device {
    //instantiate file system for the os
    VirtualFileSystem fileSystem = new VirtualFileSystem();
    private Scheduler kernelScheduler = new Scheduler();

    //array to track if page in use
    boolean[] memoryArr = new boolean[1000];


    //method to be called upon invalid memory access
    public void invalidMemoryAccess() throws IOException, InterruptedException {
        kernelScheduler.invalidMemoryAccess();
    }

    //allocate memory
    public int allocateMemory(int size)
    {
        //must be multiple of 1024
        if(size % 1024 != 0)
        {
            return -1;
        }
        int i = 0;
        int startPosition = 0;
        //code to find first large enough block of memory
        while(i - startPosition < size/1024)
        {
            if(memoryArr[startPosition+i] == true)
            {
                startPosition = startPosition+1;
                i = 0;
            }
            else
            {

                i++;
            }
        }
        //set memory as in use
        for(int j = startPosition; j < i + startPosition; j++)
        {
            memoryArr[j] = true;
            kernelScheduler.setMemoryUsage(j);
        }
        //set start position and size for removal later
        kernelScheduler.setStartposition(startPosition*1024);
        kernelScheduler.setSize(size*1024);
        //return start position
        return startPosition*1024;
    }


    public boolean freeMemory(int pointer, int size)
    {
        //make sure multiple of 1024
        if(size % 1024 != 0 || pointer % 1024 != 0)
        {
            return false;
        }
        //reset all memory
        for(int i = pointer/1024; i < pointer/1024 + size/1024; i++)
        {
            memoryArr[i/1024] = false;
            kernelScheduler.removeMemoryUsage(i/1024);
        }
        return true;
    }

    public void sendMessage(KernelMessage km) throws IOException, InterruptedException {
        //creates copy of kernel message
        KernelMessage toSend = new KernelMessage(km);
        //assign sender
        toSend.setSenderPID(getPID());
        //get target pid
        int targetPID = toSend.getTargetPID();
        //get process from target pid
        KernelandProcess process = kernelScheduler.processFromPID(targetPID);
        //add that message to its queue
        process.addMessageQueue(km);
        //remove message from the waiting queue
        kernelScheduler.restoreWaitingQueue(targetPID);
    }
    void GetMapping(int virtualPageNumber) throws IOException, InterruptedException {
        kernelScheduler.GetMapping(virtualPageNumber);
    }

    public KernelMessage waitForMessage() throws IOException, InterruptedException
    {
        //get the processes message queue
        LinkedList<KernelMessage> tmp = kernelScheduler.getMessageQueue();
        if(tmp != null && tmp.size() != 0)
        {
            //get the message off queue
            KernelMessage toRet = tmp.get(0);
            tmp.remove(0);
            //remove process from waiting queue
            kernelScheduler.restoreWaitingQueue(toRet.getTargetPID());
            //return message
            return toRet;
        }
        else
        {
            //tells process to wait for message
            kernelScheduler.waitForMessage();
            return null;
        }
    }
    public int createProcess(UserlandProcess up, Kernel passing) throws InterruptedException, IOException {
        return kernelScheduler.createProcess(up, passing,kernelScheduler);
    }

    public int createProcess(UserlandProcess up, priority prio, Kernel passing) throws InterruptedException, IOException {
        return kernelScheduler.createProcess(up,prio, passing,kernelScheduler);
    }

    public int getPID()
    {
        return kernelScheduler.getPID();
    }

    public int getPIDByName(String name)
    {
        return kernelScheduler.getPIDByName(name);
    }

    public void sleep(int milliseconds) throws InterruptedException, IOException {
        kernelScheduler.sleep(milliseconds);
    }

    @Override
    public synchronized int Open(String s) throws FileNotFoundException {
        //get the currently running process
        KernelandProcess currentlyRunning = kernelScheduler.getSchedulerKernelandProcess();
        int i = 0;
        //get first empty spot in devices array
        while(i < currentlyRunning.getDevicesArray().length && currentlyRunning.getDevicesArray()[i] != -1)
        {
            i++;
        }
        if(currentlyRunning.getDevicesArray()[i] != -1)
        {
            return -1;
        }
        //open the file
        int tmp = fileSystem.Open(s);
        if(tmp == -1)
        {
            return -1;
        }
        //set id
        currentlyRunning.getDevicesArray()[i] = tmp;
        return i;

    }

    @Override
    //calls virtual file system close using corresponding id
    public synchronized void Close(int id) throws IOException {
        KernelandProcess currentlyRunning = kernelScheduler.getSchedulerKernelandProcess();
        int vfsID = currentlyRunning.getDevicesArray()[id];
        currentlyRunning.getDevicesArray()[id] = -1;
        fileSystem.Close(vfsID);
    }

    @Override
    //calls virtual file system read using corresponding id
    public synchronized byte[] Read(int id, int size) throws IOException {
        KernelandProcess currentlyRunning = kernelScheduler.getSchedulerKernelandProcess();
        int vfsID = currentlyRunning.getDevicesArray()[id];
        return fileSystem.Read(vfsID,size);
    }

    @Override
    //calls virtual file system seek using corresponding id
    public synchronized void Seek(int id, int to) throws IOException {
        KernelandProcess currentlyRunning = kernelScheduler.getSchedulerKernelandProcess();
        int vfsID = currentlyRunning.getDevicesArray()[id];
        fileSystem.Seek(vfsID,to);
    }

    @Override
    //calls virtual file system write using corresponding id
    public synchronized int Write(int id, byte[] data) throws IOException {
        KernelandProcess currentlyRunning = kernelScheduler.getSchedulerKernelandProcess();
        int vfsID = currentlyRunning.getDevicesArray()[id];
        return fileSystem.Write(vfsID,data);
    }
}
