package com.fxj.crashdemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;



public class CrashHandler implements UncaughtExceptionHandler {
	
	private static final String tag="com.fxj.crashdemo.CrashHandler";
	/*crash��¼��Ϣ�ļ���·��*/
	private static final String path=Environment.getExternalStorageDirectory().getPath()+"/CrashDemo/log/";
	/**crash�ļ���*/
	private static final String FileName="crash";
	/**crash�ļ���׺*/
	private static final String FileNameSuffix=".txt";
	
	/**CrashHandler��������*/
	private static CrashHandler sInstance=new CrashHandler();
	
	private UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;
	/**������*/
	private Context mContext;
	
	
	public CrashHandler() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**��ȡCrashHandler����������ģʽ*/
	public static CrashHandler getInstance()
	{
		Log.i(tag,"CrashHandler#getInstance");
		
		if(sInstance==null){
			sInstance=new CrashHandler();
		}
		return sInstance;
	}

	public void init(Context context)
	{
		Log.i(tag,"CrashHandler#init");
		
		mDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		/*���߳������쳣������*/
		Thread.setDefaultUncaughtExceptionHandler(this);
		mContext = context.getApplicationContext();
	}
	
	/**
	 * ��������:uncaughtException(Thread thread, Throwable ex)
	 * ����˵��:UncaughtExceptionHandler�ӿ���δʵ�ֵ�uncaughtException
	 * 		  �ķ���������δ�����쳣����ʱϵͳ�ͻ����uncaughtException������
	 * ��������:thread---����δ�����쳣���̣߳�ex----δ�����쳣
	 * */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.i(tag,"CrashHandler#uncaughtException");
		
        /*���쳣��Ϣ������SD����*/ 
		dumpExceptionToSDCard(ex);
		/*���쳣��Ϣ�ϴ���������*/
		uploadExceptionToServer(ex);
		
		if(this.mDefaultUncaughtExceptionHandler!=null){
			this.mDefaultUncaughtExceptionHandler.uncaughtException(thread, ex);
			System.out.println(tag+",ʹ��ϵͳĬ�ϵ��쳣�������������쳣!");
			Log.i(tag,"ʹ��ϵͳĬ�ϵ��쳣�������������쳣!");
		}else{
			Process.killProcess(Process.myPid());
			System.out.println(tag+",��ǰ�����Լ�������ǰ����!");
			Log.i(tag,"��ǰ�����Լ�������ǰ����!");
		}
	}
	
	/**���쳣��Ϣ������SD����*/
	private void dumpExceptionToSDCard(Throwable ex) {
		
		/*���SD���Ƿ����*/
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Log.i(tag,"SD��������");
			return;
		} 
		Log.i(tag,"SD������");
		
		File fileDir=new File(path);
		if(!fileDir.exists()){
			/*��ָ��·�����ļ��в�����ʱ����ָ�����ļ���*/
			fileDir.mkdirs();
		}
		
		long current=System.currentTimeMillis();
		String time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
		Log.i(tag,"�쳣����ʱ��:"+time);
		File file=new File(path+FileName+time+FileNameSuffix);
        
		try {			
			if(!file.exists()){
				file.createNewFile();
			}
			
			PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.write(time);
			dumpPhoneInfo(pw);
			pw.println();
			/*���쳣�ĸ���ջ��Ϣ�����ָ���������*/
			ex.printStackTrace(pw);
//			System.out.println(ex);
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			Log.i(tag,"dump crash infomation failed!");
		}
	}
	/**�����ֻ���Ϣ
	 * @throws NameNotFoundException */
	private void dumpPhoneInfo(PrintWriter pw) {
		PackageManager pm=mContext.getPackageManager();
		try {
			PackageInfo pi=pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
			pw.println();
			pw.println("APP Version:"+pi.versionName+"_"+pi.versionCode);
			pw.println("Androidϵͳ�汾��OS Version:"+Build.VERSION.RELEASE+"_"+Build.VERSION.SDK_INT);
			pw.println("�ֻ�������Vendor:"+Build.MANUFACTURER);
			pw.println("�ֻ��ͺ�Model:"+Build.MODEL);
			pw.println("CPU�ܹ�CPU ABI:"+Build.CPU_ABI);			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.i(tag,"NameNotFoundException");
			e.printStackTrace();
		}
		
	}

	/**���쳣��Ϣ�ϴ���������*/
	private void uploadExceptionToServer(Throwable ex) {
		// TODO Auto-generated method stub
		
	}

}
