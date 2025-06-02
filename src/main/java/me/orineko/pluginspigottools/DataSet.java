package me.orineko.pluginspigottools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class DataSet<T> {

    public final Set<T> dataSet;

    public DataSet(){
        dataSet = Collections.synchronizedSet(new HashSet<>());
    }

    public T addData(@Nonnull T newData, T getData){
        if(getData != null) return getData;
        dataSet.add(newData);
        return newData;
    }

    @Nullable
    public T getData(@Nonnull GetData<T> getData){
        return dataSet.stream().filter(getData::getData).findAny().orElse(null);
    }

    public void removeData(@Nonnull T data){
        dataSet.remove(data);
    }

    public Set<T> getDataSet() {
        return dataSet;
    }

    public interface GetData<T> {
        boolean getData(@Nonnull T data);
    }

    public interface AddData<T> {
        T addData();
    }
}
