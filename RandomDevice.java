import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
public class RandomDevice implements Device{
    //array of randoms
    Random[] randomArray = new Random[10];
    public int Open(String s)
    {
        //find first empty spot in array and populate it with random
        Random arrayDevices = new Random();
        int i = 0;
        while(randomArray[i] != null){
            i++;
        }
        if(i >= randomArray.length)
        {
            return  -1;
        }
        else
        {
            randomArray[i] = arrayDevices;
        }
        //make sure we have a number to set the seed
        if(!s.equals("") || s != null) {
            randomArray[i].setSeed(Integer.valueOf(s));
            //return id
            return i;
        }
        return -1;

    }

    @Override
    //get rid of device
    public void Close(int id) {
        randomArray[id] = null;
    }

    //reads some random bytes
    @Override
    public byte[] Read(int id, int size) {
        byte[] retArray = new byte[size];
        randomArray[id].nextBytes(retArray);
        return retArray;
    }

    @Override
    //also reads some random bytes, just doesnt return them
    public void Seek(int id, int to) {
        byte[] retArray = new byte[to];
        randomArray[id].nextBytes(retArray);
    }

    @Override
    //useless function
    public int Write(int id, byte[] data) {
        return 0;
    }

}
