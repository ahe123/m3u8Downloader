package com.ahery.main;

import com.ahery.utils.ConnectionUtils;
import com.ahery.utils.ReadFile;
import net.sf.json.JSONObject;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataBaseOP {
    public void insertToRecommend_film(){
        DecimalFormat df=new DecimalFormat("0.0");
        String[] keys={"影片名称","演员", "类型", "地区", "语言", "导演", "上映日期", "片长", "豆瓣评分", "剧情介绍","m3u8_url","在线海报地址","本地海报名称"};
        ArrayList<String> lists=new ReadFile(Res.FOLD_PATH+ File.separator+"New_MovieBasicInfo_version01.json").readToList();
        int filmId=1000001;
        ConnectionUtils cu=new ConnectionUtils();
        Connection conn=null;
        try {
            conn=cu.getConn();
//            conn.setAutoCommit(false);
            String sqlStr="insert into recommend_film values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstmt;
            JSONObject jsonObj;
            for(int i=0;i<lists.size();i++){
                pstmt=conn.prepareStatement(sqlStr);
                String str=lists.get(i);
                String actor=str.substring(str.indexOf("演员")+5,str.indexOf("类型")-3);
                str=str.substring(0,str.indexOf("演员")+5)+"null"+str.substring(str.indexOf("类型")-3,str.length());
                String director=str.substring(str.indexOf("导演")+5,str.indexOf("上映日期")-3);
                str=str.substring(0,str.indexOf("导演")+5)+"null"+str.substring(str.indexOf("上映日期")-3,str.length());
                String stoty=str.substring(str.indexOf("剧情介绍")+7,str.indexOf("m3u8_url")-3);
                str=str.substring(0,str.indexOf("剧情介绍")+7)+"null"+str.substring(str.indexOf("m3u8_url")-3,str.length());
                jsonObj=JSONObject.fromObject(str);
                pstmt.setInt(1,filmId);
                for(int j=0;j<13;j++){
                    if(j==1)
                        pstmt.setString(j+2,actor);
                    else if(j==5)
                        pstmt.setString(j+2,director);
                    else if(j==9)
                        pstmt.setString(j+2,stoty);
                    else
                        pstmt.setString(j+2,jsonObj.getString(keys[j]));
                }
                pstmt.setInt(15,1);
                pstmt.executeUpdate();
                filmId++;
            }
//            pstmt.executeBatch();
//            conn.commit();
            cu.closeConn();
        }catch (Exception e){e.printStackTrace(); }//end try-catch
    }//end insert

    public void insertToFilm_basicinfo(){
        DecimalFormat df=new DecimalFormat("0.0");
        String[] keys={"影片名称","演员", "类型", "地区", "语言", "导演", "上映日期", "片长", "豆瓣评分", "剧情介绍","m3u8_url","在线海报地址","本地海报名称"};
        ArrayList<String> lists=new ReadFile(Res.FOLD_PATH+ File.separator+"TopScore_MovieBasicInfo.json").readToList();
        int filmId=1000001;
        ConnectionUtils cu=new ConnectionUtils();
        Connection conn=null;
        try {
            conn=cu.getConn();
//            conn.setAutoCommit(false);
            String sqlStr="insert into topscore_film_basicinfo values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstmt;
            JSONObject jsonObj;
            for(int i=0;i<lists.size();i++){
                System.out.println(i);
                pstmt=conn.prepareStatement(sqlStr);
                String str=lists.get(i);
                String actor=str.substring(str.indexOf("演员")+5,str.indexOf("类型")-3);
                str=str.substring(0,str.indexOf("演员")+5)+"null"+str.substring(str.indexOf("类型")-3,str.length());
                String director=str.substring(str.indexOf("导演")+5,str.indexOf("上映日期")-3);
                str=str.substring(0,str.indexOf("导演")+5)+"null"+str.substring(str.indexOf("上映日期")-3,str.length());
                String stoty=str.substring(str.indexOf("剧情介绍")+7,str.indexOf("m3u8_url")-3);
                str=str.substring(0,str.indexOf("剧情介绍")+7)+"null"+str.substring(str.indexOf("m3u8_url")-3,str.length());
                try{
                    jsonObj=JSONObject.fromObject(str);
                    pstmt.setInt(1,filmId);
                    for(int j=0;j<13;j++){
                        if(j==1)
                            pstmt.setString(j+2,actor);
                        else if(j==5)
                            pstmt.setString(j+2,director);
                        else if(j==8){
                            float score=5.0f;
                            String scoreStr=jsonObj.getString(keys[j]);
                            if(!scoreStr.equals("null")){
                                if(scoreStr.endsWith("分"))
                                    scoreStr=scoreStr.substring(0,scoreStr.length()-1);
                                try {
                                    score=Float.parseFloat(scoreStr);
                                }catch (Exception e){}
                            }
                            pstmt.setFloat(j+2,score);
                        }
                        else if(j==9)
                            pstmt.setString(j+2,stoty);
                        else
                            pstmt.setString(j+2,jsonObj.getString(keys[j]));
                    }
                    pstmt.setInt(15,0);
                    pstmt.executeUpdate();
                    filmId++;
                }catch (Exception e){continue;}
            }
//            pstmt.executeBatch();
//            conn.commit();
            cu.closeConn();
        }catch (Exception e){e.printStackTrace(); }//end try-catch
    }

    public void update1() throws Exception {

        int index=1000001;
        ConnectionUtils cu=new ConnectionUtils();
        Connection conn=null;
        conn=cu.getConn();
        String sql1="select releaseDate from topscore_film_basicinfo where filmId=?;";
        String sql2="update topscore_film_basicinfo set releaseDate=? where filmId=?;";
        PreparedStatement pstmt1;
        PreparedStatement pstmt2;
        pstmt1=conn.prepareStatement(sql1);
        pstmt2=conn.prepareStatement(sql2);
        ResultSet rset;
        String date="";//2011993
        for(;index<=1012009;index++){
            pstmt1.setInt(1,index);
            rset=pstmt1.executeQuery();
            rset.next();
            date=rset.getString(1);
            if(isValidDate1(date)) continue;
            else{
                System.out.println(index+":"+date);
                continue;
            }
//            if(date.length()>10) date=date.substring(0,10);
//            else if(date.length()==4) date+="-01-01";
//            else date="2010-06-01";
//            else if(date.length()==7) date+="-01";
//            if(!isValidDate(date)){
//                System.out.println(index+":"+date);
//                continue;
//            }
//            pstmt2.setString(1,date);
//            pstmt2.setInt(2,index);
//            pstmt2.executeUpdate();
        }
        conn.close();
    }




    public static boolean isValidDate1(String sDate) {
        String datePattern1 = "\\d{4}-\\d{2}-\\d{2}";
        String datePattern2 = "^((\\d{2}(([02468][048])|([13579][26]))"
                + "[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|"
                + "(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?"
                + "((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?("
                + "(((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?"
                + "((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
        if ((sDate != null)) {
            Pattern pattern = Pattern.compile(datePattern1);
            Matcher match = pattern.matcher(sDate);
            if (match.matches()) {
                pattern = Pattern.compile(datePattern2);
                match = pattern.matcher(sDate);
                return match.matches();
            } else {
                return false;
            }
        }
        return false;
    }


    public void test() throws Exception {
        String years="";
        for(int i=2000;i<=2020;i++)
            years+=i+"|";
        years=years.substring(0,years.length()-1);
        ConnectionUtils cu=new ConnectionUtils();
        Connection conn=null;
        conn=cu.getConn();
        String sql1="select filmId,filmName,score,onlinePosterUrl from topscore_film_basicinfo where filmId<=? ;";
        String sql2="select * from topscore_film_basicinfo where filmId>=?;";
        PreparedStatement pstmt1;
        pstmt1=conn.prepareStatement(sql1);
        PreparedStatement pstmt2=conn.prepareStatement(sql2);
        pstmt1.setString(1,1000010+"");
//        pstmt1.setString(2,"");
//        pstmt1.setString(3,"");
        ResultSet rset=pstmt1.executeQuery();
        while (rset.next())
            System.out.println(rset.getString(1));
        conn.close();
    }








}
