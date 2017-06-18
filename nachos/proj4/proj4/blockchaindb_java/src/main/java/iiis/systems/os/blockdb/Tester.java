package iiis.systems.os.blockdb;

import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * Created by lenovo on 2017/6/18.
 */
public class Tester {

    static LinkedList<DatabaseEngine.Server> serverList = new LinkedList<DatabaseEngine.Server>();

    static int testId = 0;
    static void init(JSONObject obj,String keyword) {
        Iterator<String> iterator = obj.keys();
        while (iterator.hasNext()) {
            String nowName = iterator.next();
            if (!nowName.equals("nservers")) {
                DatabaseEngine.Server server = new DatabaseEngine.Server();
                server.address = obj.getJSONObject(nowName).getString("ip");
                server.port = obj.getJSONObject(nowName).getInt("port");
                serverList.add(server);
                if (nowName.equals(keyword)) testId = serverList.size()-1;
            }
        }
    }

    static Random generator = new Random(19960210);
    static HashMap<String, Integer> money;

    static int getValue(String id) {
        if (!money.containsKey(id)) money.put(id, DatabaseEngine.defaultMoney);
        return money.get(id);
    }

    static void modifyValue(String id, int value) {
        money.put(id, value);
    }

    static String genId(int bit) {
        int max = 1;
        for (int a = 0; a < bit; a++)
            max = max * 10;
        String result = "" + abs(generator.nextInt()) % max;
        while (result.length() < bit)
            result = "0" + result;
        return result;
    }

    static int abs(int x) {
        if (x < 0) return -x;
        else return x;
    }

    static void test1() {
        //Test transfer
        System.out.println("Testing basic transfer and get");
        money = new HashMap<>();
        int try_time = 3;
        for (int a = 0; a < try_time; a++) {
            for (int b = 0; b < DatabaseEngine.MAX_BLOCK_SIZE; b++) {
                String fromId = "Test-1-" + genId(1);
                String toId = "Test-1-" + genId(1);
                if (fromId.equals(toId)) {
                    b--;
                    continue;
                }
                int storage = getValue(fromId);
                if (storage <= 10) {
                    b--;
                    continue;
                }
                int value = abs(generator.nextInt()) % (storage - 5) + 3;
                int fee = abs(generator.nextInt()) % (value - 2) + 1;
                try {
                    String uuid = UUID.randomUUID().toString();
                    boolean result = Sender.sendTransfer(serverList.get(testId).address, serverList.get(testId).port, new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
                    modifyValue(fromId, getValue(fromId) - value);
                    modifyValue(toId, getValue(toId) + value - fee);
                    System.out.println(a + " " + b + " " + result + " " + uuid);
                    //Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Type a int after the system becomes stable.");
        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
        //Test get

        int query_time = 10;
        int hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-1-" + a;
            int storage = getValue(id);
            int value = Sender.sendGet(serverList.get(testId).address, serverList.get(testId).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Test1 passed/total : " + hit + "/" + 10);

        System.out.println("Please first kill the server for testing, and then restart to test recover");
        System.out.println("Type a int after the system becomes stable.");
        scanner.nextInt();
        //Testing recovery from log

        query_time = 10;
        hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-1-" + a;
            int storage = getValue(id);
            int value = Sender.sendGet(serverList.get(testId).address, serverList.get(testId).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Test1 passed/total : " + hit + "/" + 10);
    }

    static void test2() {
        //Test transfer
        System.out.println("Testing the information communication between servers");
        money = new HashMap<>();
        int try_time = 3;
        for (int a = 0; a < try_time; a++) {
            for (int b = 0; b < DatabaseEngine.MAX_BLOCK_SIZE; b++) {
                String fromId = "Test-2-" + genId(1);
                String toId = "Test-2-" + genId(1);
                if (fromId.equals(toId)) {
                    b--;
                    continue;
                }
                int storage = getValue(fromId);
                if (storage <= 10) {
                    b--;
                    continue;
                }
                int value = abs(generator.nextInt()) % (storage - 5) + 3;
                int fee = abs(generator.nextInt()) % (value - 2) + 1;
                try {
                    String uuid = UUID.randomUUID().toString();
                    boolean result = Sender.sendTransfer(serverList.get(testId).address, serverList.get(testId).port, new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
                    modifyValue(fromId, getValue(fromId) - value);
                    modifyValue(toId, getValue(toId) + value - fee);
                    System.out.println(a + " " + b + " " + result + " " + uuid);
                    //Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Type a int after the system becomes stable.");
        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
        //Test server 2

        int query_time = 10;
        int hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-2-" + a;
            int storage = getValue(id);
            int p = abs(generator.nextInt())%serverList.size();
            int value = Sender.sendGet(serverList.get(p).address, serverList.get(p).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Test1 passed/total : " + hit + "/" + 10);

        System.out.println("Type a int after the system becomes stable.");
        scanner.nextInt();
        //Test server 3

        query_time = 10;
        hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-2-" + a;
            int storage = getValue(id);
            int p = abs(generator.nextInt())%serverList.size();
            int value = Sender.sendGet(serverList.get(p).address, serverList.get(p).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Test1 passed/total : " + hit + "/" + 10);
    }

    static void test3() {
        //Test transfer
        System.out.println("Testing server block maintain");
        money = new HashMap<>();
        LinkedList<DatabaseEngine.Transaction> uuidList = new LinkedList<>();
        for (int a = 0; a < DatabaseEngine.backStep + 3; a++) {
            for (int b = 0; b < DatabaseEngine.MAX_BLOCK_SIZE; b++) {
                String fromId = "Test-3-" + genId(1);
                String toId = "Test-3-" + genId(1);
                if (fromId.equals(toId)) {
                    b--;
                    continue;
                }
                int storage = getValue(fromId);
                if (storage <= 10) {
                    b--;
                    continue;
                }
                int value = abs(generator.nextInt()) % (storage - 5) + 3;
                int fee = abs(generator.nextInt()) % (value - 2) + 1;
                try {
                    String uuid = UUID.randomUUID().toString();
                    boolean result = Sender.sendTransfer(serverList.get(0).address, serverList.get(0).port, new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
                    modifyValue(fromId, getValue(fromId) - value);
                    modifyValue(toId, getValue(toId) + value - fee);
                    System.out.println(a + " " + b + " " + result + " " + uuid);
                    uuidList.add(new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
                    //Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Type a int after the system becomes stable.");
        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
        //Test verify

        int hit = 0;
        int p = 0;
        for (int a = 0; a < DatabaseEngine.backStep + 3; a++) {
            for (int b = 0; b < DatabaseEngine.MAX_BLOCK_SIZE; b++, p++) {
                int result = Sender.sendVerify(serverList.get(testId).address, serverList.get(testId).port, uuidList.get(p));
                int expect;
                if (a <= 2) expect = 2;
                else expect = 1;
                if (expect == result) hit++;
            }
        }
        System.out.println("Test1 passed/total : " + hit + "/" + uuidList.size());
    }
	
	static void test4(){
		//Test invalid case
        money = new HashMap<>();
		String saveuuid = "";
		int fail = 0;
		int success = 0;
		//Format test
		for (int b = 0; b <= 117; b++) {
			String fromId = "Test-4-" + genId(1);
			String toId = "Test-4-" + genId(1);
			if (fromId.equals(toId)) {
				b--;
				continue;
			}
			int storage = getValue(fromId);
			if (storage <= 10) {
				b--;
				continue;
			}
			int value = abs(generator.nextInt()) % (storage - 5) + 3;
			int fee = abs(generator.nextInt()) % (value - 2) + 1;
			try {
				String uuid = UUID.randomUUID().toString();
				switch(b) {
					case 100:
						fromId = "";
						break;
					case 101:
						fromId = null;
						break;
					case 102:
						fromId = "Test-4-";
						break;
					case 103:
						fromId = "Test-4-10";
						break;
					case 104:
						toId = "";
						break;
					case 105:
						toId = null;
						break;
					case 106:
						toId = "Test-4-";
						break;
					case 107:
						toId = "Test-4-10";
						break;
					case 108:
						toId = fromId;
						break;
					case 109:
						value = 0;
						break;
					case 110:
						value = -1;
						break;
					case 111:
						fee = value;
						break;
					case 112:
						fee = 0;
						break;
					case 113:
						fee = -1;
						break;
					case 114:
						uuid = "";
						break;
					case 115:
						uuid = null;
						break;
					case 116:
						uuid = uuid.substring(31);
						break;
					case 117:
						uuid = uuid + "233";
						break;
				}
				boolean result = Sender.sendTransfer(serverList.get(testId).address, serverList.get(testId).port, new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
				if (result && (b > 100)) {
					System.out.println("Failed on case " + b);
					fail += 1;
				}
				else
					success += 1;
				if (b < 100) {
					modifyValue(fromId, getValue(fromId) - value);
					modifyValue(toId, getValue(toId) + value - fee);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Pass: " + success + ", fail: " + fail);
	}
}
