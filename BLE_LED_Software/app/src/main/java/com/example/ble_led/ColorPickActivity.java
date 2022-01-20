package com.example.ble_led;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;




public class ColorPickActivity extends Activity {

    private View mbtn;
    private View mbtn2;
    private View line;
    private View bkgrd ;
    private View setcolor ;
    private Button on;
    private Button off;

    private Bitmap bitmap;
    private TextView title;
    private int red,green,blue;
    private float scale = 1;


    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    //private static final String LED_ADDRESS = "00:1B:10:1E:17:E5";E6380D125445
    //private static final String LED_ADDRESS = "E6:38:0D:12:54:45";52BE0E125445
    private static final String LED_ADDRESS = "52:BE:0E:12:54:45";  //这里填写你的蓝牙地址
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

    private InputStream is;    //输入流，用来接收蓝牙数据
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存


    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean _discoveryFinished = false;
    boolean bRun = true;
    boolean bThread = false;
    String color = ""+0+" "+0+" "+0+"\n";
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_pick);
        mbtn = findViewById(R.id.movebtn);
        mbtn2 = findViewById(R.id.cube);
        on = findViewById(R.id.ON);
        off = findViewById(R.id.OFF);
        line = findViewById(R.id.Line);
        setcolor = findViewById(R.id.setcolor);
        bkgrd = findViewById(R.id.backgrd);
        bkgrd.setDrawingCacheEnabled(true);
        bkgrd.buildDrawingCache();
        title = findViewById(R.id.title);
        //bitmap =((BitmapDrawable)backgrd.getDrawable()).getBitmap();//获取圆盘图片

        measureView(title);
        measureView(bkgrd);
        mbtn.setY(title.getMeasuredHeight()*5);
        mbtn.setX(title.getMeasuredWidth()/2);

        //Log.i("position","R"+ bkgrd.getMeasuredHeight()+",G"+bkgrd.getMeasuredWidth());

        mbtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
               // Log.i("ww","R");
                bitmap = bkgrd.getDrawingCache();
                int width = bkgrd.getWidth();
                int top = bkgrd.getTop();
                int bottom = bkgrd.getBottom();
                int centerX = width/2;
                int centerY = (top+bottom)/2;
                float x = 220;
                float y = 270;
                if(motionEvent.getAction()== MotionEvent.ACTION_MOVE){
                    x = view.getX()+motionEvent.getX();
                    y = view.getY()+motionEvent.getY();
                    if((x-centerX)*(x-centerX)+(y-centerY)*(y-centerY)<=centerX*centerX-3) {
                        view.setX(x-mbtn.getWidth()/2+15);
                        view.setY(y-mbtn.getHeight()/2-3);
                        if(x<0)
                            x = 0;
                        if(y<0)
                            y = 0;
                        if(y>bitmap.getHeight())
                            y=bitmap.getHeight()-3;

                        int pixel = bitmap.getPixel((int)x,(int)(y-title.getHeight()-13));
                        setcolor.setBackgroundColor(Color.rgb(Color.red(pixel),Color.green(pixel),Color.blue(pixel)));
                       // Log.i("color","R"+Color.red(pixel)+",G"+Color.green(pixel)+",B"+Color.blue(pixel));
                        red = Color.red(pixel)*3;
                        green = Color.green(pixel)*2;
                        blue = Color.blue(pixel)*2;
                        color=""+(int)(red*scale)+" "+(int)(green*scale)+" "+(int)(blue*scale)+"\n";
                        Log.i("color1",color+"   "+scale);
                        bleSend(color);

                    }
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    // color=""+Color.red(pixel)+" "+Color.green(pixel)+" "+Color.blue(pixel);
                }
                return true;
            }
        });

        mbtn2.setOnTouchListener(new View.OnTouchListener() {
            TextView light = findViewById(R.id.light);
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = 0;
                if(motionEvent.getAction()== MotionEvent.ACTION_MOVE){
                    x = view.getX()+motionEvent.getX()-5;
                    if(x<line.getWidth()-25)
                       view.setX(x);
                    scale = view.getX()/(line.getWidth()-25);
                    if(scale>0.99)
                        scale = 1;
                    if(scale<0)
                        scale = 0;
                    color = (int)(scale*red)+" "+(int)(scale*green)+" "+(int)(scale*blue)+"\n";
                    Log.i("color2",color+" haha  "+scale);
                    bleSend(color);
                    light.setText("当前亮度:"+(int)(scale*100)+"%");
                }
                return true;
            }
        });
        setcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ColorPickActivity.this, "github： nene1212！", Toast.LENGTH_SHORT).show();
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleSend(""+100+" "+10+" "+10+"\n");
                bleSend(""+1+" "+0+" "+0+"\n");
            }
        });
        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleSend(color);
            }
        });
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /* 解决兼容性问题，6.0以上使用新的API*/
        final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
        final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSION_ACCESS_COARSE_LOCATION);
                Log.e("11111","ACCESS_COARSE_LOCATION");
            }
            if(this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_ACCESS_FINE_LOCATION);
                Log.e("11111","ACCESS_FINE_LOCATION");
            }
        }

        //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 设置设备可以被搜索
        new Thread(){
            public void run(){
                if(_bluetooth.isEnabled()==false){
                    _bluetooth.enable();
                }
            }
        }.start();
        onConnectButtonClicked(mbtn);
    }
    //发送按键响应
    public void bleSend(String mes){
        int i=0;
        int n=0;
        if(_socket==null){
            Toast.makeText(this, "没有找到你的夜灯\n        (○´･д･)ﾉ", Toast.LENGTH_SHORT).show();
            onConnectButtonClicked(mbtn);
            return;
        }
        try{
            OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
            byte[] bos = mes.getBytes();
            for(i=0;i<bos.length;i++){
                if(bos[i]==0x0a)n++;
            }
            byte[] bos_new = new byte[bos.length+n];
            n=0;
            for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
                if(bos[i]==0x0a){
                    bos_new[n]=0x0d;
                    n++;
                    bos_new[n]=0x0a;
                }else{
                    bos_new[n]=bos[i];
                }
                n++;
            }

            os.write(bos_new);
        }catch(IOException e){
        }
    }
    //接收活动结果，响应startActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String address = LED_ADDRESS;
        //String address = data.getExtras()
        //        .getString(Ble_SelectActivity.EXTRA_DEVICE_ADDRESS);
        Log.i("ble6",""+address);
        // 得到蓝牙设备句柄
        _device = _bluetooth.getRemoteDevice(address);
        // 用服务号得到socket
        try{
            _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        }catch(IOException e){
            Toast.makeText(this, "为什么连接不上呢 QAQ", Toast.LENGTH_SHORT).show();
            onConnectButtonClicked(mbtn);
        }
        //连接socket
        Button btn = (Button) findViewById(R.id.link);
        try{
            _socket.connect();
            Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
//                       btn.setText("断开");
        }catch(IOException e){
            try{
                Toast.makeText(this, "失败了。。再等一下下哦 φ(゜▽゜*)♪", Toast.LENGTH_SHORT).show();
                _socket.close();
                _socket = null;
                onConnectButtonClicked(mbtn);
            }catch(IOException ee){
                Toast.makeText(this, "连接失败3！", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        //打开接收线程
        try{
            is = _socket.getInputStream();   //得到蓝牙数据输入流
        }catch(IOException e){
            Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        if(bThread==false){ readThread.start();
         bThread=true;
        }
        else{
            bRun = true;
        }
    }
    //接收数据线程
    Thread readThread=new Thread(){

        public void run(){
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            //接收线程
            while(true){
                try{
                    while(is.available()==0){
                        while(bRun == false){}
                    }
                    while(true){
                        if(!bThread)//跳出循环
                            return;

                        num = is.read(buffer);         //读入数据
                        n=0;

                        String s0 = new String(buffer,0,num);
                        fmsg+=s0;    //保存收到数据
                        for(i=0;i<num;i++){
                            if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
                                buffer_new[n] = 0x0a;
                                i++;
                            }else{
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new,0,n);
                        smsg+=s;   //写入接收缓存
                        if(is.available()==0)break;  //短时间没有数据才跳出进行显示
                    }
                    //发送显示消息，进行显示刷新
                    handler.sendMessage(handler.obtainMessage());
                }catch(IOException e){
                }
            }
        }
    };
    //消息处理队列
    Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
           // tv_in.setText(smsg);   //显示数据
         //   sv.scrollTo(0,tv_in.getMeasuredHeight()); //跳至数据最后一页
        }
    };
    //关闭程序掉用处理部分
    public void onDestroy(){
        super.onDestroy();
        if(_socket!=null)  //关闭连接socket
            try{
                _socket.close();
            }catch(IOException e){}
        //	_bluetooth.disable();  //关闭蓝牙服务
    }
    //连接按键响应函数
    public void onConnectButtonClicked(View v){
        if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            return;
        }

        //如未连接设备则打开DeviceListActivity进行设备搜索
       // Button btn = (Button) findViewById(R.id.link_btn);
        if(_socket==null){
            Intent serverIntent = new Intent(this, LinkWaitingActivity.class); //跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
        }
        else{
            //关闭连接socket
            try{
                bRun = false;
                Thread.sleep(2000);

                is.close();
                _socket.close();
                _socket = null;

               // btn.setText("连接");
            }catch(IOException e){}
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    //清除按键响应函数
    public void onClearButtonClicked(View v){
        smsg="";
        fmsg="";
        //tv_in.setText(smsg);
        return;
    }

    //退出按键响应函数
    public void onQuitButtonClicked(View v){

        //---安全关闭蓝牙连接再退出，避免报异常----//
        if(_socket!=null){
            //关闭连接socket
            try{
                bRun = false;
                Thread.sleep(2000);

                is.close();
                _socket.close();
                _socket = null;
            }catch(IOException e){}
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        finish();
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if(lp == null){
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        //headerView的宽度信息
        int childMeasureWidth = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
        int childMeasureHeight;
        if(lp.height > 0){
            childMeasureHeight = View.MeasureSpec.makeMeasureSpec(lp.height, View.MeasureSpec.EXACTLY);
            //最后一个参数表示：适合、匹配
        } else {
            childMeasureHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);//未指定
        }
//System.out.println("childViewWidth"+childMeasureWidth);
//System.out.println("childViewHeight"+childMeasureHeight);
        //将宽和高设置给child
        child.measure(childMeasureWidth, childMeasureHeight);
    }

}