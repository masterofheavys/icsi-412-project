import java.io.IOException;
import java.util.*;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;


public class Scheduler {
    Random rng = new Random();
    Clock clock = Clock.systemUTC();


    List<KernelandProcess> backProcessesList =  Collections.synchronizedList(new LinkedList<KernelandProcess>());
    List<KernelandProcess> interactiveProcessesList =  Collections.synchronizedList(new LinkedList<KernelandProcess>());
    List<KernelandProcess> realTimeProcessesList = Collections.synchronizedList(new LinkedList<KernelandProcess>());

    Timer schedulerTimer = new Timer();
    KernelandProcess schedulerKernelandProcess = null;
    ConcurrentHashMap<Long, KernelandProcess> wakeupMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Integer> nameToPID = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer,KernelandProcess> PIDtoProcess = new ConcurrentHashMap<>();

    ConcurrentHashMap<Integer,KernelandProcess> waitingForMessage = new ConcurrentHashMap<>();

    Kernel schedulerKernel;

    List<Long> wakeupTimes = Collections.synchronizedList(new LinkedList<Long>());


    public Scheduler()
    {
        //schedules hardware interrupt
        schedulerTimer.schedule(new interrupt(),250,250);
    }

    //creat process defaults to interative priority
    public int createProcess(UserlandProcess up, Kernel passing,Scheduler scheduler) throws InterruptedException, IOException {
        schedulerKernel = passing;
        KernelandProcess temp = new KernelandProcess(up,scheduler);
        interactiveProcessesList.add(temp);
        //if no current running process, call switchProcess to get the switchProcess cycle started
        if (schedulerKernelandProcess == null)
        {
            switchProcess();
        }
        //add name -> PID and PID -> process for message handling
        nameToPID.put(temp.getName(),temp.getPid());
        PIDtoProcess.put(temp.getPid(),temp);
        return temp.getPid();
    }



    public int createProcess(UserlandProcess up,priority prio, Kernel passing,Scheduler scheduler) throws InterruptedException, IOException {
        schedulerKernel = passing;
        KernelandProcess temp = new KernelandProcess(up,prio,scheduler);
        //add to approproiate queue
        if(prio == priority.interactive)
        {
            interactiveProcessesList.add(temp);
        }
        else if(prio == priority.realTime)
        {
            realTimeProcessesList.add(temp);
        }
        else
        {
            backProcessesList.add(temp);
        }

        //if no current running process, call switchProcess to get the switchProcess cycle started
        if (schedulerKernelandProcess == null)
        {
            switchProcess();
        }
        //add name -> PID and PID -> process for message handling
        PIDtoProcess.put(temp.getPid(),temp);
        return temp.getPid();
    }


    public void invalidMemoryAccess() throws IOException, InterruptedException {
        if(schedulerKernelandProcess.getProcessPrio() == priority.interactive)
        {
            interactiveProcessesList.remove(schedulerKernelandProcess);
        }
        else if (schedulerKernelandProcess.getProcessPrio() == priority.background)
        {
            backProcessesList.remove(schedulerKernelandProcess);
        }
        else
        {
            realTimeProcessesList.remove(schedulerKernelandProcess);
        }
        wakeupMap.remove(schedulerKernelandProcess);
        waitingForMessage.remove(schedulerKernelandProcess);
        KernelandProcess tmp =schedulerKernelandProcess;
        tmp.stop();
        schedulerKernelandProcess = null;
        switchProcess();
    }
    public void setStartposition(int startposition)
    {
        schedulerKernelandProcess.setStartPosition(startposition);
    }
    public void setSize(int size)
    {
        schedulerKernelandProcess.setSize(size);
    }
    public void switchProcess() throws InterruptedException, IOException {


        //make sure something is running
        if(schedulerKernelandProcess != null)
        {
            //update tlb on task switch
            UserlandProcess.clearTLB();

            KernelandProcess tmp =schedulerKernelandProcess;
            tmp.stop();

            //add it to end of corresponding list list, remove from initial position

            if(tmp.isDone() == false)
            {
                if(tmp.getProcessPrio() == priority.interactive)
                {

                    interactiveProcessesList.add(tmp);
                    interactiveProcessesList.remove(0);
                }
                else if(tmp.getProcessPrio() == priority.realTime)
                {
                    realTimeProcessesList.add(tmp);
                    realTimeProcessesList.remove(0);
                }
                else
                {

                    backProcessesList.add(tmp);
                    backProcessesList.remove(0);
                }


            }
            else
            {
                //free all memory of process on end
                schedulerKernel.freeMemory(schedulerKernelandProcess.getStartPosition(),schedulerKernelandProcess.getSize());
                //remove hashMapping
                PIDtoProcess.remove(tmp.getPid());
                nameToPID.remove(tmp.getName());
                //code to close all open devices from a finished process
                for(int i = 0; i < schedulerKernelandProcess.getDevicesArray().length; i++)
                {
                    if(schedulerKernelandProcess.getDevicesArray()[i] != -1)
                    {
                        schedulerKernel.Close(schedulerKernelandProcess.getDevicesArray()[i]);
                        System.out.println("Closing");
                    }
                }

            }
        }
        //if there are sleeping processes, go through sorted list of times until either list is empty or they're no longer supposed to wake up
        while(wakeupTimes.size() > 0 && Long.valueOf(wakeupTimes.get(0)) < clock.millis())
        {
            //add to corresponding list
            priority prio = wakeupMap.get(wakeupTimes.get(0)).getProcessPrio();
            if(prio == priority.interactive)
            {
                interactiveProcessesList.add(wakeupMap.get(wakeupTimes.get(0)));
            }
            else if(prio == priority.realTime)
            {
                realTimeProcessesList.add(wakeupMap.get(wakeupTimes.get(0)));
            }
            else
            {
                backProcessesList.add(wakeupMap.get(wakeupTimes.get(0)));
            }
            wakeupTimes.remove(0);

        }
        //probability model
        if(realTimeProcessesList.size() == 0)
        {
            if(interactiveProcessesList.size() == 0)
            {
                //if there are only background processes
                schedulerKernelandProcess = backProcessesList.get(0);
                schedulerKernelandProcess.run();
            }
            else
            {
                //if there are only integrative processes
                if(backProcessesList.size() == 0)
                {
                    schedulerKernelandProcess = interactiveProcessesList.get(0);
                    schedulerKernelandProcess.run();
                }
                //if there are no real time but there are interative and background
                else
                {
                    //one fourth chance for background 3/4 chance of interative
                    int chance = rng.nextInt(4);
                    if(chance == 0)
                    {
                        schedulerKernelandProcess = backProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                    else
                    {
                        schedulerKernelandProcess = interactiveProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                }
            }
        }
        else
        {
            // there are real time processes
            if(interactiveProcessesList.size() == 0)
            {
                if(backProcessesList.size() == 0)
                {
                    //there are only real time processes
                    schedulerKernelandProcess = realTimeProcessesList.get(0);
                    schedulerKernelandProcess.run();
                }
                else
                {
                    //there are real time and background, 1/10th background otherwise real time
                    int chance = rng.nextInt(10);
                    if(chance != 9)
                    {
                        schedulerKernelandProcess = realTimeProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                    else
                    {
                        schedulerKernelandProcess = backProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                }
            }
            else
            {
                if(backProcessesList.size() == 0)
                {
                    //there are only real time and interactive
                    int chance = rng.nextInt(10);
                    if(chance <= 6)
                    {
                        schedulerKernelandProcess = realTimeProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                    else
                    {
                        schedulerKernelandProcess = interactiveProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                }
                else
                {
                    //all queues have processes
                    int chance = rng.nextInt(10);
                    if(chance <= 5)
                    {
                        schedulerKernelandProcess = realTimeProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                    else if(chance == 6)
                    {
                        schedulerKernelandProcess = backProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                    else
                    {
                        schedulerKernelandProcess = interactiveProcessesList.get(0);
                        schedulerKernelandProcess.run();
                    }
                }
            }
        }

    }

    public synchronized void sleep(int milliseconds) throws InterruptedException, IOException {
        //get current time
        long wakeupTime = clock.millis() + milliseconds;
        //add to queue
        wakeupTimes.add(new Long(wakeupTime));
        //add process to hashmap
        wakeupMap.put(new Long(wakeupTime),schedulerKernelandProcess);
        //sort hashmap
        wakeupTimes.sort(null);

        //reset interrupt number
        schedulerKernelandProcess.setNumInterrrupt(0);

        //stop current process
        var tmp = schedulerKernelandProcess;
        schedulerKernelandProcess = null;
        tmp.stop();

        switchProcess();
    }


    //gets current process
    public KernelandProcess getSchedulerKernelandProcess() {
        return schedulerKernelandProcess;
    }

    public void waitForMessage() throws IOException, InterruptedException
    {
        //put message in wait for process queue
        waitingForMessage.put(schedulerKernelandProcess.getPid(),schedulerKernelandProcess);
        //stop current process
        var tmp = schedulerKernelandProcess;
        tmp.stop();
        schedulerKernelandProcess = null;
        //remove from appropriate queue
        if(tmp.getProcessPrio() == priority.interactive)
        {
            if(interactiveProcessesList.size() != 0)
            {
                interactiveProcessesList.remove(0);
            }
        }
        else if(tmp.getProcessPrio() == priority.realTime)
        {
            if(realTimeProcessesList.size() != 0)
            {
                realTimeProcessesList.remove(0);
            }
        }
        else
        {
            if(backProcessesList.size() != 0)
            {
                backProcessesList.remove(0);
            }
        }

        //switch process
        switchProcess();
    }

    public void setMemoryUsage(int position)
    {
        schedulerKernelandProcess.setMemoryUsage(position);
    }
    void removeMemoryUsage(int position)
    {
        schedulerKernelandProcess.removeMemoryUsage(position);
    }

    public KernelandProcess getRandomProcess()
    {
        //2d list to see which lists are populated
        List<List<KernelandProcess>> populatedList = Collections.synchronizedList(new LinkedList<List<KernelandProcess>>());
        if(backProcessesList.size()!= 0)
        {
            populatedList.add(backProcessesList);
        }
        if (interactiveProcessesList.size() != 0)
        {
            populatedList.add(interactiveProcessesList);
        }
        if(realTimeProcessesList.size() != 0)
        {
            populatedList.add(realTimeProcessesList);
        }

        //get random list
        Random rng = new Random();
        int randomList = rng.nextInt(populatedList.size());

        //get random process from random list
        return populatedList.get(randomList).get(rng.nextInt(populatedList.get(randomList).size()));
    }

    public void restoreWaitingQueue(int PID) throws IOException, InterruptedException {
        //get process from PID from waiting queue
        KernelandProcess tmp = waitingForMessage.get(PID);
        if(tmp != null)
        {
            //remove from queue, add to appropriate priority list
            waitingForMessage.remove(PID);
            if(tmp.getProcessPrio() == priority.interactive)
            {
                interactiveProcessesList.add(tmp);
            } else if (tmp.getProcessPrio() == priority.realTime)
            {
                realTimeProcessesList.add(tmp);
            }
            else
            {
                backProcessesList.add(tmp);
            }
            //switch process
            var tmpKLP = schedulerKernelandProcess;
            tmpKLP.stop();
            schedulerKernelandProcess = null;
            //interactiveProcessesList.remove(0);
            switchProcess();
        }
    }
    //gets process corresponding to PID from the hashmap
    public synchronized KernelandProcess processFromPID(int pid)
    {
        return PIDtoProcess.get(pid);
    }

    void GetMapping(int virtualPageNumber) throws IOException, InterruptedException {
        schedulerKernelandProcess.GetMapping(virtualPageNumber);
    }

    //gets a processes message queue
    public synchronized LinkedList<KernelMessage> getMessageQueue()
    {
        return schedulerKernelandProcess.getMessageQueue();
    }
    //get the current processes pid
    public synchronized int getPID()
    {
        return schedulerKernelandProcess.getPid();
    }
    //get the PID of a processes by its name
    public synchronized int getPIDByName(String name)
    {
        return nameToPID.get(name);
    }
    private class interrupt extends TimerTask
    {
        @Override
        public void run() {
            if(schedulerKernelandProcess != null)
            {
                //if hard interrupted 5 times demote
                if(schedulerKernelandProcess.getNumInterrrupt() == 5)
                {
                    //demote priority, remove from current queue and add to first place of new priority queue
                    if(schedulerKernelandProcess.getProcessPrio() == priority.realTime)
                    {
                        schedulerKernelandProcess.setProcessPrio(priority.interactive);
                        schedulerKernelandProcess.setNumInterrrupt(0);
                        realTimeProcessesList.remove(0);
                        interactiveProcessesList.add(0,schedulerKernelandProcess);
                    }
                    else if(schedulerKernelandProcess.getProcessPrio() == priority.interactive)
                    {
                        schedulerKernelandProcess.setProcessPrio(priority.background);
                        schedulerKernelandProcess.setNumInterrrupt(0);
                        interactiveProcessesList.remove(0);
                        backProcessesList.add(0,schedulerKernelandProcess);
                    }
                }
                else
                {
                    schedulerKernelandProcess.setNumInterrrupt(schedulerKernelandProcess.getNumInterrrupt()+1);
                }
            }
            try {
                switchProcess();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
