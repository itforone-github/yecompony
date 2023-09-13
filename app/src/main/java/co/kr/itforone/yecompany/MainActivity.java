package co.kr.itforone.yecompany;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.os.StrictMode;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;

import util.BackPressCloseHandler;
import util.Common;
import util.LocationPosition;
import util.NetworkCheck;

public class MainActivity extends AppCompatActivity {
    LinearLayout webLayout;
    RelativeLayout networkLayout;
    public WebView webView;
    NetworkCheck netCheck;
    Button replayBtn;

    public static boolean execBoolean = true;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    private final int AUDIO_RECORED_REQ_CODE=1500,SNS_REQ_CODE=2000;
    String firstUrl = "";
    final int REQUEST_IMAGE_CODE = 1010;
    Context mContext;
    Activity mActivity;

    private Uri cameraImageUri;
    public static String no;
    int gpsCount=0;
    static final int PERMISSION_REQUEST_CODE = 1;

    String[] PERMISSIONS = {
            "android.permission.READ_PHONE_STATE"
    };

    /*
    // cpp 관련 부분. 지금은 필요하지 않음
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
        System.loadLibrary("imported-lib");
    }
    */

    private boolean hasPermissions(String[] permissions) {
        // 퍼미션 확인
        int result = -1;
        for (int i = 0; i < permissions.length; i++) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[i]);
        }
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;

        } else {
            return false;
        }
    }


    private void requestNecessaryPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                //퍼미션을 거절했을 때 메시지 출력 후 종료
                if (!hasPermissions(PERMISSIONS)) {

                }else{
                    LocationPosition.act=MainActivity.this;
                    LocationPosition.setPosition(this);
                    if(LocationPosition.lng==0.0){
                        LocationPosition.setPosition(this);
                    }
                    String place= LocationPosition.getAddress(LocationPosition.lat,LocationPosition.lng);
                    webView.loadUrl("javascript:getAddress('"+place+"')");
                }
                return;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //mHandler.sendEmptyMessage(0);
        mContext=this;
        mActivity=this;
        //스크린샷 했을 때 빈화면이 나오게
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        //화면을 계속 켜짐
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        LocationPosition.act=MainActivity.this;
        LocationPosition.setPosition(this);
        if(LocationPosition.lng==0.0){
            LocationPosition.setPosition(this);
        }

        Intent intent = getIntent();
        firstUrl=getString(R.string.url);
        try{
            if(!intent.getExtras().getString("goUrl").equals("")){
                firstUrl =intent.getExtras().getString("goUrl");
            }
        }catch(Exception e){

        }
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }



        setLayout();

        Intent startIntent = new Intent(MainActivity.this,SplashActivity.class);
        startActivity(startIntent);
    }


    //레이아웃 설정
    public void setLayout() {
        networkLayout = (RelativeLayout) findViewById(R.id.networkLayout);//네트워크 연결이 끊겼을 때 레이아웃 가져오기
        webLayout = (LinearLayout) findViewById(R.id.webLayout);//웹뷰 레이아웃 가져오기

        webView = (WebView) findViewById(R.id.webView);//웹뷰 가져오기
        Log.d("url",firstUrl);
        webView.loadUrl(firstUrl);
        webViewSetting();



    }
    //갤러리 온클릭리스너 만들기

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    public void webViewSetting() {

        WebSettings setting = webView.getSettings();//웹뷰 세팅용

        setting.setAllowFileAccess(true);//웹에서 파일 접근 여부
        setting.setAppCacheEnabled(true);//캐쉬 사용여부
        setting.setGeolocationEnabled(true);//위치 정보 사용여부
        setting.setDatabaseEnabled(true);//HTML5에서 db 사용여부
        setting.setDomStorageEnabled(true);//HTML5에서 DOM 사용여부
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);//캐시 사용모드 LOAD_NO_CACHE는 캐시를 사용않는다는 뜻
        setting.setJavaScriptEnabled(true);//자바스크립트 사용여부
        setting.setSupportMultipleWindows(false);//윈도우 창 여러개를 사용할 것인지의 여부 무조건 false로 하는 게 좋음
        setting.setUseWideViewPort(true);//웹에서 view port 사용여부
        webView.setWebChromeClient(chrome);//웹에서 경고창이나 또는 컴펌창을 띄우기 위한 메서드
        webView.setWebViewClient(client);//웹페이지 관련된 메서드 페이지 이동할 때 또는 페이지가 로딩이 끝날 때 주로 쓰임
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setting.setTextZoom(100);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 혼합된 컨텐츠 허용//
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        setting.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Mobile Safari/537.36"+"/APP");
        webView.addJavascriptInterface(new WebJavascriptEvent(), "Android");

        //네트워크 체킹을 할 때 쓰임
        netCheck = new NetworkCheck(this, this);
        netCheck.setNetworkLayout(networkLayout);
        netCheck.setWebLayout(webLayout);
        netCheck.networkCheck();
        //뒤로가기 버튼을 눌렀을 때 클래스로 제어함
        backPressCloseHandler = new BackPressCloseHandler(this);

        replayBtn=(Button)findViewById(R.id.replayBtn);
        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                netCheck.networkCheck();
            }
        });
    }

    WebChromeClient chrome;
    {
        chrome = new WebChromeClient() {
            //새창 띄우기 여부
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return false;
            }

            //경고창 띄우기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(true)
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                }).create().show();
                return true;
            }

            //컴펌 띄우기
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.cancel();
                                    }
                                }).create().show();
                return true;
            }

            //현재 위치 정보 사용여부 묻기
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Should implement this function.
                final String myOrigin = origin;
                final GeolocationPermissions.Callback myCallback = callback;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Request message");
                builder.setMessage("Allow current location?");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, true, false);
                    }

                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, false, false);
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }


        };
    }

    WebViewClient client;
    {
        client = new WebViewClient() {
            //페이지 로딩중일 때 (마시멜로) 6.0 이후에는 쓰지 않음
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d("url",url);

                if (url.startsWith("tel")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.


                        }
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                }else if(url.startsWith("https://open")){

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    startActivity(intent);
                    return true;
                }else if(url.startsWith("http://pf.kakao.com/")){

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    startActivity(intent);
                    return true;
                }else if(url.startsWith("market://")){
                    try {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        startActivity(intent);
                        return true;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if (url.startsWith("intent:")) {

                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            getBaseContext().startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        Log.d("error1",e.toString());
                        e.printStackTrace();
                    }
                }
                /*
                if(LocationPosition.lng!=0.0){
                    LocationPosition.act=MainActivity.this;
                    LocationPosition.setPosition(LocationPosition.act);
                    if(0<url.indexOf("?")) {
                        webView.loadUrl(url + "&currentLat=" + LocationPosition.lat + "&currentLng=" + LocationPosition.lng);
                    }else{
                        webView.loadUrl(url + "?currentLat=" + LocationPosition.lat + "&currentLng=" + LocationPosition.lng);
                    }
                }*/

                return false;
            }
            //페이지 로딩이 다 끝났을 때
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //webLayout.setRefreshing(false);

                LocationPosition.act=MainActivity.this;
                LocationPosition.setPosition( LocationPosition.act);
                Log.d("url",url);
                /* cookie */
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
                Log.d("mb_id",Common.getPref(MainActivity.this,"ss_mb_id",""));
/*
                if(0<=url.lastIndexOf("co_id=order")){
                    if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
                        requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
                    } else {
                        //이미 사용자에게 퍼미션 허가를 받음.
                        String place= LocationPosition.getAddress(LocationPosition.lat,LocationPosition.lng);
                        webView.loadUrl("javascript:getAddress('"+place+"')");

                    }
                }*/
                //로그인할 때
                if(url.startsWith(getString(R.string.domain)+"bbs/login.php")||url.startsWith(getString(R.string.domain)+"bbs/register_form.php")){
                    view.loadUrl("javascript:fcmKey('"+ Common.TOKEN+"')");
                }
                if (url.startsWith(getString(R.string.url)+"index.php") || url.startsWith(getString(R.string.domain)+"/currentLat")||url.equals(getString(R.string.url)+"bbs/board.php?bo_table=short")||url.startsWith(getString(R.string.domain)+"bbs/login.php")) {
                    isIndex=true;
                } else {
                    isIndex=false;
                }
                if (hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
                    try {
                        if (Common.TOKEN.equals("") || Common.TOKEN.equals(null)) {
                            refreshToken();
                        } else {
                            view.loadUrl("javascript:setToken('" + Common.TOKEN + "','" + Common.getMyDeviceId(MainActivity.this) + "')");
                        }
                    }catch(Exception e){
                        refreshToken();
                    }
                }
                if(url.startsWith(getString(R.string.domain)+"bbs/map_search.php")){
                    Log.d("url",getString(R.string.domain)+"bbs/map_search.php");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

//Show
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                }

            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //super.onReceivedError(view, request, error);
                //view.loadUrl("");
                //페이지 오류가 났을 때 오류메세지 띄우기
                /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                builder.show();*/
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    //다시 들어왔을 때
    @Override
    protected void onResume() {
        super.onResume();
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }



        execBoolean=true;
        Log.d("newtork","onResume");
        try{
            Intent intent=getIntent();
            Uri data=intent.getData();
            Log.d("data111",data.toString());
        }catch (Exception e){

        }

        //netCheck.networkCheck();
    }
    //홈버튼 눌러서 바탕화면 나갔을 때
    @Override
    protected void onPause() {
        super.onPause();
        /* cookie */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }





        execBoolean=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        netCheck.stopReciver();
        // unregisterReceiver(receiver);


    }
    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함
        Log.d("isIndex",isIndex+"");
        if(!isIndex) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else if (webView.canGoBack() == false) {
                backPressCloseHandler.onBackPressed();
            }
        }else{
            backPressCloseHandler.onBackPressed();
        }
    }
    //로그인 로그아웃
    class WebJavascriptEvent{


        @JavascriptInterface
        public void setLogin(String mb_id){
            Log.d("login","로그인");
            Common.savePref(getApplicationContext(),"ss_mb_id",mb_id);
        }
        @JavascriptInterface
        public void setLogout(){
            Log.d("logout","로그아웃");
            Common.savePref(getApplicationContext(),"ss_mb_id","");
        }

        @JavascriptInterface
        public void getLocation(){
            try {
                if (LocationPosition.lng != 0.0) {
                    LocationPosition.act = MainActivity.this;
                    LocationPosition.setPosition(LocationPosition.act);
                }
                Log.d("lat", LocationPosition.lat + "");
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build();
                StrictMode.setThreadPolicy(policy);
                StrictMode.enableDefaults();
                final String address = LocationPosition.getAddress(LocationPosition.lat, LocationPosition.lng);
                final String url = getString(R.string.url);
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(url+"?start_place="+address+"&end_place=&start_lat="+LocationPosition.lng+"&start_lng="+LocationPosition.lat+"&start=true&end=false");
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    /*
        Handler mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                LocationPosition.act=MainActivity.this;
                LocationPosition.setPosition(LocationPosition.act);
                if(LocationPosition.lng!=0.0){
                    mHandler.removeMessages(0);
                    //Toast.makeText(mContext, "gps 위치를 잡았습니다. 웹뷰를 새로 고침합니다.", Toast.LENGTH_SHORT).show();

                    if(0<webView.getUrl().indexOf("?")) {
                        webView.loadUrl(webView.getUrl() + "&currentLat=" + LocationPosition.lat + "&currentLng=" + LocationPosition.lng);
                    }else{
                        webView.loadUrl(webView.getUrl() + "?currentLat=" + LocationPosition.lat + "&currentLng=" + LocationPosition.lng);
                    }
                }else{
                    if(gpsCount==0)
                        //Toast.makeText(mContext, "gps 위치를 잡고 있습니다.", Toast.LENGTH_SHORT).show();
                    gpsCount++;

                    mHandler.sendEmptyMessageDelayed(0,1000);

                }
            }
        };*/
    private void refreshToken(){
    }
}