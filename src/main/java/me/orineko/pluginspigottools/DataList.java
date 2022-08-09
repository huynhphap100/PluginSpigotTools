package me.orineko.pluginspigottools;

import java.util.ArrayList;
import java.util.List;

public abstract class DataList<T> {

    private final List<T> dataList;

    public DataList(){
        dataList = new ArrayList<>();
    }

    public T addData(T data){
        dataList.add(data);
        return data;
    }

    public void removeData(T data){
        dataList.remove(data);
    }

    public List<T> getDataList() {
        return dataList;
    }
}
