package com.ahery.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class M3U8 {
    private String basepath;
    private List<Ts> tsList = new ArrayList<>();
    private long startTime;// 开始时间
    private long endTime;// 结束时间
    private long startDownloadTime;// 开始下载时间
    private long endDownloadTime;// 结束下载时间

    public String getBasepath() {
        return basepath;
    }

    public void setBasepath(String basepath) {
        this.basepath = basepath;
    }

    public List<Ts> getTsList() {
        return tsList;
    }

    public void setTsList(List<Ts> tsList) {
        this.tsList = tsList;
    }

    public void addTs(Ts ts){
        this.tsList.add(ts);
    }

    public long getStartDownloadTime() {
        return startDownloadTime;
    }

    public void setStartDownloadTime(long startDownloadTime) {
        this.startDownloadTime = startDownloadTime;
    }

    public long getEndDownloadTime() {
        return endDownloadTime;
    }

    public void setEndDownloadTime(long endDownloadTime) {
        this.endDownloadTime = endDownloadTime;
    }

    public long getStartTime(){
        if(tsList.size()>0){
            Collections.sort(tsList);
            return tsList.get(0).getLongDate();
        }
        return 0;
    }

    public long getEndTime() {
        if(tsList.size()>0){
//            Collections.sort(tsList);
            Ts ts=tsList.get(tsList.size()-1);
            return ts.getLongDate()+(long)(ts.getSeconds()*1000);
        }
        return 0;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("basepath: " + basepath);
        for (Ts ts : tsList) {
            sb.append("\nts_file_name = " + ts);
        }
        sb.append("\n\nstartTime = " + startTime);
        sb.append("\n\nendTime = " + endTime);
        sb.append("\n\nstartDownloadTime = " + startDownloadTime);
        sb.append("\n\nendDownloadTime = " + endDownloadTime);
        return sb.toString();
    }
}
