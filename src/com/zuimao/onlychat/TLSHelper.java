package com.zuimao.onlychat;

import java.util.List;

import tencent.tls.platform.TLSAccountHelper;
import tencent.tls.platform.TLSLoginHelper;
import tencent.tls.platform.TLSUserInfo;
import tencent.tls.report.QLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.zuimao.onlychat.utils.Constant;
import com.tencent.tls.service.TLSConfiguration;
import com.tencent.tls.service.TLSService;

public class TLSHelper {
	private static final String TAG = TLSHelper.class.getSimpleName();

	Context context;
	
	public static String userID = null;
//	public static TLSLoginHelper loginHelper;
//	public static TLSAccountHelper accountHelper;
	static int LANG = 2052;
	static String appVer = "1.0";
	static int country = 86;
	static boolean NoPwdReg = false;
	public final static String PREFIX = "86-";
	public static TLSService tlsService;
	  
    public synchronized boolean init(Context context){
    	
        this.context=context;
        InitTLSSDK();
        return true;
    }
        
	private void InitTLSSDK()
	{

	       // 设置使用TLS SDK所需的配置信息
//        TLSConfiguration.setSdkAppid(Constant.SDK_APPID);        // 必须项, sdkAppid
//        TLSConfiguration.setAccountType(Constant.ACCOUNT_TYPE);           // 必须项, accountType
//        TLSConfiguration.setAppVersion(appVer);          // 可选项, 表示app当前版本, 默认为1.0
//        TLSConfiguration.setTimeout(8000);              // 可选项, 表示网络操作超时时间, 默认为8s
//        
        tlsService = TLSService.getInstance();

        // 设置QQ APP_ID和APP_KEY
   //     TLSConfiguration.setQqAppIdAndAppKey("1104701569", "CXtj4p63eTEB2gSu");
        TLSConfiguration.setQqAppIdAndAppKey("222222", "CXtj4p63eTEB2gSu");
        // 设置微信APP_ID和APP_SECRET
        //TLSConfiguration.setWxAppIdAndAppSecret("wxc05322d5f11ea2b0", "3ace67c5982c6ed8daa36f8911f609d7");
        TLSConfiguration.setWxAppIdAndAppSecret("wx65f71c2ea2b122da", "1d30d40f8db6d3ad0ee6492e62ad5d57");
        
        userID = tlsService.getLastUserIdentifier();        
     
	}
	
		
}
