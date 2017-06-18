package iiis.systems.os.blockdb;

import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * Created by lenovo on 2017/6/18.
 */
public class Tester {

    static LinkedList<DatabaseEngine.Server> serverList = new LinkedList<DatabaseEngine.Server>();

    static void init(JSONObject obj) {
        Iterator<String> iterator = obj.keys();
        while (iterator.hasNext()) {
            String nowName = iterator.next();
            if (!nowName.equals("nservers")) {
                DatabaseEngine.Server server = new DatabaseEngine.Server();
                server.address = obj.getJSONObject(nowName).getString("ip");
                server.port = obj.getJSONObject(nowName).getInt("port");
                serverList.add(server);
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
                int value = abs(generator.nextInt()) % (storage - 1) + 2;
                int fee = abs(generator.nextInt()) % (value - 1) + 1;
                try {
                    String uuid = UUID.randomUUID().toString();
                    boolean result = Sender.sendTransfer(serverList.get(0).address, serverList.get(0).port, new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
                    modifyValue(fromId, getValue(fromId) - value);
                    modifyValue(toId, getValue(toId) + value - fee);
                    System.out.println(a + " " + b + " " + result + " " + uuid);
                    //Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
        //Test get

        int query_time = 10;
        int hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-1-" + a;
            int storage = getValue(id);
            int value = Sender.sendGet(serverList.get(0).address, serverList.get(0).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Hit rate:" + 1.0 * hit / 10);

        scanner.nextInt();
        //Testing recovery from log

        query_time = 10;
        hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-1-" + a;
            int storage = getValue(id);
            int value = Sender.sendGet(serverList.get(0).address, serverList.get(0).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Hit rate:" + 1.0 * hit / 10);
    }

    static void test2() {
        //Test transfer
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
                int value = abs(generator.nextInt()) % (storage - 1) + 2;
                int fee = abs(generator.nextInt()) % (value - 1) + 1;
                try {
                    String uuid = UUID.randomUUID().toString();
                    boolean result = Sender.sendTransfer(serverList.get(0).address, serverList.get(0).port, new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
                    modifyValue(fromId, getValue(fromId) - value);
                    modifyValue(toId, getValue(toId) + value - fee);
                    System.out.println(a + " " + b + " " + result + " " + uuid);
                    //Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
        //Test server 2

        int query_time = 10;
        int hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-2-" + a;
            int storage = getValue(id);
            int value = Sender.sendGet(serverList.get(1).address, serverList.get(1).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Hit rate:" + 1.0 * hit / 10);

        scanner.nextInt();
        //Test server 3

        query_time = 10;
        hit = 0;
        for (int a = 0; a < 10; a++) {
            String id = "Test-2-" + a;
            int storage = getValue(id);
            int value = Sender.sendGet(serverList.get(2).address, serverList.get(2).port, id);
            if (storage == value) hit++;
            else System.out.println("Failed:" + a + " expected " + storage + " get " + value);
        }
        System.out.println("Hit rate:" + 1.0 * hit / 10);
    }

    static void test3() {
        //Test transfer
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
                int value = abs(generator.nextInt()) % (storage - 1) + 2;
                int fee = abs(generator.nextInt()) % (value - 1) + 1;
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

        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
        //Test verify

        int hit = 0;
        int p = 0;
        for (int a = 0; a < DatabaseEngine.backStep + 3; a++) {
            for (int b = 0; b < DatabaseEngine.MAX_BLOCK_SIZE; b++, p++) {
                int result = Sender.sendVerify(serverList.get(0).address, serverList.get(0).port, uuidList.get(p));
                int expect;
                if (a <= 2) expect = 2;
                else expect = 1;
                if (expect == result) hit++;
            }
        }
        System.out.println("Hit rate:" + hit * 1.0 / uuidList.size());
    }
	
	static void test4(){
		//Test invalid case
		String saveuuid = "";
		int fail = 0;
		int success = 0;
		for (int b = 0; b <= 120; a++) {
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
			int value = abs(generator.nextInt()) % (storage - 1) + 2;
			int fee = abs(generator.nextInt()) % (value - 1) + 1;
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
						saveuuid = uuid;
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
					case 118:
						uuid = saveuuid;
						break;
					case 119:
						uuid = saveuuid;
						break;
					case 120:
						value = storage + 1;
						break;
				}
				boolean result = Sender.sendTransfer(serverList.get(0).address, serverList.get(0).port, new DatabaseEngine.Transaction(fromId, toId, value, fee, uuid));
				if (result ^ (b == 118)) {
					System.out.println("Failed on case " + a);
					fail += 1;
				}
				else
					success += 1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Success: " + success + ", fail: " + fail);
	}
}
