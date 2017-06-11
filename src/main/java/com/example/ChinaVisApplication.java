package com.example;

import static org.assertj.core.api.Assertions.setMaxElementsForPrinting;
import static org.assertj.core.api.Assertions.useDefaultRepresentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.formula.functions.Count;
import org.apache.poi.ss.formula.functions.Counta;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jni.Time;
import org.apache.xmlbeans.impl.jam.mutable.MPackage;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.PublicImpl;
import org.json.JSONObject;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.BDDMockito.BDDStubber;
import org.mockito.internal.verification.Times;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.dao.JizhanBitMapDao;
import com.example.dao.JizhanDao;
import com.example.dao.MessageDao;
import com.example.dao.TextDao;
import com.example.dao.TypeAreaTimeDao;
import com.example.dao.TypeTimeAreaDao;
import com.example.domain.CountArea;
import com.example.domain.Jizhan;
import com.example.domain.JizhanBitMap;
import com.example.domain.Message;
import com.example.domain.MyDate;
import com.example.domain.News;
import com.example.domain.Pos;
import com.example.domain.Text;
import com.example.domain.TypeNum;
import com.example.domain.TypeTimeArea;
import com.example.domain.TypeTimeLngLat;
import com.example.domain.User;
import com.example.dto.MyTimestamp;
import com.example.service.MessageService;
import com.example.service.UserService;
import com.fasterxml.jackson.core.JsonParser;

@RestController
@SpringBootApplication
public class ChinaVisApplication {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	static MyDate early;
	static MyDate late;
	@Autowired
	private MessageService messageService;
	@Autowired
	private MessageDao messageDao;
	@Autowired
	private JizhanBitMapDao jizhanBitMapDao;
	@Autowired
	private TypeTimeAreaDao typeTimeAreaDao;
	@Autowired
	private TypeAreaTimeDao typeAreaTimeDao;
	@Autowired
	private JizhanDao jizhanDao;
	static long minTime = 1487088000000L;
	static double jdmaxper10min = 0.17617854625650183052408656562157;
	static double wdmaxper10min = 0.13513513513513513513513513513514;
	static double minLng = 115.52557373;
	static double minLat = 39.46577454;
	static double dLng = 0.14094283700520146441926925249726;
	static double dLat = 0.10810810810810810810810810810811;
	@Autowired
	private TextDao textDao;

	@RequestMapping("/")
	public ModelAndView map() {
		ModelAndView mv = new ModelAndView("index");
		return mv;
	}
	
	
	@RequestMapping("/getMessagesByJizhan")
	public String getMessagesByJizhan(@RequestParam("jizhan") int jizhan) {// 李接口1
		List<Message> messages=jizhanDao.selectMessageByJizhan(jizhan);
		JsonObject result=new JsonObject();
		Map<String, CountArea[][]> dayMessage=new HashMap<>();
		Map<String, boolean[]> judge=new HashMap<>();
		System.out.println(messages.size());
		for(Message message:messages){
			Timestamp time=message.getRecitime();
			int index = time.toLocaleString().indexOf(" ");
			String date = time.toLocaleString().substring(0, index);
			int hour=time.getHours();
			int min=time.getMinutes()/10;
			if(judge.get(date)==null){
				boolean b[]=new boolean[24];
				b[hour]=true;
				judge.put(date, b);
			}else{
				judge.get(date)[hour]=true;
			}
			String area=getAddt(message.getLng(), message.getLat());
			if(area.length()<=0)
				continue;
			if(dayMessage.containsKey(date)){
				CountArea countArea[][]=dayMessage.get(date);
				if(countArea[hour][min]==null){
					CountArea demo=new CountArea();
					demo.setCount(1);
					Set<String> set=new HashSet<>();
					set.add(area);
					demo.setSet(set);
					countArea[hour][min]=demo;
				}else{
					countArea[hour][min].setCount(countArea[hour][min].getCount()+1);
					countArea[hour][min].getSet().add(area);
				}
			}else{
				CountArea[][] countArea=new CountArea[24][6];
				countArea[hour][min]=new CountArea();
				countArea[hour][min].setCount(1);
				Set<String> set=new HashSet<>();
				set.add(area);
				countArea[hour][min].setSet(set);
				dayMessage.put(date,countArea);
			}
		}
		for(Map.Entry<String, CountArea[][]> set:dayMessage.entrySet()){
			JsonObject dateMessage=new JsonObject();
			String date=set.getKey();
			CountArea[][] countArea=set.getValue();
			for (int i = 0; i < countArea.length; i++) {
				if(!judge.get(date)[i])
					continue;
				JsonObject hourMessage=new JsonObject();
				int total=0;
				Set<String> hourArea=new HashSet<>();
				for (int j = 0; j < countArea[0].length; j++) {
					if(countArea[i][j]==null)
						continue;
					JsonObject minMessage=new JsonObject();
					minMessage.addProperty("count", countArea[i][j].getCount());
					total+=countArea[i][j].getCount();
					JsonArray areas=new JsonArray();
					for(String area:countArea[i][j].getSet()){
						areas.add(area);
						hourArea.add(area);
					}
					minMessage.add("regions", areas);
					hourMessage.add(j+"", minMessage);
				}
				if(total==0) //该小时没有短信
					continue;
				hourMessage.addProperty("total", total);
				JsonArray hourRegions=new JsonArray();
				for(String demo:hourArea){
					hourRegions.add(demo);
				}
				hourMessage.add("regions", hourRegions);
				dateMessage.add(i+"", hourMessage);
			}
			result.add(date, dateMessage);
		}
		return result.toString();
	}
	
	@RequestMapping("/getMessagesByPhone")
	public String getMessagesByPhone(@RequestParam("phone") String phone) {// 李子接口1
		List<Message> list = messageDao.getMessagesByPhone(phone);
		JsonObject result = new JsonObject();
		Map<String, List<Message>> dayMessage = new HashMap<>();
		for (Message message : list) {
			Timestamp ctime = message.getConntime();
			String day = (1900 + ctime.getYear()) + "-" + (ctime.getMonth() + 1) + "-" + ctime.getDate();
			if (dayMessage.containsKey(day)) {
				dayMessage.get(day).add(message);
			} else {
				List<Message> dayList = new LinkedList<>();
				dayList.add(message);
				dayMessage.put(day, dayList);
			}
		}
		for (Entry<String, List<Message>> set : dayMessage.entrySet()) {
			String day = set.getKey();
			List<Message> dMessage = set.getValue();
			JsonObject dayJson = new JsonObject();
			Map<String, List<Message>> hourMessage = new HashMap<>();
			for (Message message : dMessage) { // 某一天内所有短信
				String hour = message.getRecitime().getHours() + "";
				if (hourMessage.containsKey(hour)) {
					hourMessage.get(hour).add(message);
				} else {
					List<Message> hourList = new LinkedList<>();
					hourList.add(message);
					hourMessage.put(hour, hourList);
				}
			}
			for (Entry<String, List<Message>> entry : hourMessage.entrySet()) {
				String hour = entry.getKey();
				List<Message> hMessage = entry.getValue();
				JsonObject hourJson = new JsonObject();
				JsonArray jsonArr = new JsonArray();
				for (Message message : hMessage) {
					JsonObject son = new JsonObject();
					son.addProperty("md5", message.getMd5());
					son.addProperty("lng", message.getLng());
					son.addProperty("lat", message.getLat());
					son.addProperty("conntime", message.getConntime().toString());
					son.addProperty("recitime", message.getRecitime().toString());
					jsonArr.add(son);
				}
				hourJson.add("messages", jsonArr);
				dayJson.add(hour, hourJson);
			}
			result.add(day, dayJson);
		}
		return result.toString();
	}

	@RequestMapping("/china")
	public void china() throws FileNotFoundException {
		File root = new File("C:/Users/rain/Desktop/data"); // 创建文件对象
		File files[] = root.listFiles();
		Map<String, String> map = new HashMap<>();
		for (File file : files) {
			Scanner x = new Scanner(file);
			x.nextLine();
			while (x.hasNextLine()) {
				String m = x.nextLine();
				m += x.nextLine();
				int first = m.indexOf("\"");
				int last = m.lastIndexOf("\"");
				if (first <= 0 || first >= last) {
					System.out.println(m);
					continue;
				}
				String md5 = m.substring(0, first - 1);
				String content = m.substring(first + 1, last);
				String left = m.substring(last + 2);
				String s[] = left.split(",");
				messageDao.insertMessage(new Message(md5, s[0], new Timestamp(Long.parseLong(s[1])),
						new Timestamp(Long.parseLong(s[2])), Double.parseDouble(s[3]), Double.parseDouble(s[4])));
				if (!map.containsKey(md5)) {
					map.put(md5, content);
					Text text = new Text();
					text.setMd5(md5);
					text.setContent(content);
					textDao.insertText(text);
				}
			}
			System.out.println(map.size());
		}
	}

	public static void main(String[] args) {
		// Map<MyTimestamp, Integer> dayMessage=new HashMap<>();
		// dayMessage.put(new MyTimestamp(1487817332000L), 1);
		// dayMessage.put(new MyTimestamp(1487844263000L), 1);
		// System.out.println(dayMessage.size());
		// System.out.println(getPosByJW(116.50196838, 39.93611908));
		// System.out.println(getPosByJW(116.48497009, 39.8574791));
		// System.out.println(new Timestamp(1487088000000L));
		SpringApplication.run(ChinaVisApplication.class, args);
	}

	@RequestMapping("/getActionByDate")
	public String getActionByDate(@RequestParam("date") String date) {// 张接口2
		JsonObject result = new JsonObject();
		int start = 1, end = 77;
		for (int i = start; i <= end; i++) {
			String time[] = date.split("-");
			int year = Integer.parseInt(time[0]);
			int month = Integer.parseInt(time[1]);
			int day = Integer.parseInt(time[2]);
			Timestamp timestamp = new Timestamp(year - 1900, month - 1, day, 0, 0, 0, 0);
			List<Message> jizhanMessage = messageDao.getMessagesByJizhanAndDate(i, timestamp);
			JsonObject jizhanActionAndSend=new JsonObject();
			JsonArray jizhanAction = new JsonArray();
			Map<Timestamp,Integer> rtime=new HashMap<>();
			for (Message message : jizhanMessage) {
				JsonObject oneAction = new JsonObject();
				Timestamp ctime = message.getConntime();
				int index = ctime.toLocaleString().indexOf(" ");
				String conntime = ctime.toLocaleString().substring(index + 1);
				oneAction.addProperty("phone", message.getPhone());
				oneAction.addProperty("conntime", conntime);
				oneAction.addProperty("lng", message.getLng());
				oneAction.addProperty("lat", message.getLat());
				rtime.put(message.getRecitime(),textDao.getType(message.getMd5()));
				jizhanAction.add(oneAction);
			}
			jizhanActionAndSend.add("action", jizhanAction);
			JsonArray jizhanSend=new JsonArray();
			for(Map.Entry<Timestamp, Integer> set:rtime.entrySet()){
				JsonObject oneSend=new JsonObject();
				int index = set.getKey().toLocaleString().indexOf(" ");
				oneSend.addProperty("time", set.getKey().toLocaleString().substring(index + 1));
				oneSend.addProperty("type", set.getValue());
				jizhanSend.add(oneSend);
			}
			jizhanActionAndSend.add("send", jizhanSend);
			result.add("" + i, jizhanActionAndSend);
		}
		return result.toString();
	}
	
	@RequestMapping("/writeJizhan")
	public void writeJizhan() {// 静态化李接口1
		for (int i = 14; i <= 76; i++) {
			jizhanBitMapDao.insertText(i, getMessagesByJizhan(i));
		}
	}
	
	@RequestMapping("/writeTypeTimeArea")
	public void writeTypeTimeArea() {// 静态化李接口2,李接口3
		String dates[]=new String[]{"2017-3-05",
				"2017-4-10","2017-4-11","2017-4-23","2017-4-24",
				"2017-4-25","2017-4-26"};
//		String dates[]=new String[]{"2017-3-15"};
		for(String date:dates){
			String text=getTypeTimeArea(date);
			typeTimeAreaDao.insertText(date,text);
			text=getTypeAreaTime(date);
			typeAreaTimeDao.insertText(date,text);
		}
	}
	@RequestMapping("/getJizhanBitMap")
	public String getJizhanBitMap(@RequestParam("jizhan") int jizhan) {// 优化后李接口1
		return jizhanBitMapDao.getText(jizhan);
	}
	@RequestMapping("/getTypeTimeAreaByDate2")
	public String getTypeTimeArea2(@RequestParam("date") String date) {// 优化后李接口2
		return typeTimeAreaDao.getText(date);
	}
	@RequestMapping("/getTypeAreaTimeByDate2")
	public String getTypeAreaTime2(@RequestParam("date") String date) {// 优化后李接口3
		return typeAreaTimeDao.getText(date);
	}
	
	@RequestMapping("/getTypeTimeAreaByDate")
	public String getTypeTimeArea(@RequestParam("date") String date) {// 李接口2
		JsonObject result = new JsonObject();
		Map<String, Integer>[][] map=new HashMap[15][6];
		String tt[] = date.split("-");
		int year = Integer.parseInt(tt[0]);
		int month = Integer.parseInt(tt[1]);
		int day = Integer.parseInt(tt[2]);
		Timestamp timestamp = new Timestamp(year - 1900, month - 1, day, 0, 0, 0, 0);
		List<TypeTimeLngLat> list=messageDao.getTypeTimeAreaByDate(timestamp);
		JsonArray arr=new JsonArray();
		for(TypeTimeLngLat data:list){
			int type=data.getType();
			int time=data.getTime();
//			String area="hehe";
			String area=getAddt(data.getLng(), data.getLat());
			if(map[type][time]==null){
				map[type][time]=new HashMap<>();
				map[type][time].put(area, 1);
			}else{
				if(map[type][time].containsKey(area)){
					map[type][time].put(area, map[type][time].get(area)+1);
				}else{
					map[type][time].put(area, 1);
				}
			}
		}
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				JsonObject one=new JsonObject();
				one.addProperty("source", i);
				one.addProperty("target", j);
				JsonObject areaNum=new JsonObject();
				if(map[i][j]==null)
					continue;
				for(Map.Entry<String, Integer> set:map[i][j].entrySet()){
					areaNum.addProperty(set.getKey(), set.getValue());
				}
				one.add("value", areaNum);
				arr.add(one);
			}
		}
		result.add("links", arr);
		return result.toString();
	}
	
	@RequestMapping("/getAllArea")
	public void getAllArea(){
		Set<String> set=new HashSet<>();
		List<Pos> list=messageDao.selectAllMessage();
		for(Pos pos:list){
			set.add(getAddt(pos.getLng(), pos.getLat()));
		}
		for(String area:set){
			System.out.println(area);
		}
	}
	
	@RequestMapping("/getTypeAreaTime")
	public String getTypeAreaTime(@RequestParam("date") String date) {// 李接口3
		JsonObject result = new JsonObject();
		Map<String, int[]>[] map=new HashMap[15];
		String tt[] = date.split("-");
		int year = Integer.parseInt(tt[0]);
		int month = Integer.parseInt(tt[1]);
		int day = Integer.parseInt(tt[2]);
		Timestamp timestamp = new Timestamp(year - 1900, month - 1, day, 0, 0, 0, 0);
		List<TypeTimeLngLat> list=messageDao.getTypeTimeAreaByDate(timestamp);
		JsonArray arr=new JsonArray();
		for(TypeTimeLngLat data:list){
			int type=data.getType();
			int time=data.getTime();
			String area=getAddt(data.getLng(), data.getLat());
			if(map[type]==null){
				map[type]=new HashMap<>();
				if(map[type].get(area)==null){
					int[] timeNum=new int[6];
					timeNum[time]=1;
					map[type].put(area, timeNum);
				}else{
					map[type].get(area)[time]+=1;
				}
			}else{
				if(map[type].get(area)==null){
					int[] timeNum=new int[6];
					timeNum[time]=1;
					map[type].put(area, timeNum);
				}else{
					map[type].get(area)[time]+=1;
				}
			}
		}
		for (int i = 0; i < map.length; i++) {
			Map<String, int[]> areaTime=map[i];
			if(areaTime==null)
				continue;
			for(Map.Entry<String, int[]> set:areaTime.entrySet()){
				String area=set.getKey();
				int[] time=set.getValue();
				JsonObject one=new JsonObject();
				one.addProperty("source", i);
				one.addProperty("target", area);
				JsonObject timeNum=new JsonObject();
				for (int j = 0; j < time.length; j++) {
					timeNum.addProperty(j+"", time[j]);
				}
				one.add("value", timeNum);
				arr.add(one);
			}
		}
		result.add("links", arr);
		return result.toString();
	}

	@RequestMapping("/getAllTypeMessage")
	public String getAllTypeMessage() {// 张接口1
		int start = 736748, end = 736810;
		Timestamp base = new Timestamp(1487779200000L);
		JsonObject result = new JsonObject();
		for (int i = start; i <= end; i++) {
			base.setDate(base.getDate() + i - start);
			List<TypeNum> list = messageDao.getTypeMessageByDate(base);
			JsonObject today = new JsonObject();
			String date = (1900 + base.getYear()) + "-" + (base.getMonth() + 1) + "-" + base.getDate();
			base.setDate(base.getDate() - i + start);
			int total = 0;
			for (TypeNum typenum : list) {
				today.addProperty("" + typenum.getType(), typenum.getNum());
				total += typenum.getNum();
			}
			today.addProperty("total", total);
			result.add(date, today);
		}
		return result.toString();
	}

	@RequestMapping("/writeType")
	public void writeType() throws FileNotFoundException {
		File root = new File("C:/Users/rain/Desktop/type.txt");
		Scanner x = new Scanner(root);
		while (x.hasNextLine()) {
			String s = x.nextLine();
			String ss[] = s.split(",");
			String md5 = ss[0];
			Integer type = Integer.parseInt(ss[1].trim());
			textDao.updateType(md5, type);
		}
	}

	@RequestMapping("/change")
	public void change() {
		List<List<String>> cluster = new LinkedList<>();
		Map<String, double[][]> map = new LinkedHashMap<>();
		List<String> phones = messageDao.getAllPhones();
		// String phones[]=new String[]{"95588","10656668888"};
		for (String phone : phones) {
			List<Message> list = messageDao.getMessagesByPhone(phone);
			double[][] time2pos = new double[71 * 24 * 6][4]; // 0jmin 1jmax
																// 2wmin 3wmax
			for (Message message : list) {
				int index = getIndexByTime(message.getConntime());
				double lng = message.getLng();
				double lat = message.getLat();
				if (time2pos[index][0] == 0) {
					time2pos[index][0] = lng;
					time2pos[index][1] = lng;
					time2pos[index][2] = lat;
					time2pos[index][3] = lat;
				} else {
					time2pos[index][0] = Math.min(lng, time2pos[index][0]);
					time2pos[index][1] = Math.max(lng, time2pos[index][1]);
					time2pos[index][2] = Math.min(lat, time2pos[index][2]);
					time2pos[index][3] = Math.max(lat, time2pos[index][3]);
				}
			}
			map.put(phone, Arrays.copyOfRange(time2pos, 0, time2pos.length));
		}
		// System.out.println("map get");
		// for (int i = 0; i < map.get("10656668888").length; i++) {
		// System.out.println(Arrays.toString(map.get("10656668888")[i]));
		// }
		// for (int i = 0; i < map.get("95588").length; i++) {
		// System.out.println(Arrays.toString(map.get("95588")[i]));
		// }
		// System.out.println(judge(map.get("10656668888"), map.get("95588")));
		for (Map.Entry<String, double[][]> set : map.entrySet()) {
			String phone = set.getKey();
			if (cluster.size() <= 0) {
				List<String> newJizhan = new LinkedList<>();
				newJizhan.add(phone);
				cluster.add(newJizhan);
			} else {
				boolean isAdd = false;
				for (List<String> list : cluster) {
					if (judge(map.get(list.get(0)), map.get(phone))) {
						list.add(phone);
						isAdd = true;
						break;
					}
				}
				if (!isAdd) {
					List<String> newJizhan = new LinkedList<>();
					newJizhan.add(phone);
					cluster.add(newJizhan);
				}
			}
		}
		// System.out.println("start insert");
		int index = 0;
		for (List<String> list : cluster) {
			index++;
			for (String phone : list) {
				Jizhan jizhan = new Jizhan();
				jizhan.setPhone(phone);
				jizhan.setJizhan(index);
				jizhanDao.insert(jizhan);
			}
			// for(String phone:list){
			// System.out.print(phone+" ");
			// }
			// System.out.println();
		}
		// System.out.println("end insert");
	}

	public static boolean judge(double a[][], double b[][]) {
		double jmin = 0, jmax = 0, wmin = 0, wmax = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i][0] == 0 || b[i][0] == 0 || (a[i][1] - a[i][0]) > jdmaxper10min
					|| (b[i][1] - b[i][0]) > jdmaxper10min || (a[i][3] - a[i][2]) > wdmaxper10min
					|| (b[i][3] - b[i][2]) > wdmaxper10min)
				continue;
			jmin = Math.min(a[i][0], b[i][0]);
			jmax = Math.max(a[i][1], b[i][1]);
			wmin = Math.min(a[i][2], b[i][2]);
			wmax = Math.max(a[i][3], b[i][3]);
			if (jmax - jmin > jdmaxper10min || wmax - wmin > wdmaxper10min) {
				// System.out.println(i+","+Arrays.toString(a[i])+","+Arrays.toString(b[i]));
				return false;
			}
		}
		return true;
	}

	public static int getPosByJW(double lng, double lat) {
		int i = (int) Math.floor((lng - minLng) / dLng);
		int j = (int) Math.floor((lat - minLat) / dLat);
		return 14 * i + j;
	}

	public static int getIndexByTime(Timestamp rtime) {
		return (int) ((rtime.getTime() - minTime) / 600000);
	}

	public static String getAddt(double lng, double lat) {
		// lat 小 log 大
		// 参数解释: 纬度,经度 type 001 (100代表道路，010代表POI，001代表门址，111可以同时显示前三项)
		String urlString = "http://gc.ditu.aliyun.com/regeocoding?l=" + lat + "," + lng + "&type=010";
		String res = "";
		try {
			URL url = new URL(urlString);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			java.io.BufferedReader in = new java.io.BufferedReader(
					new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				res += line + "\n";
			}
			in.close();
		} catch (Exception e) {
			System.out.println("error in wapaction,and e is " + e.getMessage());
		}
		com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
		String result="";
		try {
			JsonObject json = (JsonObject) parser.parse(res);
			result=json.get("addrList").getAsJsonArray().get(0).getAsJsonObject().get("admCode").toString();
			return result.substring(1, result.length()-1);
		} catch (Exception e) {
			e.printStackTrace();  
			return "";
		}
	}
}
