package com.xdandroid.sample;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncSocketMessageLoader extends AsyncTask<String, Integer, Boolean> {

    private Callback callback;

    public AsyncSocketMessageLoader(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String urlFormatter = "http://ichess.sinaapp.com/ext/update.php?code=%s&flag=%s";
        String strUrl = String.format(urlFormatter,params[0],params[1]);

        try {
            //创建URL对象
            URL url = new URL(strUrl);//Get请求可以在Url中带参数： ①url + "?bookname=" + name;②url="http://www.baidu.com?name=zhang&pwd=123";
            //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //在这里设置一些属性，详细见UrlConnection文档，HttpURLConnection是UrlConnection的子类
            //设置连接超时为5秒
            httpURLConnection.setConnectTimeout(5000);
            //设定请求方式(默认为get)
            httpURLConnection.setRequestMethod("GET");
            //建立到远程对象的实际连接
            httpURLConnection.connect();
            //返回打开连接读取的输入流，输入流转化为StringBuffer类型，这一套流程要记住，常用
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                //转化为UTF-8的编码格式
                line = new String(line.getBytes("UTF-8"));
                stringBuffer.append(line);
            }
            String txtResult = stringBuffer.toString();
            bufferedReader.close();
            httpURLConnection.disconnect();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(callback!=null){
            callback.execute("");
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        System.out.println("Getting...");
    }


}