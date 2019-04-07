package com.example.newbataan.nbBlueTooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by dcst on 3/5/15.
 */

public class StreamUtils {
    public static byte[] toByteArray(Object obj){
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            bos.close();

            bytes = bos.toByteArray();

        }catch (IOException e){

        }
        return bytes;
    }

    public static Object toObject(byte[] bytes){
        Object obj = null;
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        }catch (IOException e){

        }catch (ClassNotFoundException ex){

        }
        return obj;
    }
}