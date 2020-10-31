package com.ahery.main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImgThread extends Thread {
    private String imgUrl;
    private String savePath;

    public DownloadImgThread(String imgUrl,String savePath) throws IOException {
        this.imgUrl=imgUrl;
        this.savePath=savePath;
        File imgFile=new File(savePath);
        if(!imgFile.exists()) imgFile.createNewFile();
    }

    @Override
    public void run() {
        try {
            download();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void download() throws IOException{
        HttpURLConnection conn= (HttpURLConnection) new URL(imgUrl).openConnection();
        conn.setConnectTimeout(5000);
        BufferedInputStream bis=new BufferedInputStream(conn.getInputStream());
        BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(savePath));
        byte[] bufferRead=new byte[1024];
        int bytes=0;
        while ((bytes=bis.read(bufferRead))!=-1)
            bos.write(bufferRead,0,bytes);
        bos.flush();
        bis.close();
        bos.close();
    }


}
