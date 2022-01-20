package com.example.ble_led;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class LinkWaitingActivity extends AppCompatActivity {
    // 调试用
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;


    // 返回时数据标签
    public static String EXTRA_DEVICE_ADDRESS = "设备地址";
    //private static final String LED_ADDRESS = "00:1B:10:1F:17:E5";
    //private static final String LED_ADDRESS = "E6:38:0D:12:54:45";
    private static final String LED_ADDRESS = "52:BE:0E:12:54:45";  //更改蓝牙地址以匹配你的蓝牙设备
    private boolean flag = true;        //是否匹配到对应的设备
    // 成员域
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayList<String> greet = new ArrayList<>();  //随机显示的text

    Ble_Scan BS = new Ble_Scan();
    Thread Ble_Scan = new Thread(BS,"Ble_Scan");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_waiting);

// 初使化设备存储数组
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        //添加等待问候语
        greet.add("这是一个开源软件哦~ 详细搜索Bilibili up主： nene1212");
        greet.add("bilibili  干杯！");


        // 注册接收查找到设备action接收器
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // 注册查找结束action接收器
        //filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));

        // 得到本地蓝牙句柄
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Ble_Scan.start();

/****************************************动画显示***********************************************************************/
        ImageView img = findViewById(R.id.img);
        img.animate().rotation(360000).setDuration(200000000).setInterpolator(new LinearInterpolator()).start();


        final TextView log = findViewById(R.id.log);
        ValueAnimator myanime1 = ValueAnimator.ofInt(0,4);
        myanime1.setDuration(4000);
        myanime1.setRepeatCount(ValueAnimator.INFINITE);
        myanime1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ImageView img = findViewById(R.id.img);

                switch(valueAnimator.getAnimatedValue()+""){
                    case "0":log.setText("正在寻找小夜灯   ");break;
                    case "1":log.setText("正在寻找小夜灯.  ");break;
                    case "2":log.setText("正在寻找小夜灯.. ");break;
                    case "3":log.setText("正在寻找小夜灯...");break;
                    default:
                        //Log.i("case",valueAnimator.getAnimatedValue()+"");
                }
            }
        });

        ValueAnimator myanime3 = ValueAnimator.ofInt(0,10);
        myanime3.setDuration(10000);
        myanime3.setRepeatCount(ValueAnimator.INFINITE);
        myanime3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Random num = new Random();
                final TextView hello = findViewById(R.id.hello);
                switch(valueAnimator.getAnimatedValue()+""){
                    case "0":hello.animate().alpha(0).setDuration(2000).start();break;
                    case "5":hello.animate().alpha(100).setDuration(3000).start();hello.setText(greet.get(num.nextInt(greet.size())));break;
                    default:
                       // Log.i("case",valueAnimator.getAnimatedValue()+"");
                }

            }
        });
        myanime1.start();
        myanime3.start();

    }
    /*******************************************************************************************************************/
 class Ble_Scan implements Runnable{

    @Override
    public void run() {
        while(flag){
            doDiscovery();
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // 在窗口显示查找中信息
        setProgressBarIndeterminateVisibility(true);
        // 关闭再进行的服务查找
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
        Log.e(TAG,"startDiscovery()");
    }

    // 查找到设备和搜索完成action监听器
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG,action);
            // 查找到设备action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 得到蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示

                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    Log.i("nene1212",""+device.getAddress());
                    Log.i("114514",""+LED_ADDRESS);
                    if(LED_ADDRESS.equals(device.getAddress())){
                            Log.i("31807383", "" + device.getAddress());
                            flag = false;
                            Ble_Scan.interrupt();
                            finish();
                    }

                Log.e(TAG,device.getName() + "---" + device.getAddress());
                // 搜索完成action
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle("选择要连接的设备");
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "没有找到新设备";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}