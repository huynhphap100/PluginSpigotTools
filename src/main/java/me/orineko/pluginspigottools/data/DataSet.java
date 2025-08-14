package me.orineko.pluginspigottools.data;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public abstract class DataSet<T> {

    public final Set<T> dataSet;

    public DataSet(){
        dataSet = Collections.synchronizedSet(new HashSet<>());
    }

    public T addData(@NonNull T newData, T getData){
        if(getData != null) return getData;
        synchronized(dataSet) {
            dataSet.add(newData);
        }
        return newData;
    }

    @Nullable
    public T getData(@NonNull GetData<T> getData){
        synchronized(dataSet) {
            for (T data : dataSet) {
                if (getData.getData(data)) {
                    return data;
                }
            }
        }
        return null;
    }

    public void removeData(@NonNull T data){
        synchronized(dataSet) {
            dataSet.remove(data);
        }
    }

    public interface GetData<T> {
        boolean getData(@NonNull T data);
    }

    public interface AddData<T> {
        T addData();
    }
}
