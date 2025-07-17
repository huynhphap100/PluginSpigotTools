package me.orineko.pluginspigottools;

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class DataList<T> {

    public final List<T> dataList;

    public DataList(){
        dataList = Collections.synchronizedList(new ArrayList<>());
    }

    public T addData(@Nonnull T newData, T getData){
        if(getData != null) return getData;
        dataList.add(newData);
        return newData;
    }

    @Nullable
    public T getData(@Nonnull GetData<T> getData){
        return dataList.stream().filter(getData::getData).findAny().orElse(null);
    }

    public void removeData(@Nonnull T data){
        dataList.remove(data);
    }

    public interface GetData<T> {
        boolean getData(@Nonnull T data);
    }

    public interface AddData<T> {
        T addData();
    }
}
