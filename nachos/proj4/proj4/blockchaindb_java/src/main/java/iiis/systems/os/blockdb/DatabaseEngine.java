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
    final int FAILED = 0;
    final int PENDING = 1;
    final int SUCCEEDED = 2;
    final int UNRECOGNIZED = -1;
    
    private static DatabaseEngine instance = null;

    public static DatabaseEngine getInstance() {
        return instance;
    }

    public static void setup(String serverName,String dataDir) {
        instance = new DatabaseEngine(serverName,dataDir);
        instance.init();
    }

    void init() {
    }

    private HashMap<String, Integer> money = new HashMap<>();
    private int logLength = 0;
    private int blockID = 0;
    private int perLogSize = 50;
    private String dataDir;
    private String serverName;

    DatabaseEngine(String serverName,String dataDir) {
        this.dataDir = dataDir;
    }


    public int get(String userId) {
        System.out.println(userId);
        return -1;
    }

    public boolean transfer(String fromId, String toId, int value,int fee,String uuid) {
        return false;
    }

    public int verify(String uuid) {
        return UNRECOGNIZED;
    }

    public int getHeight() {
        return -1;
    }

    public String getBlock(String hash) {
        return "";
    }

    public void pushBlock(String block) {
    }

    public void pushTransaction(String fromId, String toId, int value,int fee,String uuid) {

    }

    class Transaction {
        String fromId;
        String toId;
        int value;
        int minningFee;
        String uuid;
        Transaction(String fromId,String toId,int value,int minningFee,String uuid) {
            this.fromId = fromId;
            this.toId = toId;
            this.value = value;
            this.minningFee = minningFee;
            this.uuid = uuid;
        }
    }
}
