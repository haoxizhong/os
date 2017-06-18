package iiis.systems.os.blockdb;

import iiis.systems.os.blockchaindb.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class DatabaseEngine {
    int abs(int x) {
        if (x < 0) return -x;
        else return x;
    }

    final static int FAILED = 0;
    final static int PENDING = 1;
    final static int SUCCEEDED = 2;
    final static int UNRECOGNIZED = -1;

    final static int defaultMoney = 1000;
    final static int backStep = 6;

    final static int MAX_BLOCK_SIZE = 50;
    final static int MIN_BLOCK_SIZE = 37;
    final int MAX_TRY_TIME = 5000000;

    private static DatabaseEngine instance = null;

    LinkedList<Server> serverList = new LinkedList<Server>();
    LinkedList<Transaction> pendingTransaction = new LinkedList<Transaction>();
    HashMap<String, Integer> storage = new HashMap<String, Integer>();
    HashMap<String, Transaction> transactionList = new HashMap<String, Transaction>();
    HashMap<String, Block> blockList = new HashMap<String, Block>();
    private int logLength = 0;
    private int blockID = 0;
    private int perLogSize = 50;
    private String dataDir;
    private String serverName;

    boolean genningBlock = false;

    Random generator = new Random(19960618);

    Block currentBlock;

    String databaseLock = "";

    public static DatabaseEngine getInstance() {
        return instance;
    }

    public static void setup(JSONObject obj, String serverName) {
        instance = new DatabaseEngine(obj, serverName);
        instance.init();
    }

    void init() {
        Block basicBlock = new Block();
        basicBlock.blockHash = "0000000000000000000000000000000000000000000000000000000000000000";
        basicBlock.height = 0;
        basicBlock.transactions = new LinkedList<Transaction>();
        basicBlock.prevHash = basicBlock.blockHash;
        basicBlock.nonce = "00000000";
        basicBlock.minerID = serverName;
        basicBlock.toString();

        blockList.put(basicBlock.blockHash, basicBlock);

        currentBlock = basicBlock;

        File data = new File(dataDir);
        if (data.exists()) {
            File[] files = data.listFiles();
            for (File file : files) {
                //System.out.println(file.getAbsolutePath());
                try {
                    if (!file.isDirectory() && file.getAbsolutePath().endsWith(".zhxblock")) {
                        JSONObject obj = Util.readJsonFile(file.getAbsolutePath());
                        Block block = createBlockFromJson(obj);
                        Scanner scanner = new Scanner(new File(file.getAbsolutePath().replace(".zhxblock", ".zhxstring")));
                        block.blockString = "";
                        while (scanner.hasNextLine()) {
                            block.blockString += scanner.nextLine() + "\n";
                        }
                        scanner.close();
                        block.blockString = block.blockString.substring(0, block.blockString.length() - 1);
                        //System.out.println(block.blockString);
                        if (!Hash.checkHash(block.blockHash) || !Hash.getHashString(block.blockString).equals(block.blockHash))
                            throw new Exception();
                        blockList.put(block.blockHash, block);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        } else {
            try {
                data.mkdirs();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        HashMap<String, Integer> vote = new HashMap<String, Integer>();
        for (int a = 0; a < serverList.size(); a++) {
            Server server = serverList.get(a);
            Server response = Sender.sendGetHeight(server.address, server.port);
            String hash = response.address;
            //System.out.println(hash);
            if (!hash.equals("")) {
                if (!vote.containsKey(hash)) vote.put(hash, 0);
                int value = vote.get(hash);
                vote.put(hash, value + 1);
            }
        }

        Iterator<String> iterator = vote.keySet().iterator();
        String result = "";
        int resultVote = 0;
        while (iterator.hasNext()) {
            String next = iterator.next();
            int value = vote.get(next);
            if (value > resultVote || (value == resultVote && next.compareTo(result) < 0)) {
                result = next;
                resultVote = value;
            }
        }

        if (!result.equals("")) dfsGenBlock(result);

        currentBlock = basicBlock;
        iterator = blockList.keySet().iterator();
        while (iterator.hasNext()) {
            String hash = iterator.next();
            if (blockList.get(hash).height > currentBlock.height || (blockList.get(hash).height == currentBlock.height && blockList.get(hash).blockHash.compareTo(currentBlock.blockHash) < 0))
                currentBlock = blockList.get(hash);
        }

        iterator = blockList.keySet().iterator();
        while (iterator.hasNext()) {
            String hash = iterator.next();
            Block block = blockList.get(hash);
            for (int a = 0; a < block.transactions.size(); a++) {
                Transaction transaction = block.transactions.get(a);
                transactionList.put(transaction.uuid, transaction);
            }
        }

        dfsGenTransaction(currentBlock.blockHash, 0);

        //System.out.println(currentBlock.toString());
        System.out.println(currentBlock.blockString);
    }

    void dfsGenTransaction(String hash, int depth) {
        Block block = blockList.get(hash);
        if (block.height == 0) return;
        dfsGenTransaction(block.prevHash, depth + 1);
        for (int a = 0; a < block.transactions.size(); a++) {
            Transaction transaction = block.transactions.get(a);
            if (depth >= backStep) {
                transaction.status = SUCCEEDED;
                transactionList.put(transaction.uuid, transaction);
            }
            performTrasaction(transaction, block.minerID);
        }
    }

    void dfsGenBlock(String hash) {
        if (blockList.containsKey(hash)) return;
        Block result;
        while (true) {
            int serverId = abs(generator.nextInt()) % serverList.size();
            Server server = serverList.get(serverId);
            try {
                String resultString = Sender.sendGetBlock(server.address, server.port, hash);
                if (!resultString.equals("")) {
                    if (Hash.getHashString(resultString).equals(hash)) {
                        JSONObject obj = new JSONObject(resultString);
                        if (checkBlock(obj, resultString, false)) {
                            result = createBlockFromString(obj, resultString);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        dfsGenBlock(result.prevHash);
        Block preBlock = blockList.get(result.prevHash);
        result.height = preBlock.height + 1;
        blockList.put(result.blockHash, result);
        writeBlock(result);
    }

    int dfsCheckBlock(String hash, int depth) {
        if (blockList.containsKey(hash)) return blockList.get(hash).blockID;
        if (depth > backStep) return -1;
        Block result;
        while (true) {
            int serverId = abs(generator.nextInt()) % serverList.size();
            Server server = serverList.get(serverId);
            try {
                String resultString = Sender.sendGetBlock(server.address, server.port, hash);
                if (!resultString.equals("")) {
                    if (Hash.getHashString(resultString).equals(hash)) {
                        JSONObject obj = new JSONObject(resultString);
                        if (checkBlock(obj, resultString, false)) {
                            result = createBlockFromString(obj, resultString);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

        int value = dfsCheckBlock(result.prevHash,depth+1);
        if (value == -1) return -1;
        if (value+1 != result.blockID) return -1;
        return result.blockID;
    }

    DatabaseEngine(JSONObject obj, String serverName) {
        this.generator = new Random(19960618 + serverName.hashCode());
        this.serverName = serverName;
        this.dataDir = "." + obj.getJSONObject(serverName).getString("dataDir");
        Iterator<String> iterator = obj.keys();
        while (iterator.hasNext()) {
            String nowName = iterator.next();
            if (!nowName.equals(serverName) && !nowName.equals("nservers")) {
                Server server = new Server();
                server.address = obj.getJSONObject(nowName).getString("ip");
                server.port = obj.getJSONObject(nowName).getInt("port");
                serverList.add(server);
            }
        }
    }

    void writeBlock(Block block) {
        try {
            String hash = block.blockHash;
            FileWriter writer = new FileWriter(dataDir + hash + ".zhxblock");
            writer.write(block.toFileString());
            writer.close();
            writer = new FileWriter(dataDir + hash + ".zhxstring");
            writer.write(block.blockString);
            writer.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    int getValueById(String id) {
        if (!storage.containsKey(id)) storage.put(id, defaultMoney);
        return storage.get(id);
    }

    void storeValueById(String id, int value) {
        storage.put(id, value);
    }

    boolean checkTransactionValid(Transaction transaction, boolean tag) {
        //System.out.println(transaction.toString());
        String fromId = transaction.fromId;
        String toId = transaction.toId;
        if (fromId.length() != 8) return false;
        if (toId.length() != 8) return false;
        if (fromId.equals(toId)) return false;
        int fromV = getValueById(fromId);
        int value = transaction.value;
        int fee = transaction.miningFee;
        if (value <= fee) return false;
        if (value <= 0) return false;
        if (tag && value > fromV) return false;
        if (fee <= 0) return false;//#TODO : anymore?
        return true;
    }

    void performTrasaction(Transaction transaction, String serverId) {
        String fromId = transaction.fromId;
        String toId = transaction.toId;
        int fromV = getValueById(fromId);
        int toV = getValueById(toId);
        storeValueById(fromId, fromV - transaction.value);
        storeValueById(toId, toV + transaction.value - transaction.miningFee);
        int serverV = getValueById(serverId);
        storeValueById(serverId, serverV + transaction.miningFee);

        //transactionState.put(transaction.uuid, SUCCEEDED);

        //#TODO more effcient
    }

    void reperformTransaction(Transaction transaction, String serverId) {
        String fromId = transaction.fromId;
        String toId = transaction.toId;
        int fromV = getValueById(fromId);
        int toV = getValueById(toId);
        storeValueById(fromId, fromV + transaction.value);
        storeValueById(toId, toV - transaction.value + transaction.miningFee);
        int serverV = getValueById(serverId);
        storeValueById(serverId, serverV - transaction.miningFee);

        //transactionState.put(transaction.uuid, FAILED);

        //#TODO more effcient
    }

    String genNonce() {
        String nonce = "" + abs(generator.nextInt()) % 100000000;
        while (nonce.length() < 8)
            nonce = "0" + nonce;
        return nonce;
    }

    class genNewBlockThread extends Thread {
        public void run() {
            System.out.println("Begin to gen Block");
            Block block = new Block();
            synchronized (databaseLock) {
                Block nowBlock = blockList.get(currentBlock.blockHash);

                block.blockID = nowBlock.blockID + 1;
                block.prevHash = nowBlock.blockHash;
                block.height = nowBlock.height + 1;
                if (serverName.length() < 2) block.minerID = "Server" + "0" + serverName;
                else block.minerID = "Server" + serverName;
                block.transactions = new LinkedList<Transaction>();
                HashMap<String, Integer> nowDelta = new HashMap<String, Integer>();
                LinkedList<Transaction> failList = new LinkedList<>();
                //System.out.println(pendingTransaction.size() + " " + MAX_BLOCK_SIZE);
                for (int a = 0; a < pendingTransaction.size(); a++) {
                    Transaction transaction = pendingTransaction.get(a);
                    String fromId = transaction.fromId;
                    String toId = transaction.toId;
                    int value = transaction.value;
                    int preV = storage.get(fromId);
                    if (!nowDelta.containsKey(fromId)) nowDelta.put(fromId, 0);
                    if (!nowDelta.containsKey(toId)) nowDelta.put(toId, 0);
                    int delta = nowDelta.get(fromId);
                    if (preV + delta - value < 0) {
                        //System.out.println(preV + " " + delta + " " + value);
                        failList.add(transaction);
                        pendingTransaction.remove(a);
                        a--;
                        continue;
                    }
                    nowDelta.put(fromId, delta - value);
                    nowDelta.put(toId, nowDelta.get(toId) + value - transaction.miningFee);
                    block.transactions.add(transaction);

                    if (block.transactions.size() >= MAX_BLOCK_SIZE) break;
                }
                //System.out.println(block.transactions.size());

                for (int a = 0; a < failList.size(); a++)
                    pendingTransaction.add(failList.get(a));
            }

            if (block.transactions.size() < MIN_BLOCK_SIZE) {
                genningBlock = false;
                return;
            }

            //choosing nonce
            block.nonce = genNonce();
            String nowstr = block.toString();
            String nowhash = Hash.getHashString(nowstr);

            int try_time = 0;
            boolean success = true;
            while (!Hash.checkHash(nowhash)) {
                try_time = try_time + 1;
                if (try_time % 100000 == 0) {
                    System.out.println("Genning:" + try_time);
                    if (currentBlock.height > block.height + backStep / 2) {
                        success = false;
                        break;
                    }
                }
                if (try_time > MAX_TRY_TIME) {
                    success = false;
                    break;
                }
                block.nonce = genNonce();
                nowstr = block.toString();
                nowhash = Hash.getHashString(nowstr);
            }
            System.out.println("Genning result:" + success);
            if (success) {
                block.blockHash = nowhash;
                block.blockString = nowstr;

                genningBlock = false;
                addBlock(block);

                final Block resultBlock = block;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int a = 0; a < serverList.size(); a++) {
                            Server server = serverList.get(a);
                            Sender.sendPushBlock(server.address, server.port, resultBlock.blockString);
                        }
                    }
                }).start();
                /*for (int a=0,b=0;a<block.transactions.size();a++)
                {
                    Transaction transaction = block.transactions.get(a);
                    while (b<pendingTransaction.size() && pendingTransaction.get(b).uuid != transaction.uuid)
                        b++;
                    if (b<pendingTransaction.size()) pendingTransaction.remove(b);
                }*/
            }
        }
    }

    void genNewBlock() {
        genningBlock = true;
        new genNewBlockThread().start();
    }

    void addNewTransaction(Transaction transaction) {
        if (!transactionList.containsKey(transaction.uuid)) {
            pendingTransaction.add(transaction);
            transaction.status = PENDING;
            transactionList.put(transaction.uuid, transaction);
            //System.out.println(pendingTransaction.size());

            if (pendingTransaction.size() >= MIN_BLOCK_SIZE && !genningBlock) {
                genNewBlock();
            }

        }
    }

    boolean checkBlock(JSONObject obj, String jsonStr, boolean tag) {
        Set<String> keySet = obj.keySet();
        if (!keySet.contains("BlockID")) return false;
        if (!keySet.contains("PrevHash")) return false;
        if (tag) {
            int value = dfsCheckBlock(obj.getString("PrevHash"), 1);
            if (value == -1 || obj.getInt("BlockID") != value + 1) return false;
        }
        if (!keySet.contains("Transactions")) return false;
        if (!keySet.contains("MinerID")) return false;
        String miner = obj.getString("MinerID");
        if (miner.length() != 8 || !miner.startsWith("Server")) return false;
        String hashString = Hash.getHashString(jsonStr);
        if (!Hash.checkHash(hashString)) return false;

        JSONArray transactions = obj.getJSONArray("Transactions");
        for (int a = 0; a < transactions.length(); a++) {
            JSONObject object = transactions.getJSONObject(a);
            if (!object.has("FromID")) return false;
            if (!object.has("ToID")) return false;
            if (!object.has("Value")) return false;
            if (!object.has("MiningFee")) return false;
            if (!object.has("UUID")) return false;
            //if (tag && !transactionList.containsKey(object.getString("UUID"))) return false;
        }
        return true;
    }

    void transfer(Block block) {
        synchronized (databaseLock) {
            try {
                if (block.height > currentBlock.height || (block.height == currentBlock.height && block.blockHash.compareTo(currentBlock.blockHash) < 0)) {
                    String hash1 = block.blockHash;
                    String hash2 = currentBlock.blockHash;
                    Block block1 = block;
                    Block block2 = currentBlock;
                    LinkedList<Block> list1 = new LinkedList<Block>();
                    LinkedList<Block> list2 = new LinkedList<Block>();
                    while (!hash1.equals(hash2)) {
                        if (block1.height > block2.height) {
                            list1.add(block1);
                            hash1 = block1.prevHash;
                            if (!blockList.containsKey(hash1)) dfsGenBlock(hash1);
                            block1 = blockList.get(hash1);
                        } else {
                            list2.add(block2);
                            hash2 = block2.prevHash;
                            if (!blockList.containsKey(hash2)) dfsGenBlock(hash2);
                            block2 = blockList.get(hash2);
                        }
                    }
                    for (int a = 0; a < list2.size(); a++) {
                        Block nowBlock = list2.get(a);
                        for (int b = nowBlock.transactions.size() - 1; b >= 0; b--) {
                            Transaction transaction = nowBlock.transactions.get(b);
                            reperformTransaction(transaction, nowBlock.minerID);
                            transaction.status = PENDING;
                            transaction.belongBlock = "";
                            transactionList.put(transaction.uuid, transaction);
                        }
                    }
                    for (int a = list1.size() - 1; a >= 0; a--) {
                        Block nowBlock = list1.get(a);
                        for (int b = 0; b < nowBlock.transactions.size(); b++) {
                            Transaction transaction = nowBlock.transactions.get(b);
                            if (!checkTransactionValid(transaction, true)) throw new Exception();
                            performTrasaction(transaction, nowBlock.minerID);
                            transaction.status = PENDING;
                            transaction.belongBlock = nowBlock.blockHash;
                            transactionList.put(transaction.uuid, transaction);
                        }
                    }

                    if (block.height > backStep) {
                        Block nowBlock = block;
                        for (int a = 0; a < backStep; a++)
                            nowBlock = blockList.get(nowBlock.prevHash);
                        for (int a = 0; a < nowBlock.transactions.size(); a++) {
                            Transaction transaction = nowBlock.transactions.get(a);
                            transaction.status = SUCCEEDED;
                            transactionList.put(transaction.uuid, transaction);
                        }
                    }

                    currentBlock = block;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    void clearPending(Block block) {
        HashSet<String> set = new HashSet<String>();
        for (int a = 0; a < block.transactions.size(); a++)
            set.add(block.transactions.get(a).uuid);
        int size = 0;
        for (int a = 0; a < pendingTransaction.size(); ) {
            if (set.contains(pendingTransaction.get(a).uuid)) {
                pendingTransaction.remove(a);
                size++;
            } else a++;
        }
        //System.out.println("Cleaning size " + size);
        if (pendingTransaction.size() >=MIN_BLOCK_SIZE && !genningBlock) {
            genNewBlock();
        }
    }

    void addBlock(Block block) {
        blockList.put(block.blockHash, block);
        dfsGenBlock(block.prevHash);
        writeBlock(block);
        clearPending(block);
        transfer(block);
    }

    Block createBlockFromJson(JSONObject obj) {
        Block block = new Block();

        block.blockID = obj.getInt("BlockID");
        block.prevHash = obj.getString("PrevHash");

        JSONArray transactions = obj.getJSONArray("Transactions");
        for (int a = 0; a < transactions.length(); a++) {
            JSONObject object = transactions.getJSONObject(a);
            Transaction nowTransaction = new Transaction(object.getString("FromID"), object.getString("ToID"), object.getInt("Value"), object.getInt("MiningFee"), object.getString("UUID"));
            nowTransaction.status = PENDING;
            block.transactions.add(nowTransaction);
            transactionList.put(nowTransaction.uuid, nowTransaction);
        }

        block.minerID = obj.getString("MinerID");
        block.nonce = obj.getString("Nonce");
        block.blockHash = obj.getString("blockHash");
        block.height = obj.getInt("height");

        return block;
    }

    Block createBlockFromString(JSONObject obj, String jsonStr) {
        Block block = new Block();

        block.blockID = obj.getInt("BlockID");
        block.prevHash = obj.getString("PrevHash");

        JSONArray transactions = obj.getJSONArray("Transactions");
        for (int a = 0; a < transactions.length(); a++) {
            JSONObject object = transactions.getJSONObject(a);
            Transaction nowTransaction = new Transaction(object.getString("FromID"), object.getString("ToID"), object.getInt("Value"), object.getInt("MiningFee"), object.getString("UUID"));
            block.transactions.add(nowTransaction);
            nowTransaction.status = PENDING;
            transactionList.put(nowTransaction.uuid, nowTransaction);
        }

        block.minerID = obj.getString("MinerID");
        block.nonce = obj.getString("Nonce");
        if (blockList.containsKey(block.prevHash)) block.height = blockList.get(block.prevHash).height + 1;
        block.blockString = jsonStr;
        block.blockHash = Hash.getHashString(jsonStr);

        return block;
    }

    void addNewBlock(JSONObject obj, String jsonStr) {
        Block block = createBlockFromString(obj, jsonStr);
        addBlock(block);
    }

    public int get(String userId) {
        //System.out.println("Receive a get");
        return getValueById(userId);
    }

    public boolean transfer(String fromId, String toId, int value, int fee, String uuid, iiis.systems.os.blockchaindb.Transaction.Types type, boolean broadcast) {
        System.out.println("Receive a trasanction");
        if (type != iiis.systems.os.blockchaindb.Transaction.Types.TRANSFER) return false;
        Transaction transaction = new Transaction(fromId, toId, value, fee, uuid);
        if (checkTransactionValid(transaction, false)) {
            addNewTransaction(transaction);
            boolean success = false;
            int breakID = 0;
            for (int a = 0; a < serverList.size(); a++) {
                Server server = serverList.get(a);
                if (Sender.sendPushTransaction(server.address, server.port, transaction)) {
                    success = true;
                    breakID = a;
                }
            }
            final int ID = breakID + 1;
            final Transaction finalTransaction = transaction;
            if (success) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int a = ID; a < serverList.size(); a++) {
                            Server server = serverList.get(a);
                            Sender.sendPushTransaction(server.address, server.port, finalTransaction);
                        }
                    }
                }).start();
            }
            return success;
        } else return false;
    }

    public Server verify(String fromId, String toId, int value, int fee, String uuid, iiis.systems.os.blockchaindb.Transaction.Types type) {
        if (type != iiis.systems.os.blockchaindb.Transaction.Types.TRANSFER) return new Server("", FAILED);
        if (transactionList.containsKey(uuid)) {
            Transaction transaction = transactionList.get(uuid);
            if (!transaction.fromId.equals(fromId)) return new Server("", FAILED);
            if (!transaction.toId.equals(toId)) return new Server("", FAILED);
            if (transaction.value != value) return new Server("", FAILED);
            if (transaction.miningFee != fee) return new Server("", FAILED);
            if (!transaction.uuid.equals(uuid)) return new Server("", FAILED);
            return new Server(transaction.belongBlock, transaction.status);
        } else return new Server("", FAILED);
    }

    public Server getHeight() {
        return new Server(currentBlock.blockHash, currentBlock.height);
    }

    public String getBlock(String hash) {
        if (blockList.containsKey(hash)) return blockList.get(hash).blockString;
        else return null;
    }

    public void pushBlock(String block) {
        try {
            System.out.println("Receive block");
            //System.out.println(block);
            JSONObject obj = new JSONObject(block);
            if (checkBlock(obj, block, true)) {
                System.out.println("Adding block");
                addNewBlock(obj, block);
            }
        } catch (Exception e) {
            //e.printStackTrace();//#TODO
        }
    }

    public void pushTransaction(String fromId, String toId, int value, int fee, String uuid, iiis.systems.os.blockchaindb.Transaction.Types type) {
        if (transactionList.containsKey(uuid)) return;
        transfer(fromId, toId, value, fee, uuid, type, false);
    }

    static class Transaction {
        String fromId;
        String toId;
        int value;
        int miningFee;
        String uuid;
        String belongBlock = "";
        int status = FAILED;

        Transaction(String fromId, String toId, int value, int miningFee, String uuid) {
            this.fromId = fromId;
            this.toId = toId;
            this.value = value;
            this.miningFee = miningFee;
            this.uuid = uuid;
        }

        @Override
        public String toString() {
            return "{"
                    + "\"Type\" : \"TRANSFER\","
                    + "\"FromID\" : \"" + fromId + "\","
                    + "\"ToID\" : \"" + toId + "\","
                    + "\"Value\" : " + value + ","
                    + "\"MiningFee\" : " + miningFee + ","
                    + "\"UUID\" : \"" + uuid + "\""
                    + "}";
        }
    }

    class Block {
        int blockID;
        String prevHash;
        LinkedList<Transaction> transactions = new LinkedList<Transaction>();
        String minerID;
        String nonce;
        String blockString;
        int height;
        String blockHash;

        @Override
        public String toString() {
            String result = "";
            result = "{"
                    + "\"Nonce\" : \"" + nonce + "\",\n"
                    + "\"BlockID\" : " + blockID + ",\n"
                    + "\"PrevHash\" : \"" + prevHash + "\",\n"
                    + "\"MinerID\" : \"" + minerID + "\",\n";
            result = result + "\"Transactions\" : [\n";
            for (int a = 0; a < transactions.size(); a++) {
                result = result + transactions.get(a).toString();
                if (a + 1 == transactions.size()) result = result + "]";
                else result = result + ",";
                result = result + "\n";
            }
            if (transactions.size() == 0) result = result + "]";
            result = result + "}";
            blockString = result;
            return result;
        }

        public String toFileString() {
            String result = "";
            result = "{"
                    + "\"Nonce\" : \"" + nonce + "\",\n"
                    + "\"BlockID\" : " + blockID + ",\n"
                    + "\"PrevHash\" : \"" + prevHash + "\",\n"
                    + "\"blockHash\" : \"" + blockHash + "\",\n"
                    + "\"MinerID\" : \"" + minerID + "\",\n"
                    + "\"height\" : \"" + height + "\",\n";
            result = result + "\"Transactions\" : [\n";
            for (int a = 0; a < transactions.size(); a++) {
                result = result + transactions.get(a).toString();
                if (a + 1 == transactions.size()) result = result + "]";
                else result = result + ",";
                result = result + "\n";
            }
            if (transactions.size() == 0) result = result + "]";
            result = result + "}";
            return result;
        }
    }

    static class Server {
        String address;
        int port;

        Server() {
        }

        Server(String a, int b) {
            address = a;
            port = b;
        }
    }
}
