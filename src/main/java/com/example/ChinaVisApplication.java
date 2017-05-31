package com.example;

import static org.assertj.core.api.Assertions.setMaxElementsForPrinting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.gson.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.jam.mutable.MPackage;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.PublicImpl;
import org.json.JSONObject;
import org.mockito.BDDMockito.BDDStubber;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.dao.MessageDao;
import com.example.dao.TextDao;
import com.example.domain.Message;
import com.example.domain.MyDate;
import com.example.domain.News;
import com.example.domain.Text;
import com.example.domain.User;
import com.example.service.UserService;
import com.fasterxml.jackson.core.JsonParser;

@RestController
@SpringBootApplication
public class ChinaVisApplication {
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
	static MyDate early;
	static MyDate late;
	@Autowired
	private UserService userService;
	@Autowired
	private MessageDao messageDao;
	@Autowired
	private TextDao textDao;
	@RequestMapping("/")
	public ModelAndView map(){
		ModelAndView mv=new ModelAndView("index");
		return mv;
	}
	@RequestMapping("/china")
	public void china() throws FileNotFoundException{
		File root = new File("C:/Users/rain/Desktop/data"); //创建文件对象
		File files[]=root.listFiles();
		Map<String, String> map=new HashMap<>();
		for(File file:files){
			Scanner x=new Scanner(file);
			x.nextLine();
			while(x.hasNextLine()){
				String m=x.nextLine();
				m+=x.nextLine();
				int first=m.indexOf("\"");
				int last=m.lastIndexOf("\"");
				if(first<=0||first>=last){
					System.out.println(m);
					continue;
				}
				String md5=m.substring(0, first-1);
				String content=m.substring(first+1,last);
				String left=m.substring(last+2);
				String s[]=left.split(",");
				messageDao.insertMessage(new Message(md5, s[0], new Timestamp(Long.parseLong(s[1])), new Timestamp(Long.parseLong(s[2])), Double.parseDouble(s[3]), Double.parseDouble(s[4])));
				if(!map.containsKey(md5)){
					map.put(md5, content);
					Text text=new Text();
					text.setMd5(md5);
					text.setContent(content);
					textDao.insertText(text);
				}
			}
			System.out.println(map.size());
		}
	}
	public static void main(String[] args) {
		SpringApplication.run(ChinaVisApplication.class, args);
	}
}
