import java.io.IOException;
import java.util.Random;

public abstract class UserlandProcess implements Runnable{

    //tlb and memory
    public static tlbEntry[] tlb = new tlbEntry[]{new tlbEntry(0,0), new tlbEntry(0,0)};

    public static byte[] memory = new byte[1048576];

    byte Read(int address) throws IOException, InterruptedException {

        //check if tlb has correct value, if not update tlb and call read again (maximum 2 depth recursion)
        int pageNumber = address/1024;
        int pageOffset = address%1024;
        if(tlb[0].getVirtualAdd() != pageNumber && tlb[1].getVirtualAdd() != pageNumber)
        {
            OS.GetMapping(pageNumber);
            return Read(address);
        }
        //get correct physical address
        int physAdd;
        if(tlb[0].getVirtualAdd() == pageNumber)
        {
            physAdd = tlb[0].getPhysAdd();
        }
        else
        {
            physAdd = tlb[1].getPhysAdd();
        }
        //get byte from phys add
        physAdd = physAdd*1024+pageOffset;
        return memory[physAdd];
    }

    void Write(int address, byte value) throws IOException, InterruptedException {
        int pageNumber = address/1024;
        int pageOffset = address%1024;
        //check if tlb has correct value, if not update tlb and call read again (maximum 2 depth recursion)
        if(tlb[0].getVirtualAdd() != pageNumber && tlb[1].getVirtualAdd() != pageNumber)
        {
            OS.GetMapping(pageNumber);
            Write(address,value);
        }
        else
        {
            //get byte from phys add
            int physAdd;
            if(tlb[0].getVirtualAdd() == pageNumber)
            {
                physAdd = tlb[0].getPhysAdd();
            }
            else
            {
                physAdd = tlb[1].getPhysAdd();
            }
            //write to physical address
            physAdd = physAdd*1024+pageOffset;
            memory[physAdd] = value;
        }
    }

    static void updateTLB(int virtual, int physical)
    {
        //update tlb randomly
        Random rng = new Random();
        int val = rng.nextInt(1);
        tlb[val] = new tlbEntry(virtual,physical);
    }

    public static void clearTLB()
    {
        //clear tlb
        tlb[0] = new tlbEntry(0,0);
        tlb[1] = new tlbEntry(0,0);
    }


}
