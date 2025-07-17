package me.orineko.pluginspigottools;

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
        dataSet.add(newData);
        return newData;
    }

    @Nullable
    public T getData(@NonNull GetData<T> getData){
        return dataSet.stream().filter(getData::getData).findAny().orElse(null);
    }

    public void removeData(@NonNull T data){
        dataSet.remove(data);
    }

    public interface GetData<T> {
        boolean getData(@NonNull T data);
    }

    public interface AddData<T> {
        T addData();
    }
}
