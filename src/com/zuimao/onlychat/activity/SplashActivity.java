package com.zuimao.onlychat.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tencent.tls.platform.TLSErrInfo;
import tencent.tls.platform.TLSRefreshUserSigListener;
import tencent.tls.platform.TLSUserInfo;

import com.zuimao.onlychat.MyApplication;
import com.zuimao.onlychat.R;
import com.zuimao.onlychat.TLSHelper;
import com.zuimao.onlychat.c2c.HttpProcessor;
import com.zuimao.onlychat.c2c.UserInfoManager;
import com.zuimao.onlychat.c2c.UserInfoManagerNew;
import com.zuimao.onlychat.utils.Constant;
import com.tencent.TIMCallBack;
import com.tencent.TIMGroupBaseInfo;
import com.tencent.TIMGroupManager;
import com.tencent.TIMManager;
import com.tencent.TIMUser;
import com.tencent.TIMValueCallBack;
import com.tencent.tls.activity.HostLoginActivity;
import com.tencent.tls.service.Constants;
import com.tencent.tls.service.TLSService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class SplashActivity extends Activity {
	private Handler mHandler;
	private  StringBuilder mStrRetMsg;	
	private static final String TAG = SplashActivity.class.getSimpleName();
	public final static int GO_TO_LOGINNEW = 1;
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_splash);
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initView();
	}

	public void initView() {
			mHandler = new Handler(Looper.getMainLooper());
			mHandler.postDelayed(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub			
						if(MyApplication.getInstance().bHostRelaytionShip){
							Log.d(TAG,"init:" + TLSHelper.userID + ":"+ TLSService.getInstance().needLogin(TLSHelper.userID));
							if(TLSHelper.userID!= null && (!TLSService.getInstance().needLogin(TLSHelper.userID)) ){								
								goRefreshTicket();
							}else{
								goLoginNewActivity();
							}
						}else{
							goLoginActivity();
						}
					}				
			}, 1000);
		
	}
	
	public void goLoginNewActivity() {
		Log.d(TAG,"start activity");
	    Intent intent = new Intent(SplashActivity.this, HostLoginActivity.class);
	    intent.putExtra(Constants.EXTRA_THIRDAPP_PACKAGE_NAME_SUCC, "com.zuimao.onlychat");
	    intent.putExtra(Constants.EXTRA_THIRDAPP_CLASS_NAME_SUCC, "com.zuimao.onlychat.activity.MainActivity");
      //  startActivityForResult(intent, GO_TO_LOGINNEW);
	    startActivity(intent);
        finish();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG,"acitivy result:" + requestCode);
	} 
	
	public void goRefreshTicket(){
        // 刷新票据必须实现RefreshUserSigListener接口
		TLSService.getInstance().refreshUserSig(TLSHelper.userID,new TLSService.RefreshUserSigListener() {
            @Override
            public void onUserSigNotExist() {
            	Log.e(TAG,"onUserSigNotExist");
            	goLoginNewActivity();
            }

            @Override
            public void OnRefreshUserSigSuccess(TLSUserInfo tlsUserInfo) {
            //	LoginToIMServer((Intent)null);
        		Intent intent = new Intent();
        		intent.setClass(SplashActivity.this, MainActivity.class);
        		startActivity(intent);    
        		finish();
            }

            @Override
            public void OnRefreshUserSigFail(TLSErrInfo tlsErrInfo) { // 刷票失败
				Log.e(TAG,"刷票失败:" + tlsErrInfo.ErrCode + ":" + tlsErrInfo.Msg);
				goLoginNewActivity();
            }

            @Override
            public void OnRefreshUserSigTimeout(TLSErrInfo tlsErrInfo) { // 刷票超时
				Log.e(TAG,"刷票超时:" + tlsErrInfo.ErrCode + ":" + tlsErrInfo.Msg);
				goLoginNewActivity();
            }
        });
	}
	
	public void goLoginActivity() {
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
	//	finish();
	}	

	  
//	 private void LoginToIMServer(Intent data){
//		String userID ;
//		String userSig;
//		TIMUser user = new TIMUser();
//		//tls_sdk 更新了id
//		userID = TLSService.getInstance().getLastUserIdentifier();		
//		user.setAccountType(String.valueOf(Constant.ACCOUNT_TYPE));
//		user.setAppIdAt3rd(String.valueOf(Constant.SDK_APPID));
//		userSig = TLSService.getInstance().getUserSig(userID);		
//		TLSHelper.userID = userID;
//
//		Log.d(TAG,userID + ":" + userSig);
//		user.setIdentifier(userID);
//		
//		 //发起到IM后台登录请求
//       TIMManager.getInstance().login(Constant.SDK_APPID,user,userSig,
//        			new TIMCallBack() {//回调接口
//                    @Override
//                    public void onSuccess() {//登录成功
//                        Log.d(TAG, "login succ");  	                        
//                        
//        				if(MyApplication.getInstance().bHostRelaytionShip){
//        					UserInfoManagerNew.getInstance().getGroupListFromServer();
//        					UserInfoManagerNew.getInstance().getSelfProfile();
//            				UserInfoManagerNew.getInstance().getContactsListFromServer();
//            				UserInfoManagerNew.getInstance().getBlackListFromServer();
//        				}                				
//        				
//                		Log.d(TAG,"start main activity");		
//                		Intent intent = new Intent();
//                		intent.setClass(SplashActivity.this, MainActivity.class);
//                		startActivity(intent); 
//                	
//                	}
//
//                    @Override
//                    public void onError(int code, String desc) {
//                    	Toast.makeText(getBaseContext(), "IMServer登录失败:" + code + ":" + desc, Toast.LENGTH_SHORT).show();
//                        Log.e(TAG, "login imserver failed. code: " + code + " errmsg: " + desc);	
//                        goLoginNewActivity();
//                       
//                    }
//                });	 
//	 }
}
