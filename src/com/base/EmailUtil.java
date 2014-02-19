package com.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.teleframe.teflpr.R;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;

public class EmailUtil extends Authenticator {

	private static String TAG = "com.base.EmailUtil";
	private static Context context = null;

	private static EmailUtil emailUtil = null;

	private String host ="";
	private String userName = "";
	private String passWord = "";
	private int	   port = 21;
	private String toMail = S.EMAIL_SERVER_ADDRESS;


	private Session session;  //邮件会话对象
	private MimeMessage mimeMessage;  //MIME邮件对象

	
	public EmailUtil(){
		this.host = S.EMAIL_HOST;
		this.userName = S.EMAIL_USERNAME;
		this.passWord = S.EMAIL_PASSWORD;
		this.port = 25;
	}
	public EmailUtil(String host, String userName, String passWord, int port){
		this.host = host;
		this.userName = userName;
		this.passWord = passWord;
		this.port = port;
	}

	public static EmailUtil getInstance(){
		if(emailUtil==null)
			emailUtil = new EmailUtil();
		return emailUtil;
	}

	public static void init(Context c){
		context = c;
	}


	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, passWord);
	}


	

	/*
	 * smtphost: smtp.163.com   smtp.126.com 设置系统属性
	 * mailfron: 发送邮件的邮箱地址
	 * mailto: 接受邮件的邮箱地址
	 * mailcc: 抄送人邮箱地址
	 * mailbcc: 密送人邮箱地址
	 * mailauth: true   设置smtp身份认证：mail.smtp.auth = true
	 * mailuser: 发送邮件的邮箱用户名
	 * mailpassword: 发送邮件的邮箱密码
	 * mailsubject: 邮件的主题
	 * mailmessage: 邮件的内容
	 * mailattach:发送附件的文件名，在本地机器上的绝对地址
	 */
	public boolean sendMail(String smtphost,String mailfrom,
			boolean mailauth,String mailuser,String mailpassword,
			String [] mailto,String []mailcc, String []mailbcc,  String mailsubject,
			String mailmessage,String []mailattach) 
					throws Exception {
		Multipart multipart = new MimeMultipart(); //Multipart对象，邮件内容，标题，附件等内容均添加到其中之后再生成//MimeMessage对象 

		Properties props=new java.util.Properties();    

		props.put("mail.smtp.protocol", "smtp");  
		props.put("mail.smtp.auth", "true");//设置要验证  
		props.put("mail.smtp.host", smtphost);//设置host  
		props.put("mail.smtp.port", this.port);  //设置端口  

		System.out.println("Mail Host Address: "+smtphost);

		session = Session.getInstance(props,this); 
		mimeMessage = new MimeMessage(session);

		//设置发信人
		mimeMessage.setFrom(new InternetAddress(mailfrom)); 
		mimeMessage.saveChanges();
		System.out.println("Mail From Address: "+mailfrom);

		//设置收件人
		InternetAddress[] addressTo = new InternetAddress[mailto.length];   
		for (int i = 0; i < mailto.length; i++) {   
			addressTo[i] = new InternetAddress(mailto[i]);  
		}        
		mimeMessage.setRecipients(MimeMessage.RecipientType.TO, addressTo); 
		System.out.println("Mail To   Address: "+addressTo.toString());

		//设置主题
		mimeMessage.setSubject(mailsubject);   
		mimeMessage.setSentDate(new java.util.Date());        // setup message date   

		//设置抄送人
		if(mailcc != null && mailcc.length>0)
		{
			InternetAddress[] addressCC = new InternetAddress[mailcc.length];   
			for (int i = 0; i < mailcc.length; i++) {   
				addressCC[i] = new InternetAddress(mailcc[i]);  
			}        
			mimeMessage.setRecipients(MimeMessage.RecipientType.CC, addressCC); 
			System.out.println("Mail Cc   Address: "+mailcc);
		}

		//设置密送
		if(mailbcc != null && mailbcc.length>0)
		{
			InternetAddress[] addressBCC = new InternetAddress[mailbcc.length];   
			for (int i = 0; i < mailbcc.length; i++) {   
				addressBCC[i] = new InternetAddress(mailbcc[i]);  
			}        
			mimeMessage.setRecipients(MimeMessage.RecipientType.BCC, addressBCC); 
			System.out.println("Mail Cc   Address: "+mailcc);
		}

		mimeMessage.saveChanges();

		//设置文本内容
		BodyPart messageBodyPart = new MimeBodyPart();    
		messageBodyPart.setText(mailmessage);
		multipart.addBodyPart(messageBodyPart); // Put parts in message  

		//添加附件
		if(mailattach != null && mailattach.length>0)
		{
			for (int i = 0; i < mailattach.length; i++) {
				if(mailattach[i]==null || mailattach[i].isEmpty())
					continue;
				MimeBodyPart bp2 = new MimeBodyPart();
				FileDataSource fileds = new FileDataSource(mailattach[i]);
				DataHandler dh = new DataHandler(fileds);
				bp2.setDisposition(Part.ATTACHMENT);
				bp2.setFileName(fileds.getName());
				bp2.setDataHandler(dh);
				multipart.addBodyPart(bp2);
			}
		}

		mimeMessage.setContent(multipart);        // send email  
		mimeMessage.saveChanges();
		try{
			Transport.send(mimeMessage);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		System.out.println("Mail Successfully Sended!");
		
		multipart = null;
		return true;
	}	   


	/**
	 * 发送邮件的方法
	 * @return
	 */
	private boolean sendEmail(){  
		Properties props = new Properties();  
		props.put("mail.smtp.protocol", "smtp");  
		props.put("mail.smtp.auth", "true");//设置要验证  
		props.put("mail.smtp.host", this.host);//设置host  
		props.put("mail.smtp.port", this.port);  //设置端口  
		//		PassAuthenticator pass = new PassAuthenticator();   //获取帐号密码  
		//		Session session = Session.getInstance(props, this.getPasswordAuthentication()); //获取验证会话  
		Session session = Session.getDefaultInstance(props, this);
		try  
		{  
			//配置发送及接收邮箱  
			InternetAddress fromAddress, toAddress;  

			//这个地方需要改成自己的邮箱
			fromAddress = new InternetAddress(this.userName, "自己给自己发");  
			toAddress   = new InternetAddress(this.toMail, "自己接收自己发的邮件");

			//一下内容是：发送邮件时添加附件
			MimeBodyPart attachPart = new MimeBodyPart();  
			FileDataSource fds = new FileDataSource(Environment.getExternalStorageDirectory()+"/crash-fortrun.log"); //打开要发送的文件  

			attachPart.setDataHandler(new DataHandler(fds)); 
			attachPart.setFileName(fds.getName()); 
			MimeMultipart allMultipart = new MimeMultipart("mixed"); //附件  
			allMultipart.addBodyPart(attachPart);//添加  
			//配置发送信息  
			MimeMessage message = new MimeMessage(session);  
			//	            message.setContent("test", "text/plain"); 
			message.setContent(allMultipart); //发邮件时添加附件
			message.setSubject("这次发送仅作测试");  
			message.setFrom(fromAddress);  
			message.addRecipient(javax.mail.Message.RecipientType.TO, toAddress);  
			message.saveChanges();  
			//连接邮箱并发送  
			Transport transport = session.getTransport("smtp");  

			// 这个地方需要改称自己的账号和密码
			transport.connect(this.host, S.EMAIL_USERNAME, S.EMAIL_PASSWORD);  
			transport.send(message);  
			transport.close(); 
		} catch (Exception e) {
			throw new RuntimeException();//将此异常向上抛出，此时CrashHandler就能够接收这里抛出的异常并最终将其存放到txt文件中
			//	        	Log.e("sendmail", e.getMessage());
		}
		return true;  
	} 

}
