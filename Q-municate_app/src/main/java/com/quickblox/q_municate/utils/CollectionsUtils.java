package com.quickblox.q_municate.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionsUtils {

    public static <T> List<T> addItemsInBeginList(List<T> to, List<T> from){
        List<T> resultList = new ArrayList<>(to);
        Collections.reverse(resultList);
        List<T> fromList = new ArrayList<>(from);
        Collections.reverse(fromList);
        resultList.addAll(fromList);

        Collections.reverse(resultList);

        return resultList;
    }
}
