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
                int value = abs(generator.nextInt()) % (storage - 1) + 3;
                int fee = abs(generator.nextInt()) % (value - 2) + 2;
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
                int value = abs(generator.nextInt()) % (storage - 1) + 3;
                int fee = abs(generator.nextInt()) % (value - 2) + 2;
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
                int value = abs(generator.nextInt()) % (storage - 1) + 3;
                int fee = abs(generator.nextInt()) % (value - 2) + 2;
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
}
