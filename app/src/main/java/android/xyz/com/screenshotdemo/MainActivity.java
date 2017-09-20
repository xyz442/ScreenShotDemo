package android.xyz.com.screenshotdemo;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.xyz.com.detectorlibrary.RxScreenShot;

import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity {
    private final static String TAG = "xyz－MainActivity";

    ImageView imageView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imageView);
        textView = (TextView)findViewById(R.id.path);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RxPermissions.getInstance(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        // All requested permissions are granted
                        Log.d(TAG, "permissions are granted");
                        RxScreenShot.getInstance(this).startObservable()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
//                                .throttleFirst(1800, TimeUnit.MILLISECONDS)
                                .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
                                .subscribe(new Subscriber<String>() {
                                    @Override
                                    public void onCompleted() {
                                        System.out.println("RxScreenshot-onCompleted");
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(String path) {
                                        Log.d(TAG, "观察者: call,截图路径："+path);
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                textView.setText(""+path);
                                                Glide.with(MainActivity.this).load(path).into(imageView);
                                            }
                                        });


                                    }
                                });
                    } else {
                        // At least one permission is denied
                        Log.d(TAG, "permissions are not granted");
                    }
                });
    }
}
