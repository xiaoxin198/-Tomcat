package com.yc.web.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yc.tomcat.TomcatConstants;


/**
 * 基于http协议的请求处理类
 * @author xiaoxin
 * @date 2020年8月21日
 */
public class HttpServletRequest implements ServletRequest {
	private String method; // 请求方式
	private Map<String, String> parameter = new HashMap<String, String>(); 
	private String url; //请求的资源地址
	private InputStream is; //请求流
	private String protocalVersion;
	private BufferedReader read;

	private volatile HttpSession session = new HttpSession();
	private boolean checkJSessionId = false;
	private String jsessionid;
	private Cookie[] cookies;

	public HttpServletRequest(InputStream is) {
		this.is=is;
	}
	
	/**
	 * 解析请求头
	 */

	@Override
	public void parse() {
		try {
			read = new BufferedReader(new InputStreamReader(is));
			String line = null;
			List<String> headStrs = new ArrayList<String>();
			

			while((line = read.readLine()) != null && !"".equals(line)) {  // 读到空行说明请求头结束
				headStrs.add(line);
			}
			parseFristLine(headStrs.get(0));//解析起始行
			parseParameter(headStrs);//解析获取参数
	

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	/**
	 * 解析起始行
	 * @param string
	 */

	private void parseFristLine(String str) {
		String[] strs = str.split(" ");
		this.method = strs[0];
		if(strs[1].contains("?")) {
			this.url = strs[1].substring(0,strs[1].indexOf("?"));
		}else {
			this.url = strs[1];
		}
		this.protocalVersion = strs[2];

	}
	/**
	 * 解析获取的参数
	 * @param headStrs
	 */

	private void parseParameter(List<String> headStrs) {
		String str = headStrs.get(0).split(" ")[1];//获取请求地址
		if(str.contains("?")) {
			str = str.substring(str.indexOf("?")+1);//获取请求参数
			String[] params = str.split("&");
			String[] temp;
			for(String param : params) {
				temp = param.split("=");
				this.parameter.put(temp[0], temp[1]);
			}
		}
		if(TomcatConstants.REQUEST_METHOD_POST.equals(this.method)) {
			int len = 0;
			for(String head : headStrs) { //获取头部协议Content-Length
				if(head.contains("Content-Length:")) {
					len = Integer.parseInt(head.substring(head.indexOf(":")+1).trim());
					System.out.println(len);

					break;
				}
			}
			if(len <= 0) { //说明post后面没有数据
				return;
			}
			
			//如果要处理文件上传，则还需要获取Content-Type的头部字段
			try {
				char[] ch = new char[1024 * 10];
				int count = 0, total = 0;
				StringBuffer sbf = new StringBuffer(1024 * 10);
				while((count = read.read(ch))>0) {
					sbf.append(ch,0,count);
					total +=count;
					if(total >= len) {
						break;
					}
				}
				

				str = URLDecoder.decode(sbf.toString(),"utf-8");
				str = str.substring(str.indexOf("?") + 1);
				
				String[] params = str.split("&");
				String[] temp;
				for(String param : params) {
					temp = param.split("=");
					this.parameter.put(temp[0], temp[1]);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(this.parameter);
		

	}

	

	@Override
	public String getParameter(String key) {
		return this.parameter.getOrDefault(key, null);
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public Cookie[] getCookies() {
		return this.cookies;
	}

	@Override
	public boolean checkJSessionId() {
		return this.checkJSessionId;
	}

	@Override
	public String getJSessionId() {
		return this.jsessionid;
	}
	public String getProtocalVersion() {
		return protocalVersion;
	}



}
