package android.xyz.com.detectorlibrary;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by xieyizhi on 17/9/18.
 */

public class RxScreenShot {

    private static final String TAG = "xyz-RxScreenshot";
    private static final String EXTERNAL_CONTENT_URI_MATCHER =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
    private static final String[] PROJECTION = new String[] {
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED
    };
    private final String SORT_ORDER = MediaStore.Images.Media.DATE_ADDED + " DESC";
    private final long DEFAULT_DETECT_WINDOW_SECONDS = 10;

    private final ContentResolver mContentResolver;

    private static RxScreenShot instance = null;
        public static RxScreenShot getInstance(Context context) {
            if (instance == null) {
                instance = new RxScreenShot(context);
            }
            return instance;
        }
    private RxScreenShot(Context context) {

        mContentResolver = context.getApplicationContext().getContentResolver();
    }
    private boolean matchPath(String path) {
        return path.toLowerCase().contains("screenshot") || path.contains("截屏") ||
                path.contains("截图");
    }

    private boolean matchTime(long currentTime, long dateAdded) {
        return Math.abs(currentTime - dateAdded) <= DEFAULT_DETECT_WINDOW_SECONDS;
    }
    final ContentObserver contentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "onChange: " + selfChange + ", " + uri.toString());
            if (uri.toString().startsWith(EXTERNAL_CONTENT_URI_MATCHER)) {
                Cursor cursor = null;
                try {
                    cursor = mContentResolver.query(uri, PROJECTION, null, null,
                            SORT_ORDER);
                    if (cursor != null && cursor.moveToFirst()) {
                        String path = cursor.getString(
                                cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        long dateAdded = cursor.getLong(cursor.getColumnIndex(
                                MediaStore.Images.Media.DATE_ADDED));
                        long currentTime = System.currentTimeMillis() / 1000;
                        Log.d(TAG, "path: " + path + ", dateAdded: " + dateAdded +
                                ", currentTime: " + currentTime);
                        if (matchPath(path) && matchTime(currentTime, dateAdded)) {
                            if(mOnChangeListener!=null){
                                mOnChangeListener.onChange(path);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "open cursor fail");
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            super.onChange(selfChange, uri);
        }
    };
    Observable obs = Observable.create(new Observable.OnSubscribe<Object>() {
        @Override
        public void call(final Subscriber<? super Object> subscriber) {
            Log.d(TAG, "Observable: call");
            try {
                if (!subscriber.isUnsubscribed()) {

                    setOnChangeListener(new OnChangeListener() {
                        @Override
                        public void onChange(String path) {
                            Log.d(TAG, "onNext: call");
                            subscriber.onNext(path);
                        }
                    });
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }
    }).doOnUnsubscribe(new Action0() {
        @Override
        public void call() {
            cancelListener();
            Log.d(TAG, "doOnUnsubscribe: call");
        }
    });


    public void setOnChangeListener(OnChangeListener listener){
        mContentResolver.registerContentObserver(//注册监听对象，作为被观察者
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
        mOnChangeListener = listener;
    }
    public void cancelListener(){//注销监听对象
        mContentResolver.unregisterContentObserver(contentObserver);
        mOnChangeListener = null;
    }
    protected OnChangeListener mOnChangeListener;

    /**
     * 改变监听
     */
    public interface OnChangeListener {
        void onChange(String path);
    }


    //被观察者
    Observable.OnSubscribe<String> integerOnSubscribe = new Observable.OnSubscribe<String>() {
        @Override
        public void call(final Subscriber<? super String> subscriber) {
//            subscriber.onNext("1");
//            subscriber.onNext("2");
//            subscriber.onNext("3");
//            subscriber.onCompleted();
            try {
                Log.d(TAG, "subscriber.isUnsubscribed()="+subscriber.isUnsubscribed());
                if (!subscriber.isUnsubscribed()) {
                    // MyObject will take care of calling onNext(), onError() and onCompleted()
                    // on the subscriber.

//                    subscriber.add(new MainThreadSubscription() {
//                        @Override protected void onUnsubscribe() {
//                            cancelListener();
//                            Log.d(TAG, "subscriber.add:MainThreadSubscription call");
//                        }
//                    });
                    setOnChangeListener(new OnChangeListener() {
                        @Override
                        public void onChange(String path) {//
                            Log.d(TAG, "onNext: call");
                            subscriber.onNext(path);
                        }
                    });
                    subscriber.add(Subscriptions.create(new Action0() {
                        @Override
                        public void call() {
                            cancelListener();
                            Log.d(TAG, "subscriber.add: Action0 call");
                        }
                    }));


                }
            } catch (Exception e) {
                Log.d(TAG, "onError: call");
                subscriber.onError(e);
            }
        }
    };

    //观察者
    Subscriber<String> integerSubscriber = new Subscriber<String>() {
        @Override
        public void onCompleted() {
            System.out.println("onCompleted");
        }

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(String i) {

            Log.d(TAG, "观察者: call"+i);
            System.out.println(i);
        }
    };

    public void dingYue(){
        //订阅
        Observable.create(integerOnSubscribe)
                .subscribe(integerSubscriber);
    }
    public Observable startObservable() {
        Log.d(TAG, "startObservable: "+integerOnSubscribe);
        return Observable.create(integerOnSubscribe);
    }
}
