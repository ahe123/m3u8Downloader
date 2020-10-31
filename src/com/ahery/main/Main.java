package com.ahery.main;


import com.ahery.utils.*;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Main {

    public static Segmenter segmenter;

    public static void main(String[] args) throws Exception {

//        updateFilmUrl("dy",804);
//        updateFilmUrl("dm",140);
//        updateFilmUrl("tv",367);

//        updateFilmDetails("dy");
//        updateFilmDetails("tv");
//        updateFilmDetails("dm");

//        segmenter=new Segmenter(args);
//        updateToDataBase("dm");


        updateFilmM3u8Url();

    }


    /**
     * 获取详情页url
     * @param type
     * @param totalPages
     * @throws IOException
     */
    public static void updateFilmUrl(String type,int totalPages) throws IOException {
        String newSaveFilePath="";
        String oldSaveFilePath="";
        String filmListUrl="http://80smp4.cc/dy/index";
        switch (type){
            case "dy":
                newSaveFilePath="E:\\MyVideo\\m3u8\\dy\\playUrlUpdate.json";
                oldSaveFilePath="E:\\MyVideo\\m3u8\\dy\\playUrl.json";
                filmListUrl="http://80smp4.cc/dy/index";
                break;
            case "dm":
                newSaveFilePath="E:\\MyVideo\\m3u8\\dm\\playUrlUpdate.json";
                oldSaveFilePath="E:\\MyVideo\\m3u8\\dm\\playUrl.json";
                filmListUrl="http://80smp4.cc/dm/index";
                break;
            case "tv":
                newSaveFilePath="E:\\MyVideo\\m3u8\\tv\\playUrlUpdate.json";
                oldSaveFilePath="E:\\MyVideo\\m3u8\\tv\\playUrl.json";
                filmListUrl="http://80smp4.cc/tv/index";
                break;
        }
        new WriteFile(newSaveFilePath).write("",false);
        Crawl80smp4PlayUtils.getUpdateFilmPage(newSaveFilePath,oldSaveFilePath,filmListUrl,totalPages);
    }



    /**
     * 获取影片详细数据
     * @param type
     */
    public static void updateFilmDetails(String type){
        String urlFilePath="";
        String originFilePath="";
        String updateFilePath="";
        String filmCategory="电影";
        switch (type){
            case "dy":
                urlFilePath="E:\\MyVideo\\m3u8\\dy\\playUrlUpdate.json";
                updateFilePath="E:\\MyVideo\\m3u8\\dy\\playBasicInfoUpdate.json";
                originFilePath="E:\\MyVideo\\m3u8\\dy\\playBasicInfo.json";
                filmCategory="电影";
                break;
            case "dm":
                urlFilePath="E:\\MyVideo\\m3u8\\dm\\playUrlUpdate.json";
                updateFilePath="E:\\MyVideo\\m3u8\\dm\\playBasicInfoUpdate.json";
                originFilePath="E:\\MyVideo\\m3u8\\dm\\playBasicInfo.json";
                filmCategory="动漫";
                break;
            case "tv":
                urlFilePath="E:\\MyVideo\\m3u8\\tv\\playUrlUpdate.json";
                updateFilePath="E:\\MyVideo\\m3u8\\tv\\playBasicInfoUpdate.json";
                originFilePath="E:\\MyVideo\\m3u8\\tv\\playBasicInfo.json";
                filmCategory="电视剧";
                break;
        }
        new WriteFile(updateFilePath).write("",false);
        Crawl80smp4PlayUtils.getFilmDetails(urlFilePath,originFilePath,updateFilePath,filmCategory);
    }


    /**
     *更新到数据库
     */
    public static void updateToDataBase(String type){
        String[] keys={"filmName","updateState","onlinePosterUrl","type","location","releaseDate","actor","director",
                "story","duration","score","keyWords","hasHDPoster","filmCategory","m3u8Url","language"};
        String filePath="E:\\MyVideo\\m3u8\\"+type+"\\playBasicInfoUpdate.json";
        String table=type+"play_film_basicinfo";
        ArrayList<String> dataList= new ReadFile(filePath).readToList();
        ArrayList<ArrayList<K_V>> paramsQueue=new ArrayList<>();
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        for(int i=0;i<dataList.size();i++){
            String s=dataList.get(i);
            ArrayList<K_V> params=new ArrayList<>();
            JSONObject jobj=JSONObject.fromObject(s);
            String filmName=jobj.getString("filmName");
            if(filmName.contains("："))
                filmName=filmName.substring(0,filmName.indexOf("：")).trim();
            if(filmName.contains("第")&&filmName.contains("季"))
                filmName=filmName.substring(0,filmName.indexOf("第"));
            List<String> segmented= segmenter.getSegmenter().segmentString(filmName);
            String keyWord="%";
            for(String temp:segmented){
                if(temp.length()>1&&!pattern.matcher(temp).matches())
                    keyWord+=temp+"%";
            }
            jobj.put("keyWords",keyWord);
            String language="其它";
            String location=jobj.getString("location");
            if(Crawl80smp4PlayUtils.location_language_map.containsKey(location)){
                language=Crawl80smp4PlayUtils.location_language_map.get(location);
            }
            jobj.put("language",language);
            params.add(new K_V("table",table));
            for(String key:keys){
                params.add(new K_V(key,jobj.getString(key)));
            }
            paramsQueue.add(params);
        }
        new BalaHttpRequest("AddFilmServlet",paramsQueue).start();
    }


    public static void updateFilmM3u8Url() throws Exception{
        String sql="select filmId,filmName from all_film_basicinfo;";
        Connection conn=new ConnectionUtils().getConn();
        PreparedStatement pstmt=conn.prepareStatement(sql);
        ResultSet rset=pstmt.executeQuery();
        ArrayList<JSONObject> jobjs=new ArrayList<>();
        JSONObject jobj;
        while (rset.next()){
            jobj=JSONObject.fromObject("{}");
            jobj.put("filmId",rset.getString(1));
            jobj.put("filmName",rset.getString(2));
            jobjs.add(jobj);
        }
        ArrayList<ArrayList<JSONObject>> jobjsList=new ArrayList<>(Res.MAX_THREADS);
        for(int i=0;i<Res.MAX_THREADS;i++){
            jobjsList.add(new ArrayList<>());
        }
        for(int i=0;i<jobjs.size();i++){
            int index=i%Res.MAX_THREADS;
            jobjsList.get(index).add(jobjs.get(i));
        }
        AtomicInteger handleCount=new AtomicInteger(0);
        final int totalCount=jobjs.size();
        ArrayList<String> res=new ArrayList<>();
        for(int i=0;i<Res.MAX_THREADS;i++){
            final ArrayList<JSONObject> tempJobjs=jobjsList.get(i);
            new Thread(){
                @Override
                public void run() {
                    try {
                        for(int i=0;i<tempJobjs.size();i++){
                            JSONObject jobj=tempJobjs.get(i);
                            String searchUrl="http://80smp4.cc/search.php?searchword=";
                            Document doc=Crawl80smp4PlayUtils.recursionGet(searchUrl+jobj.getString("filmName"),3,10);
                            Elements elements=doc.getElementsByClass("activeclearfix");
                            if(elements.size()<=0){
                                System.err.println("no search result!");
                                continue;
                            }
                            Element element=elements.get(0).getElementsByTag("a").first();
                            String pageUrl="http://80smp4.cc"+element.attr("href");
                            System.out.println(handleCount.incrementAndGet()+"/"+totalCount+":"+pageUrl);
                            jobj.put("pageUrl",pageUrl);
                            res.add(jobj.toString());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        while (handleCount.get()<jobjs.size()){}
        new WriteFile("E:\\Download\\80s\\all_film_page_url.json").write(res,false);

    }




}
