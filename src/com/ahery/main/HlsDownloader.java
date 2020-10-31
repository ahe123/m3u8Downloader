package com.ahery.main;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HlsDownloader {
    private String uuid;
    private String fileName;

    public HlsDownloader(String fileName) throws IOException {
//        this.uuid = UUID.randomUUID().toString().replaceAll("-","");
//        this.preUrlPath = Res.M3U8URL.substring(0, Res.M3U8URL.lastIndexOf("/")+1);
        this.fileName = fileName;
        File file = new File(Res.FOLD_PATH+File.separator+fileName);
        if(!file.exists()) file.mkdirs();
        file=new File(Res.FOLD_PATH+File.separator+fileName+File.separator+fileName+".mp4");
        if(!file.exists()) file.createNewFile();
    }

    public String download() throws Exception {
        List<String> urlList = getIndexFile(Res.M3U8URL);
        ConcurrentHashMap<Integer,String> keyFileMap = new ConcurrentHashMap<>();
        //分配线程数与子任务
        ArrayList<ConcurrentHashMap<String,Integer>> subUrlList=new ArrayList<>();
        int threads=urlList.size()<Res.MAX_THREADS?urlList.size():Res.MAX_THREADS;
        for(int i=0;i<threads;i++)
            subUrlList.add(new ConcurrentHashMap<String ,Integer>());
        int subIndex=0;
        for(int i=0;i<urlList.size();i++){
            subUrlList.get(subIndex++).put(urlList.get(i),i);
            subIndex=subIndex>=threads?0:subIndex;
        }
        //下载
        for(int i=0; i<subUrlList.size();i++)
            new DownloadThread(subUrlList.get(i), keyFileMap,urlList.size(),fileName).start();
        while (keyFileMap.size()<urlList.size()){
            //当前视频片段没有全部完成下载，需要等待
            Thread.sleep(Res.WAIT_DOWNLOAD_TIME);
        }
        return composeFile(keyFileMap);
    }

    /* 下载并解析索引文件 */
    public List<String> getIndexFile(String urlPath) throws Exception{
        URL url = new URL(urlPath);
        //下载资源
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
        String content = "" ;
        String line;
        while ((line = in.readLine()) != null) {
            content += line + "\n";
        }
        in.close();
        //解析单个子文件
        Pattern pattern = Pattern.compile(".*ts|.*m3u8");
        Matcher ma = pattern.matcher(content);
        List<String> list = new ArrayList<String>();
        while(ma.find()){
            String item=ma.group();
            if(item.endsWith("m3u8")){
                Res.M3U8URL=item.startsWith("http")?item:Res.M3U8BASEURL+item;
                Res.M3U8BASEURL=Res.M3U8URL.substring(0,Res.M3U8URL.lastIndexOf("/")+1);
                list.addAll(getIndexFile(Res.M3U8URL));
                break;
            }
            else
                list.add(item);
        }
        return list;
    }

    //视频片段合成
    public String composeFile(ConcurrentHashMap<Integer,String> keyFileMap) throws Exception{
        if(keyFileMap.isEmpty()) return null;
        String fileOutPath = Res.FOLD_PATH + File.separator + fileName+File.separator+fileName+".mp4";
        BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(new File(fileOutPath)));
        BufferedInputStream bis;
        byte[] bytes = new byte[1024];
        int length = 0;
        for(int i=0; i<keyFileMap.size(); i++){
            String nodePath = keyFileMap.get(i);
            File file = new File(nodePath);
            if(!file.exists())  continue;
            bis=new BufferedInputStream(new FileInputStream(file));
            while ((length = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, length);
            }
            bis.close();
            file.delete();
        }
        bos.flush();
        bos.close();
        return fileName;
    }




}
