package iiis.systems.os.blockdb;

import java.util.HashMap;

public class DatabaseEngine {
    private static DatabaseEngine instance = null;

    public static DatabaseEngine getInstance() {
        return instance;
    }

    public static void setup(String dataDir) {
        instance = new DatabaseEngine(dataDir);
    }

    private HashMap<String, Integer> balances = new HashMap<>();
    private int logLength = 0;
    private String dataDir;

    DatabaseEngine(String dataDir) {
        this.dataDir = dataDir;
    }

    private int getOrZero(String userId) {
        if (balances.containsKey(userId)) {
            return balances.get(userId);
        } else {
            return 0;
        }
    }

    public int get(String userId) {
        logLength++;
        return getOrZero(userId);
    }

    public boolean put(String userId, int value) {
        logLength++;
        balances.put(userId, value);
        return true;
    }

    public boolean deposit(String userId, int value) {
        logLength++;
        int balance = getOrZero(userId);
        balances.put(userId, balance + value);
        return true;
    }

    public boolean withdraw(String userId, int value) {
        logLength++;
        int balance = getOrZero(userId);
        balances.put(userId, balance - value);
        return true;
    }

    public boolean transfer(String fromId, String toId, int value) {
        logLength++;
        int fromBalance = getOrZero(fromId);
        int toBalance = getOrZero(toId);
        balances.put(fromId, fromBalance - value);
        balances.put(toId, toBalance + value);
        return true;
    }

    public int getLogLength() {
        return logLength;
    }
}
