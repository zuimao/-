package com.zuimao.onlychat.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zuimao.onlychat.MyApplication;
import com.zuimao.onlychat.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.zuimao.onlychat.adapter.RecentListAdapter;
import com.zuimao.onlychat.utils.Constant;
import com.zuimao.onlychat.utils.RecentEntity;
import com.zuimao.onlychat.activity.ChatNewActivity;
import com.zuimao.onlychat.c2c.UserInfo;
import com.zuimao.onlychat.c2c.UserInfoManager;
import com.zuimao.onlychat.c2c.UserInfoManagerNew;
import com.tencent.TIMCallBack;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMGroupBaseInfo;
import com.tencent.TIMGroupDetailInfo;
import com.tencent.TIMGroupManager;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMMessageStatus;
import com.tencent.TIMValueCallBack;

@SuppressLint("NewApi") public class RecentFragment extends Fragment {

	private static String TAG = RecentFragment.class.getSimpleName();
	private List<RecentEntity> entitys;
	private static long conversation_num=0;
	private RecentListAdapter adapter;
	private ListView recentList;
	private boolean mHidden = false;
//	private boolean fromChatActivity = false;
	private int backPos = 0;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fg_recent, container, false);		
		
		getChatList(view);
		
		return view;
	}
	//初始化消息列表
	public void getChatList(View view) {
		
		recentList = (ListView) view.findViewById(R.id.list_recent);	
		entitys = new ArrayList<RecentEntity>();
		adapter = new RecentListAdapter(getActivity(),entitys);		
		recentList.setAdapter(adapter);	//为消息列表填充适配器
		recentList.setVisibility(View.GONE);		

		//列表点击事件
		recentList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
				final RecentEntity entity = (RecentEntity) adapter.getItem(position);					 
				    Intent intent = new Intent(getActivity(), ChatNewActivity.class);
				    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				    if(entity.getIsGroupMsg()){
				    	intent.putExtra("chatType", ChatNewActivity.CHATTYPE_GROUP);
				    	intent.putExtra("groupID", entity.getName());
				    	intent.putExtra("groupName", entity.getNick());
				    	
				    	//intent.putExtra("groupType", entity.getNick());
				    }else{
				    	intent.putExtra("chatType", ChatNewActivity.CHATTYPE_C2C);
				    	intent.putExtra("userName", entity.getName());
				    }	
				    intent.putExtra("itemPos", position);
				    startActivityForResult(intent,1);
			}
		});
		//列表长按事件
		recentList.setOnItemLongClickListener(new OnItemLongClickListener () {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				 final RecentEntity entity = (RecentEntity) adapter.getItem(position);
			       Dialog alertDialog = new AlertDialog.Builder(getActivity()). 
			                setTitle("确定删除？"). 
			                setMessage("确定删除该会话吗？"). 			       
			                setNegativeButton("确定", new DialogInterface.OnClickListener() { 
			                     
			                    @Override 
			                    public void onClick(DialogInterface dialog, int which) { 
			                        // TODO Auto-generated method stub  
			                    	boolean result = TIMManager.getInstance().deleteConversation(entity.getMessage().getConversation().getType(),entity.getMessage().getConversation().getPeer());
			                    	Log.d(TAG,"delete result:"+ result);
			                    	if(result){
			                    		loadRecentContent();
			                    		adapter.notifyDataSetChanged();	
			                    	}
			                    } 
			                }). 
			                setPositiveButton("取消", new DialogInterface.OnClickListener() { 
			                     
			                    @Override 
			                    public void onClick(DialogInterface dialog, int which) { 
			                        // TODO Auto-generated method stub  
			                    } 
			                }). 			               
			                create(); 
			        alertDialog.show(); 	
				return true;
			}
		});		
	}	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==0){
			if(data!=null){
				backPos = data.getIntExtra("itemPos", 0);
				Log.d(TAG,"onActivityResult" + backPos);
			}
		}
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.mHidden = hidden;
		if (!mHidden) {
			loadRecentContent();
		}
	}
	
	@Override
	public void onResume() {		
		super.onResume();	
		if (!mHidden) {
			Log.d(TAG,"activity resume ,refresh list");
			if(MainActivity.bLogin ){
				//登陆成功拉取消息
				loadRecentContent();
			}
		//	fromChatActivity = false;
		}		
	}	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}	
	
	//加载聊天内容
	public void loadRecentContent(){			
		conversation_num = TIMManager.getInstance().getConversationCount();//获取会话个数		
		Log.d(TAG,"loadRecentContent begin:" + conversation_num);
		recentList.setVisibility(View.GONE);
		entitys.clear();
		//遍历会话列表
//		if(conversation_num > Constant.RECENT_MSG_NUM){
//			conversation_num = Constant.RECENT_MSG_NUM;
//		}
		for(long i = 0; i < conversation_num; ++i) 	{	
			//根据索引获取会话
			final TIMConversation conversation = TIMManager.getInstance().getConversationByIndex(i);
			Log.d(TAG,"loadRecentContent:" + i + ":" + conversation.getType() + ":" + conversation.getUnreadMessageNum());
			//判断会话类型是否为系统消息
			if(conversation.getType() == TIMConversationType.System){
				UserInfoManagerNew.getInstance().setUnReadSystem(conversation.getUnreadMessageNum());
				Log.d(TAG,"setUnReadSystem:" + conversation.getUnreadMessageNum());
				continue;
			}
			//获取聊天记录
	        conversation.getMessage(5, null, new TIMValueCallBack<List<TIMMessage>>() {
	            @Override
	            public void onError(int code, String desc) {
	                Log.e(TAG, "get msgs failed");
	            }

	            @Override
	            public void onSuccess(List<TIMMessage> msgs) {       
	            	if(msgs.size()<1){
	            		return;
	            	}          
            		TIMMessage msg0 =null;
            		for(TIMMessage msg:msgs){
            			//判断消息是否被标记为删除
            			if(msg.status() != TIMMessageStatus.HasDeleted){
            				msg0 = msg;
            				break;
            			}
            		}
            		//判断会话类型是否为群消息
	            	if(conversation.getType() == TIMConversationType.Group){
	            		ProcessGroupMsg(msg0);	            			
	            	}else{
						ProcessC2CMsg(msg0);
	            	}
	            }
	        });			
		}	
	}
	
	private void ProcessC2CMsg(TIMMessage msg){
		if(msg==null)
			return;
		final TIMMessage tmpMsg = msg;
		final TIMConversation conversation = msg.getConversation();		
		String strUserName = conversation.getPeer();//获取会话的对方帐号/昵称
    	RecentEntity entity = new RecentEntity();//消息实体
    	entity.setMessage(tmpMsg);	            	
    	entity.setName(strUserName);	            		
		entity.setIsGroupMsg(false);
    	UserInfo userInfo ;
    	//if(MyApplication.getInstance().bHostRelaytionShip){
    		userInfo = UserInfoManagerNew.getInstance().getContactsList().get(strUserName);
//    	}else{
//    		userInfo = UserInfoManager.getInstance().getContactsList().get(strUserName);
//    	}
    	if(userInfo != null){
    		entity.setNick(userInfo.getNick()); 
    	}            		            	
    	entity.setCount(conversation.getUnreadMessageNum());    
    	//activity初始化或者新消息通知都有可能调用到这里，且顺序不确定。 数据可能重复，需要去重
     	Log.d(TAG,"c2c msg:" + strUserName + "|unreadNum:" + conversation.getUnreadMessageNum() +"|entitys size:" +entitys.size());
       	for(int i=0;i<entitys.size();i++){       		
       		if(entitys.get(i).getName().equals(strUserName)){
      			if( entitys.get(i).getMessage().timestamp() < tmpMsg.timestamp()){
       				entitys.remove(i);
       				break;
       			}else{
       				return;
       			}	         			
       		}
       	}	
        entitys.add(entity); 	
    	recentList.setVisibility(View.VISIBLE);
    	adapter.notifyDataSetChanged();	
			
	}
	
	private void ProcessGroupMsg(TIMMessage msg){
		if(msg==null)
			return;
		final TIMMessage tmpMsg =msg;
		final TIMConversation conversation = msg.getConversation();
		final String strGroupID = conversation.getPeer();
    //	Log.d(TAG,"grp msg:" + strGroupID );		
		
       	for(int i=0;i<entitys.size();i++){
       		if(entitys.get(i).getName().equals(strGroupID)){	           			
       			if( entitys.get(i).getMessage().timestamp() < tmpMsg.timestamp()){
       				entitys.remove(i);
       				break;
       			}else{
       				return;
       			}	    			
       		}       		
       	}     

		if(!UserInfoManagerNew.getInstance().getGroupID2Info().containsKey(strGroupID)){
	       	//群组关系可能发生了变化，比如被加入了新群后发送的消息。更新一下
			Log.d(TAG,"grp recent msg, no name,may be change " + strGroupID);			
            TIMGroupManager.getInstance().getGroupList(new TIMValueCallBack<List<TIMGroupBaseInfo>>() {
                @Override
                public void onError(int code, String desc) {
                    Log.e(TAG, "get gruop list failed: " + code + " desc");                  
                }
          
    			@Override
    			public void onSuccess(List<TIMGroupBaseInfo> arg0) {
    				// TODO Auto-generated method stub
    				//缓存群列表。生成群id和群名称的对应关系
    				UserInfoManagerNew.getInstance().getGroupID2Info().clear();
    				UserInfoManagerNew.getInstance().getPrivateGroupID2Name().clear();
    				UserInfoManagerNew.getInstance().getPublicGroupID2Name().clear();
    				UserInfoManagerNew.getInstance().getChatRoomID2Name().clear();
//    				Map<String,String> mGroup = new HashMap<String,String>();
    				for(TIMGroupBaseInfo baseInfo :arg0){
    					//mGroup.put(baseInfo.getGroupId(),baseInfo.getGroupName());
    					UserInfoManagerNew.getInstance().UpdateGroupID2Name(baseInfo.getGroupId(),baseInfo.getGroupName(), baseInfo.getGroupType(), true);
    				}
//    				UserInfoManagerNew.getInstance().setGroupID2Name(mGroup);    				
    				
    		       	RecentEntity entity = new RecentEntity();
    		    	entity.setMessage(tmpMsg);	            	
    		    	entity.setName(strGroupID);	            		
    				entity.setIsGroupMsg(true);
    				if(UserInfoManagerNew.getInstance().getGroupID2Info().containsKey(strGroupID)){
    					entity.setNick(UserInfoManagerNew.getInstance().getGroupID2Info().get(strGroupID).getName());
    		    	}else{
    		    		entity.setNick("");
    		    		Log.e(TAG,"can't get group nmae" + strGroupID);
    		    	}
    		        entity.setCount(conversation.getUnreadMessageNum());
    		        entitys.add(entity);
    		    	recentList.setVisibility(View.VISIBLE);
    		    	adapter.notifyDataSetChanged();  	
    			}
            });           			
			return;
		}       
		
		Log.d(TAG,"grp msg:" + strGroupID + ":" + UserInfoManagerNew.getInstance().getGroupID2Info().get(strGroupID) 
				   + "|unreadNum:" + conversation.getUnreadMessageNum() +"|entitys size:" +entitys.size());
       	RecentEntity entity = new RecentEntity();
    	entity.setMessage(tmpMsg);	            	
    	entity.setName(strGroupID);	            		
		entity.setIsGroupMsg(true);
    	entity.setNick(UserInfoManagerNew.getInstance().getGroupID2Info().get(strGroupID).getName());        
        entity.setCount(conversation.getUnreadMessageNum());
        entitys.add(entity);
    	recentList.setVisibility(View.VISIBLE);
    	adapter.notifyDataSetChanged();     
	}
	   	
}
