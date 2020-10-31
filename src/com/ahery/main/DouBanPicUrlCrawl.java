package com.ahery.main;

import com.ahery.utils.WriteFile;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DouBanPicUrlCrawl extends Thread {
    public ArrayList<String> items;
    private String itemStr;
    private String imgUrl;
    private String filmName;
    private ArrayList<String> results;
    private int index=0;

    public DouBanPicUrlCrawl(ArrayList<String> items){
        this.items=items;
        results=new ArrayList<>();
    }

    @Override
    public void run() {
        for(int i=1345;i<items.size();i++){
            index=i;
            itemStr=items.get(i);
            filmName=itemStr.substring(itemStr.indexOf("影片名称")+7,itemStr.indexOf("演员")-3);
            try {
                getUrl();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            results.add(itemStr);
            if(results.size()==100){
                new WriteFile(Res.FOLD_PATH + File.separator + "New_MovieBasicInfo_version01.json").write(results, true);
                results.clear();
            }
        }
        new WriteFile(Res.FOLD_PATH + File.separator + "New_MovieBasicInfo_version01.json").write(results, true);
    }

    public void getUrl() throws InterruptedException {
        if(filmName==null||filmName.equals(""))
            return;
        String urlStr= Res.DOUBAN_POSTER_CRAWL_URL+filmName;
        sleep(Res.THREAD_SLEEP_TIME);
        Connection conn= Jsoup.connect(urlStr);
        conn.timeout(Res.CONNECT_TIMEOUT);
//        conn.c
        Document doc;
        try {
            doc=conn.get();
            Elements elements1=doc.getElementsByClass("result-list");
            if(elements1==null||elements1.size()==0){
                System.err.println("item"+index+"抓取失败");
                return;
            }
            Elements elements2=elements1.get(0).getElementsByClass("result");
            if(elements2==null){
                System.err.println("item"+index+"抓取失败");
                return;
            }
            for(Element element:elements2){
                element=element.getElementsByTag("h3").first();
                String content=element.text();
                if(!content.startsWith("[电影]"))
                    continue;
                element=element.getElementsByTag("a").first();
                String href=element.attr("href");
                href=href.substring(href.indexOf("subject%2F")+10,href.indexOf("%2F&query"));
                href=Res.DOUBAN_POSTER_DETAIL_URL+href+"/photos?type=R";
                getPosterUrl(href);
                break;
            }
        }catch (IOException e){
            System.err.println("item"+index+"抓取失败");
        }
    }


    public void getPosterUrl(String urlStr){
        Document doc;
        try {
            sleep(Res.THREAD_SLEEP_TIME);
            Connection conn1=Jsoup.connect(urlStr);
            conn1.timeout(Res.CONNECT_TIMEOUT);
            doc=conn1.get();
            Elements elements1=doc.getElementById("wrapper").getElementsByTag("li");
            if(elements1==null||elements1.size()==0){
                System.err.println("item"+index+"抓取失败");
                return;
            }
            String pic_id=elements1.first().attr("data-id");
//            sleep(Res.THREAD_SLEEP_TIME);
//            Connection conn2=Jsoup.connect(Res.DOUBAN_PIC_URL+pic_id+"/");
//            conn2.timeout(Res.CONNECT_TIMEOUT);
//            doc=conn2.get();
//            Element element=doc.getElementsByClass("photo-wp").first();
//            element=element.getElementsByTag("img").first();
//            imgUrl=element.attr("src");
            if(pic_id==null||pic_id.length()==0){
                System.err.println("item"+index+"抓取失败");
                return;
            }
            imgUrl="https://img1.doubanio.com/view/photo/l/public/p"+pic_id+".jpg";
            itemStr=itemStr.substring(0,itemStr.indexOf("在线海报地址")+9)+imgUrl+itemStr.substring(itemStr.indexOf("本地海报名称")-3,itemStr.length());
            System.out.println("item"+index+"抓取成功:"+imgUrl);
            int a=0;
        }catch (IOException | InterruptedException e){
            System.err.println("item"+index+"抓取失败");
        }
    }//end getPosterUrl()








}
