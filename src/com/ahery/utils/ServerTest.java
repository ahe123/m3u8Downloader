package com.ahery.utils;

import com.ahery.main.Res;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ServerTest {
    public static void request() throws IOException {
        String servletName="AppFilmDetailServlet";
        HttpURLConnection httpConn= (HttpURLConnection) new URL(Res.BALABALA_SERVER_IPV4+":"+
                Res.BALABALA_SERVER_PORT+"/"+Res.BALABALA_SERVER_PROJECT_RELEASE_NAME+"/" +
                servletName).openConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setConnectTimeout(Res.CONNECT_TIMEOUT);
        httpConn.setReadTimeout(Res.CONNECT_TIMEOUT);
        httpConn.setDoOutput(true);
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpConn.connect();
        DataOutputStream dos=new DataOutputStream(httpConn.getOutputStream());
        //设置参数
        ArrayList<K_V> params=new ArrayList<>();
        params.add(new K_V("filmName","拯救大兵瑞恩"));
        params.add(new K_V("filmId","7015320"));
        params.add(new K_V("client_ip","10.208.39.41"));
        String paramsStr="";
        for(K_V k_v:params)
            paramsStr+="&"+ URLEncoder.encode(k_v.getKey().toString(),"utf-8")+"="+URLEncoder.encode(k_v.getValue().toString(),"utf-8");
        paramsStr=paramsStr.length()==0?paramsStr:paramsStr.substring(1,paramsStr.length());
        dos.writeBytes(paramsStr);
        dos.flush();
        dos.close();

        //获取返回值
        BufferedReader br=new BufferedReader(new InputStreamReader(httpConn.getInputStream(),"utf-8"));
        String result="";
        String line;
        while ((line=br.readLine())!=null)
            result+=line;
        br.close();
        httpConn.disconnect();
        System.out.println(result);
    }
}
