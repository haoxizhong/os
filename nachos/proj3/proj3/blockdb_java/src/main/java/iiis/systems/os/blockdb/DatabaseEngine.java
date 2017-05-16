package iiis.systems.os.blockdb;

import java.util.HashMap;

import java.util.LinkedList;

import org.json.JSONObject;
import org.json.JSONArray;

import java.lang.IndexOutOfBoundsException;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream; 

public class DatabaseEngine {
	private static DatabaseEngine instance = null;

	public static DatabaseEngine getInstance() {
		return instance;
	}

	public static void setup(String dataDir) {
		instance = new DatabaseEngine(dataDir);
		instance.init();
	}

	class Operation
	{
		public String operation;
		public String user,userTo;
		public int value;
	}

	void init()
	{
		initializing = true;
		while (true)
		{
			blockID ++;
			String fileName = dataDir + blockID + ".json";
			File file = new File(fileName);
			if (!file.exists())
			{
				break;
			}
			try
			{
				JSONObject mid = Util.readJsonFile(fileName);
				JSONArray arr = mid.getJSONArray("Transactions");
				for (int a=0;a<arr.length();a++)
				{
					JSONObject obj = arr.getJSONObject(a);
					String operation = obj.getString("Type");
					if (operation.equals("TRANSFER")) getOrZero(obj.getString("FromID"));
					else getOrZero(obj.getString("UserID"));
					//System.out.println(operation+" " +obj.getString("UserID")+ " " + obj.getInt("Value"));
					if (operation.equals("PUT")) this.put(obj.getString("UserID"),obj.getInt("Value"));
					else if (operation.equals("DEPOSIT")) this.deposit(obj.getString("UserID"),obj.getInt("Value"));
					else if (operation.equals("WITHDRAW")) this.withdraw(obj.getString("UserID"),obj.getInt("Value"));
					else if (operation.equals("TRANSFER")) this.transfer(obj.getString("FromID"),obj.getString("ToID"),obj.getInt("Value"));
					else
					{
						System.out.println("There is an error with previous data");
						System.exit(0);
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				break;
			}
		}
		blockID --;
		if (blockID < 0) blockID = 0;
		initializing = false;
	}

	private LinkedList<Operation> opList = new LinkedList<Operation>();

	private HashMap<String, Integer> balances = new HashMap<>();
	private int logLength = 0;
	private int blockID = 0;
	private int perLogSize = 50;
	private String dataDir;

	private static String logLock="",setupLock="";

	private static HashMap<String, String> userLock = new HashMap<>();

	public static boolean initializing = false;

	DatabaseEngine(String dataDir) {
		this.dataDir = dataDir;
	}

	private String getLock(String userId)
	{
		if (!userLock.containsKey(userId))
		{
			userLock.put(userId,"");
		}
		return userLock.get(userId);
	}

	private int getOrZero(String userId) {
		if (balances.containsKey(userId)) {
			return balances.get(userId);
		} else {
			return 0;
		}
	}

	private String genData()
	{
		synchronized (setupLock)
		{
			String data = "{\n";
			data = data + "\"BlockID\":" + blockID + ",\n";
			data = data + "\"PrevHash\":\"00000000\",\n";
			data = data + "\"Transactions\":[\n";

			boolean first = true;
			for (int a=0;a<opList.size();a++)
			{
				Operation now = opList.get(a);
				data = data + "{";
				data = data + "\"Type\":\""+now.operation+"\",";
				if (!now.operation.equals("TRANSFER"))
				{
					if (!now.operation.equals("GET")) data = data + "\"Value\":"+now.value+",\"UserID\":\""+now.user+"\"";
					else data = data + "\"UserID\":\""+now.user+"\"";
				}
				else
				{
					data = data + "\"Value\":"+now.value+",\"FromID\":\""+now.user+"\",\"ToID\":\""+now.userTo+"\"";
				}
				if (a==opList.size()-1) data = data + "}\n";
				else data = data + "},\n";
			}

			data = data + "],\n";
			data = data + "\"Nonce\":\"00000000\"\n";
			data = data + "}\n";

			return data;
		}
	}

	private void saveToLogFile()
	{
		try
		{
			blockID ++;
			String fileName = dataDir + blockID + ".json";
			File file = new File(fileName);
			if (!file.exists())
			{
				file.createNewFile();
			}

			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			//System.out.println(genData());
			writer.print(genData());
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private void genLog(String operation,String user,int value,String userTo)
	{
		if (initializing) return;
		synchronized (logLock)
		{
			Operation now = new Operation();
			now.operation = operation;
			now.user = user;
			now.value = value;
			now.userTo = userTo;

			opList.add(now);
			logLength ++;
			if (opList.size() >= perLogSize)
			{
				saveToLogFile();
				opList.clear();
				logLength = 0;
			}
		}
	}

	boolean exist(String uid)
	{
		return balances.containsKey(uid);
	}

	public int get(String userId) {
		synchronized (setupLock)
		{
			int value = getOrZero(userId);
			//System.out.println("Get value:"+userId + " " + value);
			return value;
		}
	}

	public boolean put(String userId, int value) {
		synchronized (setupLock)
		{
			//System.out.println("???");
			String writeLock = getLock(userId);
			synchronized (writeLock)
			{
				balances.put(userId, value);
				//System.out.println("Put value:"+userId+" "+value);

				genLog("PUT",userId,value,"");
				return true;
			}
		}
	}

	public boolean deposit(String userId, int value) {
		synchronized (setupLock)
		{
			String writeLock = getLock(userId);
			synchronized (writeLock)
			{
				int balance = getOrZero(userId);
				balances.put(userId, balance + value);

				genLog("DEPOSIT",userId,value,"");
				return true;
			}
		}
	}

	public boolean withdraw(String userId, int value) {
		synchronized (setupLock)
		{
			String writeLock = getLock(userId);
			synchronized (writeLock)
			{
				int balance = getOrZero(userId);
				if (balance < value) return false;
				balances.put(userId, balance - value);

				genLog("WITHDRAW",userId,value,"");
				return true;
			}
		}
	}

	public boolean transfer(String fromId, String toId, int value) {
		synchronized (setupLock)
		{
			String writeLock1 = getLock(fromId);
			String writeLock2 = getLock(toId);
			synchronized (writeLock1)
			{
				synchronized (writeLock2)
				{

					int fromBalance = getOrZero(fromId);
					if (fromBalance < value) return false;
					int toBalance = getOrZero(toId);
					balances.put(fromId, fromBalance - value);
					balances.put(toId, toBalance + value);

					genLog("TRANSFER",fromId,value,toId);
					return true;
				}
			}
		}
	}

	public int getLogLength() {
		return logLength;
	}
}
