package com.github.ulwx.aka.dbutils.springboot.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class AkaRedisSelector {
    private final static ThreadLocal<Stack<String>> LOCAL_DS =
            new ThreadLocal<Stack<String>>(){
                @Override
                protected Stack<String> initialValue() {
                    return new Stack<>();
                }
            };

    private final static ThreadLocal<Map<String,Object>> PARAMATERS_MAP =
            new ThreadLocal<Map<String,Object>>(){
                @Override
                protected Map<String,Object> initialValue() {
                    return new HashMap<String,Object>();
                }
            };

    public static void push(String name) {
        LOCAL_DS.get().push(name);
    }


    public static String  get() {
        return LOCAL_DS.get().size()==0?null:LOCAL_DS.get().peek();
    }

    public static String pop(String dsName) {
        if(LOCAL_DS.get().size()>0) {
            String pop= LOCAL_DS.get().pop();
            if(dsName.equals(pop)){
                return pop;
            }else{
                throw new RuntimeException("内存状态不一致！dsName="+dsName+",pop="+pop);
            }
        }
        return null;
    }

}
