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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

import com.example.dao.JizhanDao;
import com.example.dao.MessageDao;
import com.example.dao.TextDao;
import com.example.domain.Jizhan;
import com.example.domain.Message;
import com.example.domain.MyDate;
import com.example.domain.News;
import com.example.domain.Text;
import com.example.domain.TypeNum;
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

	@RequestMapping("/getMessagesByPhone")
	public String getMessagesByPhone(@RequestParam("phone") String phone) {// 李接口1
		List<Message> list = messageDao.getMessagesByPhone(phone);
		JsonObject result = new JsonObject();
		Map<String, List<Message>> dayMessage = new HashMap<>();
		for (Message message : list) {
			Timestamp rtime = message.getRecitime();
			String day = (1900 + rtime.getYear()) + "-" + (rtime.getMonth() + 1) + "-" + rtime.getDate();
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
			JsonArray jizhanAction = new JsonArray();
			for (Message message : jizhanMessage) {
				JsonObject oneAction = new JsonObject();
				Timestamp ctime = message.getConntime();
				int index = ctime.toLocaleString().indexOf(" ");
				String conntime = ctime.toLocaleString().substring(index + 1);
				oneAction.addProperty("phone", message.getPhone());
				oneAction.addProperty("conntime", conntime);
				oneAction.addProperty("lng", message.getLng());
				oneAction.addProperty("lat", message.getLat());
				oneAction.addProperty("type", textDao.getType(message.getMd5()));// 2分钟
				jizhanAction.add(oneAction);
			}
			result.add("" + i, jizhanAction);
		}
		return result.toString();
	}

	@RequestMapping("/getAllTypeMessage")
	public String getAllTypeMessage() {// 张接口1
		int start = 736748, end = 736810;
		Timestamp base = new Timestamp(1487779200000L);
		JsonObject result = new JsonObject();
		for (int i = start; i <= end; i++) {
			List<TypeNum> list = messageDao.getTypeMessageByDate(i);
			JsonObject today = new JsonObject();
			base.setDate(base.getDate() + i - start);
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
}
