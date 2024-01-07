import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device{
    //array to hold random access files
    RandomAccessFile[] rafArray = new RandomAccessFile[10];

    //open file
    @Override
    public int Open(String s) throws FileNotFoundException {
        try
        {
            //open for reading a writing
            RandomAccessFile raf = new RandomAccessFile(s,"rwd");
            //find first empty spot in array
            int i = 0;
            while(rafArray[i] != null){
                i++;
            }
            if(i >= rafArray.length)
            {
                return  -1;
            }
            else
            {
                rafArray[i] = raf;
            }
            //return file id
            return i;
        }
        catch (FileNotFoundException e)
        {
            return -1;
        }

    }

    @Override
    //close file and set entry to null
    public void Close(int id) throws IOException {
        rafArray[id].close();
        rafArray[id] = null;
    }

    //read from an opened random access file
    @Override
    public byte[] Read(int id, int size) throws IOException {
        byte[] retArray = new byte[size];
        rafArray[id].read(retArray);
        return retArray;
    }

    //unused seek function
    @Override
    public void Seek(int id, int to) throws IOException {
        rafArray[id].seek(to);
    }

    //write to opened random access file
    @Override
    public int Write(int id, byte[] data) throws IOException {
        rafArray[id].write(data);
        return data.length;
    }
}
