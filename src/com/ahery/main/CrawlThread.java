package com.ahery.main;

import com.ahery.utils.WriteFile;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CrawlThread extends Thread {

    public ConcurrentHashMap<Integer, String> urls;
    public ArrayList<String> movieLists;
    private DecimalFormat df;
    private int totalTasks;
    private int executeFlag;

    public CrawlThread(ConcurrentHashMap<Integer, String> urls, ArrayList<String> movieLists, int totalTasks, int executeFlag) {
        Res.THREAD_RUN_FLAG = true;
        this.urls = urls;
        this.movieLists = movieLists;
        this.totalTasks = totalTasks;
        this.executeFlag = executeFlag;
        df = new DecimalFormat("0.00");
    }

    @Override
    public void run() {
        if (executeFlag == 1)
            getListUrl();
        else if (executeFlag == 2) {
            getM3U8();
        }
        Res.THREAD_RUN_FLAG = false;
    }


    //从列表页获取影片详情页地址
    public void getListUrl() {
        Document doc;
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i + 1);
            try {
                //影片列表页
                Connection conn = Jsoup.connect(url);
                conn.timeout(Res.CONNECT_TIMEOUT);
                doc = conn.get();
                Elements movieElements = doc.getElementsByClass("me1 clearfix").get(0).children();
                int index = 0;
                ArrayList<String> tempList = new ArrayList<>();
                for (Element element : movieElements) {
                    Element children = element.children().get(0);
                    String title = children.attr("title");
                    String href = Res.BASE_URL + children.attr("href");
                    String imgUrl = children.children().get(0).attr("_src");
                    tempList.add(title + "&" + href + "&" + imgUrl);
                    System.out.println("爬取完成：" + url + "[" + (index++) + "][" + title + "][进度：" + df.format((double) (movieLists.size() + tempList.size()) / totalTasks * 100) + "%]");
                }
                movieLists.addAll(tempList);
            } catch (Exception e) {
                System.err.println("连接超时：" + url);
                for (int j = 0; j < Res.MOVIES_PER_PAGE; j++)
                    movieLists.add("null");
                continue;
            }//catch
        }//urls iterate
    }//end


    //从影片播放页获取影片M3U8地址
    public void getM3U8() {
        Document doc;
        Connection conn;
        int crawledCount = 0;
        for (int i = 0; i < urls.size(); i++) {
            String[] arr = urls.get(i).split("&");
            String[][] m_bacicInfo = {{"影片名称","演员", "类型", "地区", "语言", "导演", "上映日期", "片长", "豆瓣评分", "剧情介绍","m3u8_url","在线海报地址","本地海报名称"}, {arr[0], "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "https:"+arr[2], "null"}};
            ConcurrentHashMap cMap = new ConcurrentHashMap();
            for (int j = 0; j < m_bacicInfo[0].length; j++)
                cMap.put(m_bacicInfo[0][j], m_bacicInfo[1][j]);

            //下载影片海报
            String posterFileName="";
            try {
                posterFileName=arr[0]+"_"+UUID.randomUUID().toString().replaceAll("-","")+".jpg";
                new DownloadImgThread("https:"+arr[2],Res.MOVIEPOSTER_FOLD_PATH+File.separator+ posterFileName).start();
                cMap.put("本地海报名称",posterFileName);
            }catch (Exception e){ }

            //获取影片基本信息
            try {
                conn = Jsoup.connect(arr[1]);
                conn.timeout(Res.CONNECT_TIMEOUT);
                doc = conn.get();
                Elements m_elements = doc.getElementById("minfo").getElementsByClass("font_888");
                for (Element m_element : m_elements) {
                    String temp = m_element.parent().text();
                    if (temp.startsWith("演员：") || temp.startsWith("类型：") || temp.startsWith("地区：") || temp.startsWith("语言：") || temp.startsWith("导演：") || temp.startsWith("上映日期：") || temp.startsWith("片长：") || temp.startsWith("豆瓣评分：") || temp.startsWith("剧情介绍：")){
                        String key=temp.substring(0, temp.indexOf("：")).trim();
                        String value=temp.substring(temp.indexOf("：") + 1).trim();
                        value=value==null?"null":value.length()==0?"null":value;
                        cMap.put(key,value);
                    }
                }
            } catch (Exception e) { }


            //影片播放页获取m3u8地址
            try {
                try {
                    sleep(Res.THREAD_SLEEP_TIME * 2);
                } catch (InterruptedException ie) {
                }
                conn = Jsoup.connect(arr[1] + "/play");
                conn.timeout(Res.CONNECT_TIMEOUT);
                doc = conn.get();
                Elements elements = doc.getElementsByTag("iframe");
                if (elements == null || elements.isEmpty()) {
                    crawledCount++;
                    System.err.println("无在线链接：" + arr[1] + "[" + arr[0] + "][m3u8][进度：" + df.format((double) crawledCount / urls.size() * 100) + "%]");
                    continue;
                }
                //获取m3u8地址
                Element element = elements.get(0);
                String href = element.attr("src");
                href = href.substring(href.indexOf("url=") + 4, href.length());
                cMap.put("m3u8_url",href);
                crawledCount++;
                System.out.println("爬取完成：" + arr[1] + "[" + arr[0] + "][m3u8][进度：" + df.format((double) crawledCount / urls.size() * 100) + "%]");
            } catch (Exception e) {
                crawledCount++;
                System.err.println("连接超时：" + arr[1] + "[" + arr[0] + "][m3u8][进度：" + df.format((double) crawledCount / urls.size() * 100) + "%]");
                continue;
            }//catch
            String content="{";
            for(int j=0;j<m_bacicInfo[0].length;j++)
                content+="\""+m_bacicInfo[0][j]+"\":"+"\""+cMap.get(m_bacicInfo[0][j])+"\",";
            content=content.substring(0,content.length()-1)+"}";
            movieLists.add(content);
        }//urls iterate
        new WriteFile(Res.FOLD_PATH + File.separator + "New_MovieBasicInfo.json").write(movieLists, true);
        System.err.println("crawling New_MovieBasicInfo ok");
//        sleep(Res.THREAD_SLEEP_TIME*2*60*10);
    }//end


}
