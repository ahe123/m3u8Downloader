package com.ahery.main;

import com.ahery.utils.ReadFile;
import com.ahery.utils.WriteFile;
import net.sf.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Crawl80smp4PlayUtils {
    static HashMap<String,String> location_language_map=new HashMap<>();
    static {
        location_language_map.put("大陆","普通话");
        location_language_map.put("日本","日语");
        location_language_map.put("韩国","韩语");
        location_language_map.put("香港","粤语");
        location_language_map.put("台湾","汉语");
        location_language_map.put("美国","英语");
        location_language_map.put("英国","英语");
        location_language_map.put("泰国","泰语");
        location_language_map.put("意大利","意大利语");
        location_language_map.put("德国","德语");
        location_language_map.put("法国","法语");
        location_language_map.put("西班牙","西班牙语");
        location_language_map.put("俄罗斯","俄语");
    }



    /**
     * 爬取影片详情页url
     * @param newSaveFilePath
     * @param oldSaveFilePath
     * @param totalPages
     * @throws IOException
     */
    public static void getUpdateFilmPage(String newSaveFilePath,String oldSaveFilePath,String filmListUrl,int totalPages) throws IOException {
        ArrayList<String> oldUrls=new ReadFile(oldSaveFilePath).readToList();
        HashMap<String,Integer> map=new HashMap<>();
        for(String s:oldUrls){
            JSONObject jobj=JSONObject.fromObject(s);
            String key=jobj.getString("title")+jobj.getString("updateState");
            map.put(key,1);
        }
        WriteFile wf=new WriteFile(newSaveFilePath);
        WriteFile oldWf=new WriteFile(oldSaveFilePath);
        String baseUrl="http://80smp4.cc";
        String tailUrl=".html";
        for(int i=1;i<=totalPages;i++){
            JSONObject jsonObject = JSONObject.fromObject("{}");
            String url=filmListUrl+(i==1?"":i)+tailUrl;
            Document doc=Jsoup.connect(url)
                    .timeout(10000)
                    .get();
            Elements elements=doc.getElementsByClass("stui-vodlist__thumb lazyload");
            for(Element e:elements){
                jsonObject.put("title",e.attr("title"));
                jsonObject.put("href",baseUrl+e.attr("href"));
                try {
                    jsonObject.put("updateState",e.child(1).text().trim());
                }catch (Exception e1){
                    jsonObject.put("updateState","未知");
                }
                if(map.containsKey(jsonObject.getString("title")+jsonObject.getString("updateState"))){
                    System.out.println(i+":repeat!");
                    continue;
                }
                wf.write(jsonObject.toString()+"\r\n",true);
                oldWf.write(jsonObject.toString()+"\r\n",true);
                System.out.println(i+":"+jsonObject.toString());
            }
        }
    }





    /**
     * 爬取影片详细信息
     * @param originFilePath 原始文件路径
     * @param updateFilePath 更新文件路径
     * @param filmCategory 影片类型：电影/电视剧/动漫
     */
    public static void getFilmDetails(String urlFilePath,String originFilePath,String updateFilePath,String filmCategory){
        ArrayList<String> urls=new ReadFile(urlFilePath).readToList();
        int threadCount=10;
        ArrayList<ArrayList<String>> subUrlLists=new ArrayList<>();
        for(int i=0;i<threadCount;i++)
            subUrlLists.add(new ArrayList<>());
        int index=0;
        for(int i=0;i<urls.size();i++){
            subUrlLists.get(index++).add(urls.get(i));
            index=index<threadCount?index:0;
        }
        for(int i=0;i<threadCount;i++){
            final int finalI=i;
            new Thread(){
                @Override
                public void run() {
                    doCrawl(subUrlLists.get(finalI),originFilePath,updateFilePath,filmCategory);
                }
            }.start();
        }
    }
    public static void doCrawl(ArrayList<String> urls,String originFilePath,String updateFilePath,String filmCategory) {
        String baseUrl="http://80smp4.cc";
        JSONObject basicInfoObj=JSONObject.fromObject("{}");
        JSONObject tempObj=null;
        Document doc=null;
        WriteFile owf=new WriteFile(originFilePath);
        WriteFile uwf=new WriteFile(updateFilePath);
        for(int i=0;i<urls.size();i++){
            try {
                tempObj =JSONObject.fromObject(urls.get(i));
                basicInfoObj.put("filmName",tempObj.getString("title"));
                basicInfoObj.put("updateState",tempObj.getString("updateState"));
                doc=recursionGet(tempObj.getString("href"),1,1);
                Element element1=doc.getElementsByClass("col-pd clearfix").last();
                Element imgElement=element1.getElementsByTag("img").first();
                String onPosterUrl=imgElement.attr("data-original");
                onPosterUrl=onPosterUrl.startsWith("http")?onPosterUrl:baseUrl+onPosterUrl;
                basicInfoObj.put("onlinePosterUrl",onPosterUrl);
                Elements elements1=element1.getElementsByTag("p");
                for(int j=0;j<3;j++){
                    Element tempElement=elements1.get(j);
                    if(j==0){
                        basicInfoObj.put("type",tempElement.getElementsByTag("a").get(0).text().trim());
                        basicInfoObj.put("location",tempElement.getElementsByTag("a").get(1).text().trim());
                        basicInfoObj.put("releaseDate",tempElement.getElementsByTag("a").get(2).text().trim()+"-07-01");
                    }else if(j==2||j==1){
                        String tempStr=tempElement.text();
                        tempStr=tempStr.substring(tempStr.indexOf("：")+1,tempStr.length()).trim();
                        basicInfoObj.put(j==1?"actor":"director",tempStr);
                    }
                }
                Element element2=doc.getElementById("desc").getElementsByTag("p").get(1);
                basicInfoObj.put("story",element2.text());
                String language="其它";
                if(location_language_map.containsKey(basicInfoObj.getString("location")))
                    language=location_language_map.get(basicInfoObj.getString("location"));
                basicInfoObj.put("language",language);
                basicInfoObj.put("duration","未知");
                basicInfoObj.put("score","0.0");
                basicInfoObj.put("keyWords","未知");
                basicInfoObj.put("hasHDPoster","0");
                basicInfoObj.put("filmCategory",filmCategory);

                ArrayList<String> playPageUrls=new ArrayList<>();
                ArrayList<String> playPageTitles=new ArrayList<>();
                elements1=doc.getElementsByClass("stui-content__playlist column8 clearfix").first().getElementsByTag("a");
                for(Element element:elements1){
                    playPageUrls.add(baseUrl+element.attr("href"));
                    playPageTitles.add(element.attr("title"));
                }
                JSONObject playSetsM3u8UrlObj=getSetsM3u8Url(playPageTitles,playPageUrls);
                basicInfoObj.put("m3u8Url",playSetsM3u8UrlObj.toString());
                uwf.write(basicInfoObj.toString()+"\r\n", true);
                owf.write(basicInfoObj.toString()+"\r\n", true);
                System.out.println(i+"----"+tempObj.getString("title")+":success");
            }catch (Exception e){
                e.printStackTrace();
                System.err.println(i+"----"+tempObj.getString("title")+":fail");
            }
        }

    }

    /**
     * @param playPageUrls
     * @return
     * @throws IOException
     */
    private static JSONObject getSetsM3u8Url(ArrayList<String> titles,ArrayList<String> playPageUrls) throws IOException{
        JSONObject jobj=JSONObject.fromObject("{}");
        Document doc=null;
        for(int i=0;i<playPageUrls.size();i++){
            String url=playPageUrls.get(i);
            doc=recursionGet(url,2,1);
            if(doc==null) return null;
            Element element=doc.getElementsByClass("stui-player__video clearfix").first();
            element=element.getElementsByTag("script").first();
            String m3u8Url=element.html();
            m3u8Url=m3u8Url.substring(m3u8Url.indexOf("now=")+5,m3u8Url.indexOf(";var pn")-1);
            jobj.put(titles.get(i),m3u8Url);
        }
        return jobj;
    }

    /**
     * @param url
     * @param flag
     * @return
     */
    public static Document recursionGet(String url,int flag ,int deep) throws IOException {
        if(deep>10) return null;
        Document doc = Jsoup.connect(url)
//                .header("Accept-Encoding", "gzip, deflate")
//                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
//                .maxBodySize(0)
//                .timeout(600000)
                .get();
        Element element=null;
        if(flag==1){
            element=doc.getElementsByClass("col-pd clearfix").last();
        }else if(flag==2){
            element=doc.getElementsByClass("stui-player__video clearfix").first();
        }else if(flag==3){
            element=doc.getElementsByClass("activeclearfix").first();
        }

        if(element==null){
//            System.out.println("url:"+url+",flag:"+flag+",deep:"+deep+"----fail");
            String newUrl=doc.html().trim();
            newUrl=getUrlFromJS(newUrl);
            return recursionGet(newUrl,flag,deep+1);
        }else
//         System.out.println("url:"+url+",flag:"+flag+",deep:"+deep+"----success");
        return doc;
    }
    private static String getUrlFromJS(String jsStr){
        String url="http://80smp4.cc";
        ArrayList<String> subStr=new ArrayList<>();
        boolean split=false;
        while (jsStr.indexOf("'")!=-1){
            if(split)
                subStr.add(jsStr.substring(0,jsStr.indexOf("'")));
            jsStr=jsStr.substring(jsStr.indexOf("'")+1,jsStr.length());
            split=split?false:true;
        }
        for(int i=subStr.size()-1;i>=0;i--)
            url+=subStr.get(i);
        return url;
    }



}
