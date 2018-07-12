package com.xuzhong.rpctest.test.netty;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;  

import java.util.Properties;  
  
  
  
/** properties文件加载类 
 * @author xiefg 
 * 
 */  
public class Configuration {  
      
    private static Properties SYSTEM_CONFIG = new Properties();  
  
      
    public static String fileName = "";  
         
    private static final String PATH_SERVER_CONF = "/home/bird/eclipse-workspace/rpcTest/config/config.properties";  
      
  
    public static void init() {  
        try {  
        	if(!new File(PATH_SERVER_CONF).exists())
        		throw new RuntimeException("file is not exist!");
            InputStream inputStream = null;  
/*            ClassLoader classLoader = null;  
            classLoader = Thread.currentThread().getContextClassLoader();  
            inputStream = classLoader.getResourceAsStream(PATH_SERVER_CONF);  */
            inputStream = new FileInputStream(new File(PATH_SERVER_CONF));
            System.out.println(inputStream);
            SYSTEM_CONFIG.load(inputStream);  
              
            fileName = "";  
            int index = PATH_SERVER_CONF.lastIndexOf("/") == -1 ? PATH_SERVER_CONF  
                    .lastIndexOf("\\") : PATH_SERVER_CONF.lastIndexOf("/");  
            if (index > 0) {  
                fileName = PATH_SERVER_CONF.substring(index + 1);  
            }  
            inputStream.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return;  
    }  
  
    public static String getProperty(String key, String defaultValue) {  
        try {  
            return SYSTEM_CONFIG.getProperty(key, defaultValue);  
        } catch (Exception e) {  
            return null;  
        }  
    }  
  
    public static String getProperty(String key) {  
        try {  
            String value = SYSTEM_CONFIG.getProperty(key);  
            return value;  
        } catch (Exception e) {  
            return null;  
        }  
    }  
  
    public static void main(String[] args) {  
        init();  
        System.out.println(Configuration.getProperty("nioServerIp"));  
          
    }  
}  
