package util;

import java.util.ArrayList;

public class ListMessage extends Message {
    private ArrayList<String> list;

    public ListMessage() {
        new ListMessage(null);
    }

    public ListMessage(ArrayList<String> list) {
        this.list = list;
    }

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }
}
