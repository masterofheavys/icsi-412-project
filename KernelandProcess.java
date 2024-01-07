import javax.swing.plaf.PanelUI;
import java.io.IOException;
import java.util.Formattable;
import java.util.LinkedList;

public class KernelandProcess {
    private static int nextpid = 1;
    private int pid;
    private boolean threadStarted = false;
    private Thread kernelandthread;
    private int numInterrrupt = 0;

    static int physPageNum = 0;

    //linked list to store kernel messages
    private LinkedList<KernelMessage> messageQueue = new LinkedList<>();
    private String name;
    //array to store devices (default -1 for empty)
    private int[] devicesArray = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
    private VirtualToPhysicalMapping[] memoryArray = new VirtualToPhysicalMapping[100];
    private int startPosition;
    private int size;
    private Scheduler scheduler;
    private FakeFileSystem fakeFileSystem;

    private priority processPrio = priority.interactive;

    //accessor for devices array
    public int[] getDevicesArray()
    {
        return devicesArray;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public int getStartPosition() {
        return startPosition;
    }



    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    //get mapping if not found in tlb
    void GetMapping(int virtualPageNumber) throws IOException, InterruptedException {
        //handle invalid memory access
        if(memoryArray[virtualPageNumber] == null)
        {
            System.out.println("ACCESSING MEMORY UNALLOCATED, KILLING CURRENT PROCESS");
            kernelandthread.stop();
            //OS.invalidMemoryAccess();
        }
        //update tlb
        if(memoryArray[virtualPageNumber].physicalPageNumber == -1)
        {
            for(int i = 0; i < memoryArray.length; i++)
            {
                if(memoryArray[i] != null)
                {
                    if (memoryArray[i].physicalPageNumber != -1)
                    {
                        UserlandProcess.updateTLB(virtualPageNumber,memoryArray[i].physicalPageNumber);
                        return;
                    }
                }
            }
            int hasMemory = -1;
            //get random process that has memory
            KernelandProcess rand = scheduler.getRandomProcess();
            while(hasMemory == -1)
            {
                hasMemory = rand.hasMemory();
                rand = scheduler.getRandomProcess();
            }
            //update physical page number
            memoryArray[virtualPageNumber].physicalPageNumber = hasMemory;

            //write memory to swap
            for(int i = 0; i < rand.memoryArray.length; i++)
            {
                if(memoryArray[i] != null)
                {
                    if(memoryArray[i].physicalPageNumber != -1)
                    {
                        byte[] tmp = new byte[1];
                        tmp[0] = UserlandProcess.memory[memoryArray[i].physicalPageNumber];
                        fakeFileSystem.Write(OS.swap+OS.pageNumber,tmp);
                        OS.pageNumber++;
                    }

                }

            }


        }
        //update tlb
        UserlandProcess.updateTLB(virtualPageNumber,memoryArray[virtualPageNumber].physicalPageNumber);
    }

    //check if a process has memory allocated
    public int hasMemory()
    {
        int hasMemory = -1;
        for(int i = 0; i < memoryArray.length; i++)
        {
            if(memoryArray[i] != null)
            {
                if (memoryArray[i].physicalPageNumber != -1)
                {
                    int toRet = memoryArray[i].physicalPageNumber;
                    memoryArray[i].physicalPageNumber = -1;
                    return toRet;
                }
            }
        }
        return hasMemory;
    }

    //set memory as in use
    void setMemoryUsage(int position)
    {
        memoryArray[position] = new VirtualToPhysicalMapping();
        memoryArray[position].physicalPageNumber =  physPageNum;
        physPageNum++;
    }

    //remove memory from use
    void removeMemoryUsage(int position)
    {

        if(memoryArray[position] != null && memoryArray[position].physicalPageNumber != -1)
        {
            memoryArray[position] =  null;
        }
    }



    KernelandProcess(UserlandProcess up,Scheduler scheduler)
    {
        kernelandthread = new Thread(up);
        pid = nextpid;
        nextpid += 1;
        name = up.getClass().getSimpleName();
        this.scheduler = scheduler;
    }

    //add a message to the queue
    public void addMessageQueue(KernelMessage km)
    {
        messageQueue.add(km);
    }

    public LinkedList<KernelMessage> getMessageQueue()
    {
        return messageQueue;
    }
    KernelandProcess(UserlandProcess up, priority prio,Scheduler scheduler)
    {
        kernelandthread = new Thread(up);
        pid = nextpid;
        nextpid += 1;
        processPrio = prio;
        name = up.getClass().getSimpleName();
        this.scheduler = scheduler;
    }

    //method to pause thread
    void stop()
    {
        if(threadStarted == true)
        {
            kernelandthread.suspend();
        }
    }

    //check if process is completed
    boolean isDone()
    {
        if(threadStarted && !kernelandthread.isAlive())
        {
            return true;
        }
        return false;
    }

    //code to actually run a thread
    void run()
    {

        //String printable = ("Running process at " + processPrio + " priority");
        //safePrintln(printable);
        //check if the thread has been started before to avoid thread state exception, then use appropriate method on the thread
        if (kernelandthread.getState() == Thread.State.NEW && threadStarted == false)
        {
            kernelandthread.start();
            //mark thread as started
            threadStarted = true;
        }
        else if(threadStarted == true)
        {
            kernelandthread.resume();
        }

    }

    //accessor to get pid
    public int getPid() {
        return pid;
    }

    //get name of process
    public String getName()
    {
        return name;
    }

    public priority getProcessPrio() {
        return processPrio;
    }

    public int getNumInterrrupt() {
        return numInterrrupt;
    }

    public void setNumInterrrupt(int numInterrrupt) {
        this.numInterrrupt = numInterrrupt;
    }

    public void setProcessPrio(priority processPrio) {
        this.processPrio = processPrio;
    }

    public void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }
}
