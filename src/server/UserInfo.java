package server;

import java.io.DataOutputStream;
import java.io.Serializable;

//将用户和他对应的DOS抽象
public class UserInfo implements Serializable {
    private String username;
    private DataOutputStream dataOutputStream;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }
}
