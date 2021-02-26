package uk.ac.kcl.sufcwmillionapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import uk.ac.kcl.sufcwmillionapplication.bean.SearchBean;

public final class SPUtils {

    private final static String name = "SEARCHHISTORY";
    private final static String history_name = "history";
    private final static int mode = Context.MODE_PRIVATE;

    /**
     * 保存首选项
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }


    /**
     * 获取首选项
     *
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getBoolean(key, defValue);
    }

    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getInt(key, defValue);
    }

    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(name, mode);
        return sp.getString(key, defValue);
    }

    /**
     * desc:将数组转为16进制
     *
     * @param bArray
     * @return modified:
     */
    private static String bytesToHexString(byte[] bArray) {
        if (bArray == null) {
            return null;
        }
        if (bArray.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * desc:保存对象
     *
     * @param context
     * @param key
     * @param obj     要保存的对象，只能保存实现了serializable的对象 modified:
     */
    public static void saveObject(Context context, String key, Object obj) {
        try {
            // 保存对象
            SharedPreferences.Editor sharedata = context.getSharedPreferences("user", Context.MODE_PRIVATE).edit();
            // 先将序列化结果写到byte缓存中，其实就分配一个内存空间
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            // 将对象序列化写入byte缓存
            os.writeObject(obj);
            // 将序列化的数据转为16进制保存
            String bytesToHexString = bytesToHexString(bos.toByteArray());
            // 保存该16进制数组
            sharedata.putString(key, bytesToHexString);
            sharedata.commit();
        } catch (IOException e) {
            e.printStackTrace();
            //Log.e("", "保存obj失败");
        }
    }

    /**
     * desc:将16进制的数据转为数组
     *
     * @param data
     * @return modified:
     */
    private static byte[] StringToBytes(String data) {
        String hexString = data.toUpperCase().trim();
        if (hexString.length() % 2 != 0) {
            return null;
        }
        byte[] retData = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i++) {
            int int_ch; // 两位16进制数转化后的10进制数
            char hex_char1 = hexString.charAt(i); // 两位16进制数中的第一位(高位*16)
            int int_ch1;
            if (hex_char1 >= '0' && hex_char1 <= '9')
                int_ch1 = (hex_char1 - 48) * 16; // // 0 的Ascll - 48
            else if (hex_char1 >= 'A' && hex_char1 <= 'F')
                int_ch1 = (hex_char1 - 55) * 16; // // A 的Ascll - 65
            else
                return null;
            i++;
            char hex_char2 = hexString.charAt(i); // /两位16进制数中的第二位(低位)
            int int_ch2;
            if (hex_char2 >= '0' && hex_char2 <= '9')
                int_ch2 = (hex_char2 - 48); // // 0 的Ascll - 48
            else if (hex_char2 >= 'A' && hex_char2 <= 'F')
                int_ch2 = hex_char2 - 55; // // A 的Ascll - 65
            else
                return null;
            int_ch = int_ch1 + int_ch2;
            retData[i / 2] = (byte) int_ch;// 将转化后的数放入Byte里
        }
        return retData;
    }

    /**
     * desc:获取保存的Object对象
     *
     * @param context
     * @param key
     * @return modified:
     */
    public static Object getObject(Context context, String key) {
        try {
            SharedPreferences sharedata = context.getSharedPreferences("user", Context.MODE_PRIVATE);
            if (sharedata.contains(key)) {
                String string = sharedata.getString(key, "");
                if (TextUtils.isEmpty(string)) {
                    return null;
                } else {
                    // 将16进制的数据转为数组，准备反序列化
                    byte[] stringToBytes = StringToBytes(string);
                    ByteArrayInputStream bis = new ByteArrayInputStream(stringToBytes);
                    ObjectInputStream is = new ObjectInputStream(bis);
                    // 返回反序列化得到的对象
                    Object readObject = is.readObject();
                    return readObject;
                }
            }
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // 所有异常返回null
        return null;
    }

    public static void saveHistory(Context context,List<SearchBean> searchBeans,SearchBean searchBean){
        if (searchBeans.size()>=5){
            searchBeans.remove(searchBeans.size()-1);
        }
        searchBeans.add(0,searchBean);
        saveHistories(context,searchBeans);
    }

    public static void saveHistories(Context context,List<SearchBean> searchBeans){
        Gson gson = new Gson();
        String data = gson.toJson(searchBeans);
        saveString(context,history_name, data);
    }

    public static ArrayList<SearchBean> getHistories(Context context) {
        String data = getString(context, history_name, "");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<SearchBean>>() {
        }.getType();
        ArrayList<SearchBean> list = gson.fromJson(data, listType);

        if (list == null){
            return new ArrayList<>();
        }
        return list;
    }
}