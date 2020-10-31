package com.ahery.main;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadThread extends Thread {
    private String fileOutPath="";
    private String fileName;
    private ConcurrentHashMap<String,Integer> urls;
    private ConcurrentHashMap<Integer,String> keyFileMap;
    private int totalTasks;
    private DecimalFormat df;

    public DownloadThread(ConcurrentHashMap<String ,Integer> urls, ConcurrentHashMap<Integer,String> keyFileMap,int totalTasks,String fileName){
        this.urls=urls;
        this.keyFileMap=keyFileMap;
        this.totalTasks=totalTasks;
        this.fileName=fileName;
        this.df=new DecimalFormat("0.00");
    }

    @Override
    public void run() {
        Set<Map.Entry<String,Integer>> sets=urls.entrySet();
        for(Map.Entry<String,Integer> entry:sets){
            String tempUrl=Res.M3U8BASEURL+entry.getKey();
            int index=entry.getValue();
            fileOutPath=Res.FOLD_PATH+File.separator+fileName+File.separator+index+".ts";
            try{
                downloadNet(tempUrl,fileOutPath);
                keyFileMap.put(index, fileOutPath);
                urls.remove(entry.getKey());
                System.out.println(fileName+"片段"+index+":success[进度："+(df.format((double)keyFileMap.size()/totalTasks*100))+"%]");
            }catch (Exception e){
                System.err.println(fileName+"片段"+index+":超时重连");
                run();
            }
        }
    }


    private void downloadNet(String urlPath, String fileOutPath) throws IOException{
        // 下载网络文件
        int byteread = 0;
        URL url = new URL(urlPath);
        URLConnection conn= url.openConnection();
        conn.setConnectTimeout(Res.CONNECT_TIMEOUT);
        conn.setReadTimeout(Res.CONNECT_TIMEOUT);
        BufferedInputStream bis=null;
        BufferedOutputStream bos=null;
        try {
            bis=new BufferedInputStream(conn.getInputStream());
            bos=new BufferedOutputStream(new FileOutputStream(fileOutPath));
            byte[] buffer = new byte[1024];
            while ((byteread = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, byteread);
            }
            bos.flush();
            bos.close();
            bis.close();
        }catch (IOException e){
            if(bos!=null)
                bos.close();
            if(bis!=null)
                bis.close();
        }
    }


}
