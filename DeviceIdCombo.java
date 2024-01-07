//class to map device ids to corresponding device
public class DeviceIdCombo {
    private int id;
    private Device device;

    public DeviceIdCombo(int argID, Device argDevice)
    {
        id = argID;
        device = argDevice;
    }

    public int getId() {
        return id;
    }

    public Device getDevice() {
        return device;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
