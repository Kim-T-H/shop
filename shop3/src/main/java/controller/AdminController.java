package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import exception.LoginException;
import logic.Mail;
import logic.ShopService;
import logic.User;

/*
 * AdminController 클래스의 모든 메서드는 admin 으로 로그인 한 경우만 처리 가능함.
 * 1.로그인 안된 경우: 로그인 후 가능합니다. login.shop 페이지 이동
 * 2.관리자 로그인이 아닌 경우 : 관리자만 가능한 거래입니다. main.shop 페이지 이동   => AdminAspect 클래스 구현하기
 * 
 */




@RequestMapping("admin") 
@Controller
public class AdminController {
	@Autowired
	private ShopService service;
	
	@GetMapping("list")
	public ModelAndView list(HttpSession session) {
		ModelAndView mav= new ModelAndView();
		List<User> list = service.getlistAll();
		System.out.println(list);
		mav.addObject("list", list); //    "list"   :     list.jsp 의  forEach items 의 "${list}"
		
		return mav;
		
	}
	
	@RequestMapping("mailForm")
	public ModelAndView mailForm(String[] idchks, HttpSession session) {
		ModelAndView mav= new ModelAndView("admin/mail");
		if(idchks==null || idchks.length==0) {
			throw new LoginException("메일을 보낼 대상자를 선택하세요","list.shop");
		}
		//list : 선택된 userid의 해당하는 User 객체들
		List<User> list=service.userList(idchks);
		mav.addObject("list",list);
		return mav;
	}
	
	@RequestMapping("mail")
	public ModelAndView mail(Mail mail, HttpSession session) {
		ModelAndView mav= new ModelAndView("/alert");
		mailSend(mail);
		mav.addObject("msg","메일 전송이 완료 되었습니다.");
		mav.addObject("url","list.shop"); 
		return mav;
	}
	
	private final class MyAuthenticator extends Authenticator{
		private String id;
		private String pw;
		public MyAuthenticator(String id, String pw) {
			this.id= id;
			this.pw = pw;
		}
		
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(id,pw);
		}
	}
	
	private void mailSend(Mail mail) {
		//네이버 메일 전송을 위한 인증 객체
		MyAuthenticator auth=new MyAuthenticator(mail.getNaverid(),mail.getNaverpw());
		//메일 전송을 위한 환경 변수 설정
		Properties prop= new Properties();
		
		try {
			FileInputStream fis= new FileInputStream("C:\\Users\\GDJ24\\git\\shop\\shop3\\src\\main\\resources\\mail.properties");
			prop.load(fis);	//mail.properties  의 내용을 Properties(Map) 객체로  
			prop.put("mail.smtp.user", mail.getNaverid());
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		//메일 전송을 위한 객체
		Session session=Session.getInstance(prop,auth);
		//메일의 내용을 저장하기 위한 객체
		MimeMessage mimeMsg = new MimeMessage(session);
		try {
			//보내는 사람 설정
			mimeMsg.setFrom(new InternetAddress(mail.getNaverid()+"@naver.com"));
			List<InternetAddress> addrs=new ArrayList<InternetAddress>();
			//홍길동<hong@aaa.bbb>, 김삿갓<kim@aaa.bbb>,..
			String[] emails = mail.getRecipient().split(",");
			/*
			 * new String(email.getBytes("utf-8"),"8859_1")
			 * email.getBytes("utf-8"): email 문자열을 byte[] 형태로 변경.  utf -8 문자로 인식
			 * 
			 * 8859_1: byte [] 배열을 8859_1로 변경하여 문자열로 생성 => 수신된 메일에서 한글이름 유지되도록 설정
			 * 
			 */
			
			
			
			for(String email: emails) {
				try {
					addrs.add(new InternetAddress(new String(email.getBytes("utf-8"),"8859_1")));
				}catch(UnsupportedEncodingException ue) {
					ue.printStackTrace();
				}
			}
			InternetAddress[] arr=
					new InternetAddress[emails.length];
			for(int i=0; i<addrs.size();i++) {
				arr[i]= addrs.get(i);
			}
			mimeMsg.setSentDate(new Date());  //보낸 일자
			mimeMsg.setRecipients(Message.RecipientType.TO, arr);		// 받는사람
			mimeMsg.setSubject(mail.getTitle());		//제목
			MimeMultipart multipart = new MimeMultipart();  
			MimeBodyPart message=new MimeBodyPart();
			
			message.setContent(mail.getContents(),mail.getMtype());
			multipart.addBodyPart(message);
			
			//첨부파일 부분
			for(MultipartFile mf: mail.getFile1()) {
				if((mf !=null) && (!mf.isEmpty())) {
					multipart.addBodyPart(bodyPart(mf));
				}
			}
			mimeMsg.setContent(multipart);
			Transport.send(mimeMsg);
			
		} catch(MessagingException me) {
			me.printStackTrace();
		}
		
	}

	private BodyPart bodyPart(MultipartFile mf) {
		MimeBodyPart body= new MimeBodyPart();
		
		//업로드된 파일의 이름
		String orgFile= mf.getOriginalFilename();
		String path="d:/20200224/spring/mailupload/";
		File f = new File(path);
		if(!f.exists()) f.mkdirs();
		File f1=new File(path+orgFile);		//업로드된 내용을 저장하는 파일
		try {
			mf.transferTo(f1);	//업로드 완성
			body.attachFile(f1);	//메일에 첨부
			body.setFileName(new String(orgFile.getBytes("UTF-8"),"8859_1"));	//첨부파일이름설정
		}catch(Exception e) {
			e.printStackTrace();
		}
		return body;
	}
	
}
