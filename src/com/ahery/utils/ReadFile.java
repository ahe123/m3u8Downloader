package com.ahery.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadFile {

    public String filePath="";

    public ReadFile(String filePath){
        this.filePath=filePath;
    }

    public String readToString(){
        if(filePath==null) return null;
        String content="";
        try {
            BufferedReader br=new BufferedReader(new FileReader(filePath));
            String line;
            while ((line=br.readLine())!=null)
                content+=line+"\r\n";
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return content;
    }

    public ArrayList<String> readToList(){
        if(filePath==null) return null;
        ArrayList<String> list=new ArrayList<>();
        try {
            BufferedReader br=new BufferedReader(new FileReader(filePath));
            String line;
            while ((line=br.readLine())!=null){
                if(line.equals("null"))
                    continue;
                list.add(line);
            }
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return list;
    }



}
