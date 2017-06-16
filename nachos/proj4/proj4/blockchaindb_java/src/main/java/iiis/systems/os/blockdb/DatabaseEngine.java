package iiis.systems.os.blockdb;

import java.util.HashMap;

import java.util.LinkedList;

import iiis.systems.os.blockchaindb.Transaction;
import org.json.JSONObject;
import org.json.JSONArray;
import org.omg.CORBA.TRANSACTION_MODE;

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

    final int defaultMoney = 1000;

    final int MAX_BLOCK_SIZE = 10;

    private static DatabaseEngine instance = null;

    LinkedList<Transaction> pendingTransaction = new LinkedList<Transaction>();
    HashMap<String, Integer> storage = new HashMap<String, Integer>();
    HashMap<String, Integer> transactionState = new HashMap<String, Integer>();
    private int logLength = 0;
    private int blockID = 0;
    private int perLogSize = 50;
    private String dataDir;
    private String serverName;

    String databaseLock = "";

    public static DatabaseEngine getInstance() {
        return instance;
    }

    public static void setup(JSONObject obj, String serverName) {
        instance = new DatabaseEngine(obj, serverName);
        instance.init();
    }

    void init() {
    }

    DatabaseEngine(JSONObject obj, String serverName) {
    }

    int getValueById(String id) {
        if (!storage.containsKey(id)) storage.put(id, defaultMoney);
        return storage.get(id);
    }

    void storeValueById(String id, int value) {
        storage.put(id, value);
    }

    boolean checkTransactionValid(Transaction transaction) {
        String fromId = transaction.fromId;
        String toId = transaction.toId;
        int fromV = getValueById(fromId);
        int value = transaction.value;
        int fee = transaction.minningFee;
        if (value <= fee) return false;
        if (value <= 0) return false;
        if (value > fromV) return false;
        if (fee <= 0) return false;//#TODO : anymore?
        return true;
    }

    void performTrasaction(Transaction transaction) {
        String fromId = transaction.fromId;
        String toId = transaction.toId;
        int fromV = getValueById(fromId);
        int toV = getValueById(toId);
        storeValueById(fromId, fromV - transaction.value);
        storeValueById(toId, toV + transaction.value - transaction.minningFee);

        transactionState.put(transaction.uuid,SUCCEEDED);
    }

    void reperformTransaction(Transaction transaction) {
        String fromId = transaction.fromId;
        String toId = transaction.toId;
        int fromV = getValueById(fromId);
        int toV = getValueById(toId);
        storeValueById(fromId, fromV + transaction.value);
        storeValueById(toId, toV - transaction.value + transaction.minningFee);

        transactionState.put(transaction.uuid,FAILED);
    }

    void genNewBlock() {

    }

    void addNewTransaction(Transaction transaction) {
        pendingTransaction.add(transaction);
        transactionState.put(transaction.uuid,PENDING);

        if (pendingTransaction.size() >= MAX_BLOCK_SIZE) {
            genNewBlock();
        }
    }


    public int get(String userId) {
        return getValueById(userId);
    }

    public boolean transfer(String fromId, String toId, int value, int fee, String uuid) {
        Transaction transaction = new Transaction(fromId,toId,value,fee,uuid);
        if (checkTransactionValid(transaction)) {
            addNewTransaction(transaction);
            return true;
        }
        else return false;
    }

    public int verify(String uuid) {
        if (transactionState.containsKey(uuid)) return transactionState.get(uuid);
        else return FAILED;
    }

    public int getHeight() {
        return -1;
    }

    public String getBlock(String hash) {
        return "";
    }

    public void pushBlock(String block) {
    }

    public void pushTransaction(String fromId, String toId, int value, int fee, String uuid) {

    }

    class Transaction {
        String fromId;
        String toId;
        int value;
        int minningFee;
        String uuid;

        Transaction(String fromId, String toId, int value, int minningFee, String uuid) {
            this.fromId = fromId;
            this.toId = toId;
            this.value = value;
            this.minningFee = minningFee;
            this.uuid = uuid;
        }
    }
}
