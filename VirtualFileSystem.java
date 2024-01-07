import java.io.FileNotFoundException;
import java.io.IOException;

public class VirtualFileSystem implements Device{
    //devices for the file system
    RandomDevice vfsRandom = new RandomDevice();
    FakeFileSystem vfsFSS = new FakeFileSystem();

    //map id to devices
    DeviceIdCombo[] pairArray = new DeviceIdCombo[10];
    @Override
    public int Open(String s) throws FileNotFoundException {
        try{
            //see if its a random device or not
            s = s.toLowerCase();
            if(s.contains("random "))
            {
                //get the number from the string to use as seed
                s = s.substring(7,s.length()-1);

                //find first empty index
                int i = 0;
                while(pairArray[i] != null){
                    i++;
                }
                if(i >= pairArray.length)
                {
                    return  -1;
                }
                //open random object
                int tmp = vfsRandom.Open(s);
                //store
                pairArray[i] = new DeviceIdCombo(tmp,vfsRandom);
                //return index
                return tmp;

            }
            else
            {
                s = s.substring(5,s.length());
                //find first empty index in array
                int i = 0;
                while(pairArray[i] != null){
                    i++;
                }
                if(i >= pairArray.length)
                {
                    return  -1;
                }
                //open fake file system
                int tmp = vfsFSS.Open(s);
                //store pair
                pairArray[i] = new DeviceIdCombo(tmp,vfsFSS);
                //return id
                return tmp;
            }
        }
        catch (FileNotFoundException e)
        {
            return -1;
        }

    }

    @Override
    public void Close(int id) throws IOException {
        //determine what type of device and call appropriate close function
        if(pairArray[id].getDevice() instanceof RandomDevice)
        {
            ((RandomDevice)pairArray[id].getDevice()).Close(pairArray[id].getId());
        }
        else
        {
            ((FakeFileSystem)pairArray[id].getDevice()).Close(pairArray[id].getId());
        }
        //remove pairing from array
        pairArray[id] = null;
    }

    @Override
    //determine what type of device and call appropriate read
    public byte[] Read(int id, int size) throws IOException {
        if(pairArray[id].getDevice() instanceof RandomDevice)
        {
            return ((RandomDevice)pairArray[id].getDevice()).Read(pairArray[id].getId(),size);
        }
        else
        {
            return ((FakeFileSystem)pairArray[id].getDevice()).Read(pairArray[id].getId(),size);
        }
    }

    @Override
    //determine what type of device and call appropriate seek
    public void Seek(int id, int to) throws IOException {
        if(pairArray[id].getDevice() instanceof RandomDevice)
        {
            ((RandomDevice)pairArray[id].getDevice()).Seek(pairArray[id].getId(),to);
        }
        else
        {
            ((FakeFileSystem)pairArray[id].getDevice()).Seek(pairArray[id].getId(),to);
        }
    }

    @Override
    //determine what type of device and call appropriate write
    public int Write(int id, byte[] data) throws IOException {
        if(pairArray[id].getDevice() instanceof RandomDevice)
        {
            return ((RandomDevice)pairArray[id].getDevice()).Write(pairArray[id].getId(),data);
        }
        else
        {
            return ((FakeFileSystem)pairArray[id].getDevice()).Write(pairArray[id].getId(),data);
        }
    }
}
