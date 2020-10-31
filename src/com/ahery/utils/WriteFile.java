package com.ahery.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WriteFile {
    private String filePath;

    public WriteFile(String filePath){
        this.filePath=filePath;
        try {
            File file=new File(filePath);
            if(!file.exists())
                file.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * @param type 写方式 true追加 false覆盖
     * @return
     */
    public synchronized boolean write(String content,boolean type){
        if(filePath==null||content==null)
            return false;
        FileWriter fw=null;
        BufferedWriter bw=null;
        try {
            fw=new FileWriter(filePath,type);
            bw=new BufferedWriter(fw);
            bw.write(content);
//            String[] strings=content.split("\r\n");
////            for(String item:strings){
////                bw.write(item);
////                bw.newLine();
////            }
            bw.flush();
            bw.close();
            fw.flush();
            fw.close();
            return true;
        }catch (Exception e){
            return false;
        }
    }


    /**
     * @param type 写方式 true追加 false覆盖
     * @return
     */
    public boolean write(ArrayList<String> contentList,boolean type){
        if(filePath==null||contentList==null)
            return false;
        FileWriter fw=null;
        BufferedWriter bw=null;
        try {
            fw=new FileWriter(filePath,type);
            bw=new BufferedWriter(fw);
            for(String item:contentList){
                bw.write(item);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fw.close();
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
