package vite.rxbus;

import android.util.Log;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by trs on 16-11-14.
 */

class RxBusImpl implements RxBus.Bus {

    /**
     * 记录所有已注册的
     */
    private static final ConcurrentMap<MethodKey, Set<ObvBuilder>> MethodMap = new ConcurrentHashMap<>();

    /**
     * @param target 类实例
     */
    @Override

    public void register(Object target) {
        //先获取类中所有注解的方法和对应的key
        Log.v("RxBus register", "target:" + target.getClass().getName());
        MethodHelper.getMethodList(target, MethodMap);
        Iterator iter = MethodMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Set<ObvBuilder> sets = (Set<ObvBuilder>) entry.getValue();
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.create();
            }
        }
    }

    @Override
    public void unregister(Object target) {
        Log.v("RxBus", "unregister target:" + target.getClass().getName());
        final Class clazz = target.getClass();
        ArrayList<MethodKey> keyArray = MethodHelper.getMethodKeys(target);
        for (MethodKey key : keyArray) {
            Set<ObvBuilder> sets = MethodMap.get(key);
            Iterator iter = sets.iterator();
            while (iter.hasNext()) {
                ObvBuilder value = (ObvBuilder) iter.next();
                if (value.getClassEntity().equals(target)) {
                    value.destory();
                    sets.remove(value);
                }
            }
        }
    }

    @Override
    public void post(String tag) {
        String t = tag;
        if (t == null)
            t = "__default__";
        MethodKey k = new MethodKey(t, Void.TYPE);
        Set<ObvBuilder> sets = MethodMap.get(k);
        if (sets != null) {
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.post(null);
            }
        }
    }

    @Override
    public void post(String tag, Object value) {
        String t = tag;
        if (t == null)
            t = "__default__";
        Class clazz = value == null ? Void.TYPE : value.getClass();
        MethodKey k = new MethodKey(t, clazz);
        Set<ObvBuilder> sets = MethodMap.get(k);
        if (sets != null) {
            Iterator setIter = sets.iterator();
            while (setIter.hasNext()) {
                ObvBuilder obv = (ObvBuilder) setIter.next();
                obv.post(value);
            }
        }
    }

}
