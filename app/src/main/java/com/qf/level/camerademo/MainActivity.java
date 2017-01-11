package com.qf.level.camerademo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String path;
    private String uploadUrl="http://192.168.56.1:8080/UploadServer/UploadAction";

    @ViewInject(R.id.imgId)
    private ImageView imageview;
    @ViewInject(R.id.videoId)
    private VideoView videoview;
    @ViewInject(R.id.playBtn1Id)
    private ImageButton playbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        x.Ext.setDebug(true);
        x.Ext.init(getApplication());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        x.view().inject(this);
    }

    private static final int REQUEST_CODE_IMAGE = 100;
    private static final int REQUEST_CODE_VIDEO = 200;
    @Event(R.id.btn1Id)
    private void takePhoto(View view){

        path= Environment.getExternalStorageDirectory()+"/zsp.jpg";
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //设置照片保存路径
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(path)));
        startActivityForResult(cameraIntent,REQUEST_CODE_IMAGE);

    }
    @Event(R.id.btn3Id)
    private void takeVedio(View view){

        path=Environment.getExternalStorageDirectory()+"/asd.mp4";
        Intent vedioIntent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        vedioIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
        vedioIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(path)));
        startActivityForResult(vedioIntent,REQUEST_CODE_VIDEO);
    }

    @Event(R.id.btn2Id)
    private void upLoad(View view){
        if (path==null){
            return;}
        RequestParams params=new RequestParams(uploadUrl);
        params.addParameter("name","zsp");
        params.addParameter("imgFile",new File(path));
        Log.d("zsp","111111");

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("zsp","2222");
                Toast.makeText(getApplicationContext(),"upload ok",1).show();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d("zsp","3333");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                Log.d("zsp","4444");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==REQUEST_CODE_IMAGE&&resultCode==RESULT_OK){
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(path,options);
            options.inSampleSize= (int) Math.max(1f*options.outWidth/imageview.getMeasuredWidth(),
                    1f*options.outHeight/imageview.getMeasuredHeight());
            options.inJustDecodeBounds=false;
            imageview.setImageBitmap(BitmapFactory.decodeFile(path,options));
        }else if (requestCode==REQUEST_CODE_VIDEO&&resultCode==RESULT_OK){
            //获取视频中某一帧，借助于原媒体文件控件
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource(path);
            //获取录制视频的事件，单位毫秒
            String timeTxt=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long time=Long.parseLong(timeTxt)/1000;
            if (time<10){
                setTitle("录制时间太短，不足十秒");
                //删除文件
                new File(path).delete();
                path=null;
                return;
            }
            Bitmap bitmap=retriever.getFrameAtTime(2000);
            imageview.setImageBitmap(bitmap);
            retriever.release();
            if (playbutton!=null){
                playbutton.setVisibility(View.VISIBLE);
            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Event(R.id.playBtn1Id)
    private void playVideo(View view){
        videoview.setVideoPath(path);
        videoview.start();
        videoview.setVisibility(View.VISIBLE);
        playbutton.setVisibility(View.GONE);
        imageview.setVisibility(View.GONE);

    }
    @Event(R.id.videoId)
    private void pause(View view){
        if (videoview.isPlaying()){
            videoview.pause();
            imageview.setVisibility(View.VISIBLE);
            playbutton.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
