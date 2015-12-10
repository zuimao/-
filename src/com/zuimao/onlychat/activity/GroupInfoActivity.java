package com.zuimao.onlychat.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zuimao.onlychat.MyApplication;
import  com.zuimao.onlychat.R;
import com.zuimao.onlychat.TLSHelper;
import com.zuimao.onlychat.adapter.GroupMemberAdapter;
import com.zuimao.onlychat.c2c.UserInfo;
import com.zuimao.onlychat.c2c.UserInfoManager;
import com.zuimao.onlychat.c2c.UserInfoManagerNew;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.zuimao.onlychat.utils.Constant;
import com.zuimao.onlychat.view.MyGridView;
import com.tencent.TIMCallBack;
import com.tencent.TIMGroupAddOpt;
import com.tencent.TIMGroupDetailInfo;
import com.tencent.TIMGroupManager;
import com.tencent.TIMGroupMemberInfo;
import com.tencent.TIMGroupMemberRoleType;
import com.tencent.TIMGroupReceiveMessageOpt;
import com.tencent.TIMGroupSelfInfo;
import com.tencent.TIMManager;
import com.tencent.TIMValueCallBack;


 public class GroupInfoActivity  extends MyBaseActivity {
	private final static String TAG = GroupInfoActivity.class.getSimpleName();
	private  ArrayList<String> mListMember;
	private MyGridView mGVMembers;
	private Button mBtnQuitGroup;
	public GroupMemberAdapter mAdapter;
	private TextView memberNum;
	private TextView groupOwner;	
	private String mStrGroupID;
	private String mStrGroupName;
	private String 	mStrGroupType;
	private String mStrGroupIntroduce;
	private String mStrGroupNotification;
	private TextView groupName;
	private TextView tvGroupIntroduce;
	private TextView tvGroupID;
	private TextView tvGroupNotification;
	private String mOwerID;
	//private ToggleButton tbMsgOpt;
	public final static int MSG_NOT_RECEIVE = 1;
	public final static int MSG_RECEIVE_AND_NOTIFY = 2;
	public final static int MSG_RECEIVE_NOT_NOTIFY = 3;
	
	public final static int ADD_FORBID = 1;
	public final static int ADD_ALLOW_ANY = 2;
	public final static int ADD_NEED_CONFIRM= 3;	
	private int mMsgReceiveOpt;
	private  int groupAddOpt;
	//public final static
	 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_info);	
		 
		initView();	
	}
	
	public void initView() {	
	
		mGVMembers= (MyGridView)findViewById(R.id.gv_members);	
		mListMember = new ArrayList<String>();
		mAdapter = new GroupMemberAdapter(getBaseContext(),mListMember);
		mGVMembers.setAdapter(mAdapter);			
		groupName = (TextView) findViewById(R.id.tv_group_name);
		tvGroupIntroduce = (TextView)findViewById(R.id.tv_group_introduce);
		tvGroupNotification = (TextView)findViewById(R.id.tv_group_notification);
		memberNum = (TextView) findViewById(R.id.tv_member_num);
		groupOwner = (TextView) findViewById(R.id.tv_group_owner);
		mStrGroupID = getIntent().getStringExtra("groupID");
		mStrGroupName = getIntent().getStringExtra("groupName");
		mStrGroupType = UserInfoManagerNew.getInstance().getGroupID2Info().get(mStrGroupID).getType();
		tvGroupID = (TextView) findViewById(R.id.tv_group_id);
		tvGroupID.setText(mStrGroupID);
		
		initReceiveMessgaeOpt();
		if(mStrGroupType.equals(Constant.TYPE_PUBLIC_GROUP)){
			initJoinStyle();
		}

	    mBtnQuitGroup = (Button)findViewById(R.id.btn_quit_group);
	    mBtnQuitGroup.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub		
                TIMGroupManager.getInstance().quitGroup(mStrGroupID, new TIMCallBack() {
                    @Override
                    public void onError(int code, String desc) {
                    	Toast.makeText(getBaseContext(), "quit group error:" + code + ":" + desc, Toast.LENGTH_SHORT).show();
                    	Log.e(TAG,  "quit group error:" + code + ":" + desc);
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "quit group succ");
//						Map<String,String> groupID2Name = UserInfoManager.getInstance().getGroupID2Name();
//						groupID2Name.remove(mStrGroupID);
                        
                        UserInfoManagerNew.getInstance().UpdateGroupID2Name(mStrGroupID,mStrGroupName,mStrGroupType,false);
                        finish();
            			runOnUiThread(new Runnable() {
        					public void run(){
        						Intent intent = new Intent(GroupInfoActivity.this,MainActivity.class); 
        						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        						startActivity(intent);
        				}
        			});	
                    }
                });
			}
	    	
	    });
	    
	    mGVMembers.setOnItemClickListener(new OnItemClickListener() {  
	    	public void onItemClick(AdapterView<?> arg0,
	    	                                  View arg1,
	    	                                  int arg2,
	    	                                  long arg3
	    	                                  ) {   
	    	    String userName =(String) arg0.getItemAtPosition(arg2);
	    	    if(userName.equals(Constant.INVITE_FRIEND_TO_GROUP)){
	    	    	Intent intent = new Intent(GroupInfoActivity.this,InviteToGroupActivity.class);
	    	    	intent.putStringArrayListExtra("members", mListMember);
	    	    	intent.putExtra("groupID", mStrGroupID);
	    	    	intent.putExtra("groupName", mStrGroupName);
	    	    	startActivity(intent);
	    	    }
	    	    
	    	    if(userName.equals(Constant.DELETE_GROUP_MEMBER)){
	    	    	Intent intent = new Intent(GroupInfoActivity.this,DeleteGroupMemberActivity.class);
	    	    	intent.putStringArrayListExtra("members", mListMember);
	    	    	intent.putExtra("groupID", mStrGroupID);
	    	    	intent.putExtra("groupName", mStrGroupName);
	    	    	startActivity(intent);
	    	    }	    	    
	    	      
	    	}
	    });
	    

	}	
	
	private void initReceiveMessgaeOpt(){
		RelativeLayout rl_msg_opt_set = (RelativeLayout) findViewById(R.id.rl_msg_opt_set);
		rl_msg_opt_set.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
    	    	Intent intent = new Intent(GroupInfoActivity.this,ReceiveMsgOptActivity.class);    	    	
    	    	intent.putExtra("groupID", mStrGroupID);
    	    	intent.putExtra("currentOPT",mMsgReceiveOpt);
    	    	Log.d(TAG,"mMsgReceiveOpt:" + mMsgReceiveOpt);
    	    	startActivity(intent);			
			}
	    	
	    });	
	}
	
	private void initJoinStyle(){		
		RelativeLayout rl_join_style_set = (RelativeLayout) findViewById(R.id.rl_join_style_set);
		rl_join_style_set.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
    	    	Intent intent = new Intent(GroupInfoActivity.this,GroupJoinStyleActivity.class);    	    	
    	    	intent.putExtra("groupID", mStrGroupID);
    	    	intent.putExtra("currentOPT",groupAddOpt);
    	    	Log.d(TAG,"groupAddOpt:" + groupAddOpt);
    	    	startActivity(intent);			
			}
	    	
	    });	
	}
	private void modifyGroupName(){
	    RelativeLayout rlGroupName = (RelativeLayout) findViewById(R.id.rl_group_name);
	    rlGroupName.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
    	    	Intent intent = new Intent(GroupInfoActivity.this,EditGroupNameActivity.class);    	    	
    	    	intent.putExtra("groupID", mStrGroupID);
    	    	intent.putExtra("groupName", mStrGroupName);
    	    	startActivity(intent);			
			}
	    	
	    });	
	}

	private void modifyGroupIntroduce(){
	    RelativeLayout rlGroupIntroduce = (RelativeLayout) findViewById(R.id.rl_group_introduce);
	    rlGroupIntroduce.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
    	    	Intent intent = new Intent(GroupInfoActivity.this,EditGroupIntroduceActivity.class);    	    	
    	    	intent.putExtra("groupID", mStrGroupID);
    	    	intent.putExtra("groupIntroduce", mStrGroupIntroduce);
    	    	startActivity(intent);			
			}
	    	
	    });			
	}
	
	private void modifyGroupNotification(){
	    RelativeLayout rlGroupNotification = (RelativeLayout) findViewById(R.id.rl_group_notification);
	    rlGroupNotification.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
    	    	Intent intent = new Intent(GroupInfoActivity.this,EditGroupNotificationActivity.class);    	    	
    	    	intent.putExtra("groupID", mStrGroupID);
    	    	intent.putExtra("groupNotification", mStrGroupNotification);
    	    	startActivity(intent);			
			}
	    	
	    });			
	}	
	
	private void manageGroupMember(){
		View vv = (View)findViewById(R.id.v_member_manage);
		vv.setVisibility(View.VISIBLE);
	    RelativeLayout rlMemberManage = (RelativeLayout) findViewById(R.id.rl_member_manage);
	    rlMemberManage.setVisibility(View.VISIBLE);
	    rlMemberManage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
    	    	Intent intent = new Intent(GroupInfoActivity.this,GroupMemberManageActivity.class);    	
    	    	intent.putStringArrayListExtra("members", mListMember);
    	    	intent.putExtra("groupID", mStrGroupID);
    	    	startActivity(intent);			
			}
	    	
	    });	
	}
	
	@Override
	public void onResume() {		
		super.onResume();			
		Log.d(TAG,"activity resume ,refresh list" + mStrGroupID);
		loadGroupMember(mStrGroupID);		
	}	
	
	
	private void loadGroupMember(String groupID){
		List<String> lGroup = new ArrayList<String>();
		lGroup.add(groupID);
        
		TIMGroupManager.getInstance().getGroupDetailInfo(lGroup,new TIMValueCallBack<List<TIMGroupDetailInfo>>() {
            @Override
            public void onError(int code, String desc) {
                Log.e(TAG, "get gruop detailinfo failed: " + code + " desc");
            }
      
			@Override
			public void onSuccess(List<TIMGroupDetailInfo> arg0) {
				// TODO Auto-generated method stub
				if(arg0.size() != 1){
					Log.e(TAG,"result size error:" + arg0.size() );
					return;
				}
				TIMGroupDetailInfo info = arg0.get(0);
				
				groupName.setText(info.getGroupName());
				tvGroupIntroduce.setText(info.getGroupIntroduction());
				tvGroupNotification.setText(info.getGroupNotification());	
				mOwerID = info.getGroupOwner();
				TIMGroupAddOpt addOpt  =info.getGroupAddOpt();
				if(addOpt == TIMGroupAddOpt.TIM_GROUP_ADD_FORBID){
					groupAddOpt = ADD_FORBID;
				}else if(addOpt == TIMGroupAddOpt.TIM_GROUP_ADD_ANY){
					groupAddOpt = ADD_ALLOW_ANY;
				}else if(addOpt == TIMGroupAddOpt.TIM_GROUP_ADD_AUTH){
					groupAddOpt = ADD_NEED_CONFIRM;
				}	
				String strName;
				if(info.getGroupOwner().equals(TLSHelper.userID)){
					strName = UserInfoManagerNew.getInstance().getNickName();
				}else{
					com.zuimao.onlychat.c2c.UserInfo user= UserInfoManagerNew.getInstance().getContactsList().get(info.getGroupOwner());
					strName = (user!=null?user.getDisplayUserName():info.getGroupOwner());
				}
			
				groupOwner.setText(strName);
            	memberNum.setText(String.valueOf(info.getMemberNum()) + "/" + Constant.MAX_GROUP_MEMBER_NUM);
            	mGVMembers.setVisibility(View.VISIBLE);
            	mAdapter.notifyDataSetChanged(); 	   
            	mStrGroupName = info.getGroupName();
            	mStrGroupIntroduce = info.getGroupIntroduction();
            	mStrGroupNotification = info.getGroupNotification();
            	
        		if(mOwerID==null || mOwerID.equals(TLSHelper.userID)){
        			modifyGroupName();
        		}
            	
			}

        });
		
		TIMGroupManager.getInstance().getGroupMembers(groupID,new TIMValueCallBack<List<TIMGroupMemberInfo>>(){

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				  Log.e(TAG, "get gruop member failed: " + arg0 + " arg1");
			}

			@Override
			public void onSuccess(List<TIMGroupMemberInfo> arg0) {
				// TODO Auto-generated method stub
		    	mListMember.clear();   
		    	TIMGroupMemberRoleType  myRole = TIMGroupMemberRoleType.Normal;
		    	for(int i=0;i<arg0.size();i++){
		    		mListMember.add(arg0.get(i).getUser());		    		
		    		if(arg0.get(i).getUser().equals(TLSHelper.userID)){
		    			myRole=arg0.get(i).getRole();
		    		}
		    		//Log.d(TAG,"group member:" + arg0.get(i).getUser());
		    	}
		    	Log.d(TAG,"getGroupMembers succ:" + mStrGroupType);
		    	//只有讨论组才能拉人
		    	if(mStrGroupType.equals(Constant.TYPE_PRIVATE_GROUP)){
		    		mListMember.add(Constant.INVITE_FRIEND_TO_GROUP);	
		    	}
		    	//管理员和创建者可以踢人.可以修改群资料和管理群成员
		    	if( (myRole == TIMGroupMemberRoleType.Admin) || (myRole == TIMGroupMemberRoleType.Owner) ) {
		    		mListMember.add(Constant.DELETE_GROUP_MEMBER);
		    		modifyGroupNotification();
		    		modifyGroupIntroduce();
		    		manageGroupMember();
		    	}
		    	
	        	mGVMembers.setVisibility(View.VISIBLE);
	        	mAdapter.notifyDataSetChanged(); 
			}
			
		});
		
		TIMGroupManager.getInstance().getSelfInfo(groupID, new TIMValueCallBack<TIMGroupSelfInfo>(){

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.e(TAG,"getSelfInfo error:" + arg0 + ":" + arg1); 
			}

			@Override
			public void onSuccess(TIMGroupSelfInfo arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG,"mMessageOpt succ:" + arg0.getRecvOpt());
				TIMGroupReceiveMessageOpt mMessageOpt = arg0.getRecvOpt();
				if(mMessageOpt == TIMGroupReceiveMessageOpt.NotReceive){
					mMsgReceiveOpt = MSG_NOT_RECEIVE;
				}else if(mMessageOpt == TIMGroupReceiveMessageOpt.ReceiveAndNotify){
					mMsgReceiveOpt = MSG_RECEIVE_AND_NOTIFY;
				}else if(mMessageOpt == TIMGroupReceiveMessageOpt.ReceiveNotNotify){
					mMsgReceiveOpt = MSG_RECEIVE_NOT_NOTIFY;
				}				
			}			
		});
		
	//	TIMGroupManager.getInstance().getGroupPublicInfo(lGroup, arg1)
	}
	
	public void onBack(View view){	
		finish();	
	}
	

}
