package com.ahery.main;

import com.ahery.utils.ReadFile;
import com.ahery.utils.WriteFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class WebPageCrawling {

    private ArrayList<String> movieUrlLists;
    private ArrayList<ConcurrentHashMap<Integer,String>> subUrls;

    public WebPageCrawling(){
        movieUrlLists=new ArrayList<>();
        subUrls=new ArrayList<>();
    }

    public void crawling() {
        try {
//            crawlMovieUrlList();

            for(int i=0;i<19300;i+=100){
                System.out.println("iterate:"+i);
                crawlM3U8Url(i,i+100);
                while (Res.THREAD_RUN_FLAG)
                    Thread.sleep(Res.THREAD_SLEEP_TIME);
            }
        }catch (Exception e){
            System.out.println("网络异常");
        }
    }

    ////获取listurl
    public void crawlMovieUrlList() throws InterruptedException {
        //分配线程数与子线程任务
        int threads=Res.TOTAL_PAGE>Res.MAX_THREADS?Res.MAX_THREADS:Res.TOTAL_PAGE;
        for(int i=0;i<threads;i++)
            subUrls.add(new ConcurrentHashMap<>());
        int subIndex=0;
        for(int i=1;i<=Res.TOTAL_PAGE;i++){
            subUrls.get(subIndex++).put(i,Res.URL+i);
            subIndex=subIndex>=threads?0:subIndex;
        }
        //启动线程下载
        for(int i=0;i<threads;i++)
            new CrawlThread(subUrls.get(i),movieUrlLists,Res.TOTAL_PAGE*Res.MOVIES_PER_PAGE,1).start();
        while (movieUrlLists.size()<Res.TOTAL_PAGE*Res.MOVIES_PER_PAGE)
            Thread.sleep(Res.WAIT_DOWNLOAD_TIME);
        new WriteFile(Res.FOLD_PATH+ File.separator+"hot_movieUrlLists.txt").write(movieUrlLists,true);
        System.out.println("crawling hot_movieUrlLists ok");
    }


    ////获取m3u8Urllist
    public void crawlM3U8Url(int startIndex,int endIndex) throws InterruptedException {
        subUrls.clear();
        movieUrlLists.clear();
        movieUrlLists=new ReadFile(Res.FOLD_PATH+ File.separator+"new_movieUrlLists.txt").readToList();
        int threads=Res.MAX_THREADS<movieUrlLists.size()?Res.MAX_THREADS:movieUrlLists.size();
        for(int i=0;i<threads;i++)
            subUrls.add(new ConcurrentHashMap<>());
        int subIndex=0,totalTasks=0;
        for(int i=startIndex;i<endIndex;i++){
            String str=movieUrlLists.get(i);
            if(str.equals("null"))
                continue;
            subUrls.get(subIndex++).put(i-startIndex,str);
            totalTasks++;
            subIndex=subIndex>=threads?0:subIndex;
        }
        movieUrlLists.clear();
        for(int i=0;i<threads;i++)
            new CrawlThread(subUrls.get(i),movieUrlLists,totalTasks,2).start();
//        while (movieUrlLists.size()<totalTasks)
//            Thread.sleep(Res.WAIT_DOWNLOAD_TIME);
//        new WriteFile(Res.FOLD_PATH+ File.separator+"topScore_m3u8Url.txt").write(movieUrlLists,true);
//        System.out.println("crawling topScore_m3u8Url ok");
    }





}
