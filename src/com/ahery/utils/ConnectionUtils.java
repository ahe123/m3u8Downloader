package com.ahery.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionUtils {
    public static String driver = "com.mysql.cj.jdbc.Driver";
    public static String url = "jdbc:mysql://localhost:3306/balabala?serverTimezone=GMT";
    public static String username = "root";
    public static String password = "123456";
    //ThreadLocal：保证一个线程中只能有一个连接，避免连接的浪费
    public static ThreadLocal<Connection> tl = new ThreadLocal<>();

    public ConnectionUtils(){
        try {
            Class.forName(driver);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取连接的方法
     */
    public Connection getConn() throws Exception{
        //先尝试从tl中获取
        Connection conn = tl.get();
        if (conn == null) {
            conn = DriverManager.getConnection(url, username, password);
            tl.set(conn);
        }
        return conn;
    }

    /**
     * 关闭连接的方法
     */
    public void closeConn() throws Exception{
        //先尝试从tl中获取
        Connection conn = tl.get();
        if (conn != null) {
            conn.close();
        }
        tl.set(null);
    }

}
