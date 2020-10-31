package com.ahery.main;

public class Res {
    /**
     * 本地服务器地址
     */
//    public static String BALABALA_SERVER_IPV4="http://127.0.0.1";
//    public static String BALABALA_SERVER_PORT="8081";
//    public static String BALABALA_SERVER_PROJECT_RELEASE_NAME="BalabalaServer_war_exploded";


    /**
     * 服务器地址
     */
    public static String BALABALA_SERVER_IPV4="http://49.235.40.89";
    public static String BALABALA_SERVER_PORT="8080";
    public static String BALABALA_SERVER_PROJECT_RELEASE_NAME="BalabalaServer_war";


    public static String FOLD_PATH="E:\\MyVideo\\m3u8";
    public static String MOVIEPOSTER_FOLD_PATH=FOLD_PATH+"\\moviePoster";
    public static String M3U8URL="https://qq.com-ixx-qq.com/20200602/9803_66f2b26e/index.m3u8";
    public static String M3U8BASEURL= M3U8URL.substring(0,M3U8URL.lastIndexOf("/")+1);

    public static int WAIT_DOWNLOAD_TIME=5*1000;
    public static int CONNECT_TIMEOUT=40*1000;
    public static int READ_TIMEOUT=40*1000;

    public static int MAX_THREADS=1;//可以开启的最大线程数


    /**
     * 网页爬取
     */
    public static String URL="http://www.y80s.com/movie/list/----h-p";
    public static String WEBSITE_URL="https://www.cmdy5.com/";
    public static String BASE_URL="https://www.cmdy5.com/dianying-";
    public static int TOTAL_PAGE=15;
    public static int MOVIES_PER_PAGE=25;
    public static int MAX_REQUEST=5;
    public static int THREAD_SLEEP_TIME=3000;

    //单线程运行标志
    public static boolean THREAD_RUN_FLAG=false;


    //豆瓣高清大图url
    //海报爬取网址，豆瓣
    public static String DOUBAN_POSTER_CRAWL_URL="https://www.douban.com/search?q=";
    //海报详情页url
    public static String DOUBAN_POSTER_DETAIL_URL="https://movie.douban.com/subject/";
    //豆瓣图片库url
    public static String DOUBAN_PIC_URL="https://movie.douban.com/photos/photo/";


}
