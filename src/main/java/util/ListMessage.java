package util;

import java.util.ArrayList;

public class ListMessage extends Message {
    private ArrayList<String> list;
    private String user;

    public ListMessage(String user) {
        new ListMessage(user,null);
    }

    public ListMessage(String user, ArrayList<String> list) {
        this.list = list;
        this.user = user;
    }

    public ListMessage(ArrayList<String> list) {
        this.list = list;
    }

    public ArrayList<String> getList() {
        return list;
    }

    public String getUser() {
        return user;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }
}
