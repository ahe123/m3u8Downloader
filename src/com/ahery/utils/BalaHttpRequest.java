package com.ahery.utils;


import com.ahery.main.Res;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class BalaHttpRequest<T,S> extends Thread implements Serializable {

    private static final String SUCCESS_CODE="success";
    private static final String FAIL_CODE="fail";
    public static final int REQUEST_DELAY_TIME=300;

    private static final long serialVersionUID = 100185078845600300L;
    private String servletName;
    private ArrayList<ArrayList<K_V<T,S>>> paramsQueue;
    private int paramsCount;
    private ArrayList<K_V<T,S>> currentParams;
    private String result;

    public BalaHttpRequest(String servletName, ArrayList<ArrayList<K_V<T,S>>> paramsQueue) {
        this.servletName=servletName;
        this.paramsQueue=paramsQueue;
        this.paramsCount=paramsQueue.size();
        this.currentParams=null;
        this.result="";
    }

    @Override
    public void run() {
        while (!paramsQueue.isEmpty()){
            try {
                Thread.sleep(REQUEST_DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentParams=paramsQueue.remove(0);
            request();
            JSONObject jobj =JSONObject.fromObject(result);
            result="";
            if(jobj.getString("state").equals(SUCCESS_CODE)){
                String executeType=jobj.getString("executeType");
                System.out.println((paramsCount-paramsQueue.size())+"/"+paramsCount+" "+executeType+" completed");
            }else{
//                paramsQueue.add(currentParams);
            }
        }
    }

    public void request(){
        try {
            HttpURLConnection httpConn= (HttpURLConnection) new URL(Res.BALABALA_SERVER_IPV4+":"+Res.BALABALA_SERVER_PORT+"/"+Res.BALABALA_SERVER_PROJECT_RELEASE_NAME+"/" +servletName).openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setConnectTimeout(Res.CONNECT_TIMEOUT);
            httpConn.setReadTimeout(Res.READ_TIMEOUT);
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.connect();
            DataOutputStream dos=new DataOutputStream(httpConn.getOutputStream());
            //设置参数
            String paramsStr="";
            for(K_V k_v:currentParams)
                paramsStr+="&"+ URLEncoder.encode(k_v.getKey().toString(),"utf-8")+"="+ URLEncoder.encode(k_v.getValue().toString(),"utf-8");
            paramsStr=paramsStr.length()==0?paramsStr:paramsStr.substring(1,paramsStr.length());
            dos.writeBytes(paramsStr);
            dos.flush();
            dos.close();

            //获取返回值
            BufferedReader br=new BufferedReader(new InputStreamReader(httpConn.getInputStream(),"utf-8"));
            String line;
            while ((line=br.readLine())!=null)
                result+=line;
            br.close();
            httpConn.disconnect();
        }catch (ProtocolException e){
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }


}
