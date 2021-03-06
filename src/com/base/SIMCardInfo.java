package com.base;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * class name：SIMCardInfo<BR>
 * class description：读取Sim卡信息<BR>
 * PS： 必须在加入各种权限 <BR>
 * Date:2012-3-12<BR>
 * 
 * @version 1.00
 * @author CODYY)peijiangping
 */
public class SIMCardInfo {
	/**
	 * TelephonyManager提供设备上获取通讯服务信息的入口。 应用程序可以使用这个类方法确定的电信服务商和国家 以及某些类型的用户访问信息。
	 * 应用程序也可以注册一个监听器到电话收状态的变化。不需要直接实例化这个类
	 * 使用Context.getSystemService(Context.TELEPHONY_SERVICE)来获取这个类的实例。
	 */
	private static TelephonyManager telephonyManager;
	private static Map<String, String> simMap = new  HashMap<String, String>();
	
	/**
	 * 国际移动用户识别码
	 */
	private String IMSI=null;
	
	private static SIMCardInfo simCardInfo = null;
	
	public static SIMCardInfo getInstance(){
		if(simCardInfo==null)
			simCardInfo = new SIMCardInfo();
		return simCardInfo;
	}
	
	public SIMCardInfo() {}
	
	public static void init(Context context){
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * Role:获取当前设置的电话号码
	 * <BR>Date:2012-3-12
	 * <BR>@author CODYY)peijiangping
	 */
	public String getNativePhoneNumber() {
		String NativePhoneNumber=null;
		NativePhoneNumber=telephonyManager.getLine1Number();
		return NativePhoneNumber;
	}

	/**
	 * Role:Telecom service providers获取手机服务商信息 <BR>
	 * 需要加入权限<uses-permission
	 * android:name="android.permission.READ_PHONE_STATE"/> <BR>
	 * Date:2012-3-12 <BR>
	 * 
	 * @author CODYY)peijiangping
	 */
	public String getProvidersName() {
		String ProvidersName = null;
		// 返回唯一的用户ID;就是这张卡的编号神马的
		IMSI = telephonyManager.getSubscriberId();
		// IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
	//	System.out.println(IMSI);
		if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
			ProvidersName = "中国移动";
		} else if (IMSI.startsWith("46001")) {
			ProvidersName = "中国联通";
		} else if (IMSI.startsWith("46003")) {
			ProvidersName = "中国电信";
		}
		return ProvidersName;
	}
	
	public Map<String, String> getSimMap(){
		
		simMap.put("Line1Number", telephonyManager.getLine1Number() );
		simMap.put("IMSI", telephonyManager.getSubscriberId() );
		simMap.put("getDeviceId", telephonyManager.getDeviceId());
		simMap.put("NetworkOperatorName", telephonyManager.getNetworkOperatorName());
		simMap.put("SimOperatorName", telephonyManager.getSimOperatorName() );
		simMap.put("SimSerialNumbe", telephonyManager.getSimSerialNumber() );
		simMap.put("CellLocation", telephonyManager.getCellLocation().toString() );
		simMap.put("SimCountryIso", telephonyManager.getSimCountryIso() );
		
		return simMap;
	}

	public String getIMSI() {
		if(IMSI==null)
			IMSI = telephonyManager.getSubscriberId();
		return IMSI;
	}

	public void setIMSI(String iMSI) {
		IMSI = iMSI;
	}
}