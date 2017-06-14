package blue.happening.service.adapter;

/**
 * Created by fabi on 14.06.17.
 */

public class MeshDevice {

    String deviceid = "";
    String devicename = "";

    public MeshDevice(String deviceid, String devicename) {
        this.deviceid = deviceid;
        this.devicename = devicename;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public String getDevicename() {
        return devicename;
    }

    @Override
    public boolean equals(Object obj) {
        MeshDevice other = (MeshDevice) obj;
        return deviceid.equals(other.deviceid);
    }
}
