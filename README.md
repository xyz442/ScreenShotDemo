# ScreenShotDemo
ScreenShotDemo

-------------------

## 原理分析
Android系统并没有提供截屏通知相关的API，需要我们自己对Android系统媒体数据库进行监测，因为使用系统截屏截取一张图片都会把这张图片的详细信息加入到这个媒体数据库，并发出内容改变通知，因此监听媒体数据库的变化，就可以得到一个截屏通知然后判断该图片符合特定的规则，符合则认为被截屏了。然后利用RxJava订阅事件的变化，就比较方便了。


## 第一步：定义监听对象，确定触发监听条件

>直接上代码说
``` 
```
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
```
## 第二步：设置注册和监听接口

public void setOnChangeListener(OnChangeListener listener){
        mContentResolver.registerContentObserver(//注册监听对象，作为被观察者
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
        mOnChangeListener = listener;
    }
```
```
public void cancelListener(){//注销监听对象        	    mContentResolver.unregisterContentObserver(contentObserver);
        mOnChangeListener = null;
    }
```

## 第三步：确定被观察者
```
//被观察者
Observable.OnSubscribe<String> integerOnSubscribe = new Observable.OnSubscribe<String>() {
        @Override
        public void call(final Subscriber<? super String> subscriber) {
            try {
                Log.d(TAG, "subscriber.isUnsubscribed()="+subscriber.isUnsubscribed());
                if (!subscriber.isUnsubscribed()) {
                    setOnChangeListener(new OnChangeListener() {//设置监听
                        @Override
                        public void onChange(String path) {//回调变化
                            Log.d(TAG, "onNext: call");
                            subscriber.onNext(path);
                        }
                    });
                    subscriber.add(Subscriptions.create(new Action0() {//
                        @Override
                        public void call() {//解除绑定订阅
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
```

## 第四步：创建被观察者对象
```
public Observable startObservable() {
        Log.d(TAG, "startObservable: "+integerOnSubscribe);
        return Observable.create(integerOnSubscribe);
    }
```


## 第五步：连接绑定观察者和被观察者，订阅
这一步就是使用了，值得注意的是，在使用的时候需要申请Manifest.permission.WRITE_EXTERNAL_STORAGE权限，
这里使用了RxPermissions来动态申请权限，在onResume的时候订阅截屏监听，当页面退到后台的时候利用了.compose(this.bindUntilEvent(ActivityEvent.PAUSE))解绑订阅，防止内存泄漏，也需求的需要吧！
```
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
```
http://blog.csdn.net/XYZ442/article/details/78049032
