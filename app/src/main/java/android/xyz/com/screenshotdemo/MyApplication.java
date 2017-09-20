package android.xyz.com.screenshotdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import rx.Subscription;


/**
 * Created by xyz on 2017/3/3.
 */

public class MyApplication extends Application {

    private final static String TAG = "xyz－MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "------onCreate------");
        registerActivityLifecycleCallback();
    }

    Subscription subscription;
    /**
     * 监听app是否在前端运行
     */
    private void registerActivityLifecycleCallback() {
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStopped(Activity activity) {
                if (subscription != null)
                    subscription.unsubscribe();
                Log.d(TAG, "------Switch to the background------");
            }

            @Override
            public void onActivityStarted(final Activity activity) {
                Log.d(TAG, "------Switch to the front------");
//                RxPermissions.getInstance(activity)
//                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        .subscribe(granted -> {
//                            if (granted) {
//                                // All requested permissions are granted
//                                Log.d(TAG, "permissions are granted");
//                                subscription = RxScreenShot.getInstance(getApplicationContext()).startObservable().subscribeOn(Schedulers.io())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .throttleFirst(1800, TimeUnit.MILLISECONDS)
////                            .compose(this.bindUntilEvent(ActivityEvent.PAUSE))
//                                        .subscribe(new Subscriber<String>() {
//                                            @Override
//                                            public void onCompleted() {
//                                                System.out.println("RxScreenshot-onCompleted");
//                                            }
//
//                                            @Override
//                                            public void onError(Throwable e) {
//
//                                            }
//
//                                            @Override
//                                            public void onNext(String path) {
//                                                Intent it = new Intent("NeedFeekback");
//                                                it.putExtra("ImagePath",path);
//                                                sendBroadcast(it);
//                                                Log.d(TAG, "观察者: call"+path);
//                                            }
//                                        });
//                            } else {
//                                // At least one permission is denied
//                                Log.d(TAG, "permissions are not granted");
//                            }
//                        });



            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }
        });
    }


}
