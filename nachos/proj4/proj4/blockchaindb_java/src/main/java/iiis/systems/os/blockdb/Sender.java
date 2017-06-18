package iiis.systems.os.blockdb;

import iiis.systems.os.blockchaindb.*;
import io.grpc.*;

import java.util.HashMap;
import java.util.concurrent.Exchanger;

/**
 * Created by zhonghaoxi on 17-6-10.
 */
public class Sender {

    public static void init() {
    }

    public static int sendGet(String ip, int port, String userId) {
        Channel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        BlockChainMinerGrpc.BlockChainMinerBlockingStub stub = BlockChainMinerGrpc.newBlockingStub(channel);

        GetRequest request = GetRequest.newBuilder().setUserID(userId).build();
        GetResponse response;
        try {
            response = stub.get(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return -1;
        }
        return response.getValue();
    }

    public static int sendVerify(String ip,int port, DatabaseEngine.Transaction transaction) {
        Channel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        BlockChainMinerGrpc.BlockChainMinerBlockingStub stub = BlockChainMinerGrpc.newBlockingStub(channel);

        Transaction request = Transaction.newBuilder()
                .setType(Transaction.Types.TRANSFER)
                .setFromID(transaction.fromId)
                .setToID(transaction.toId)
                .setValue(transaction.value)
                .setMiningFee(transaction.miningFee)
                .setUUID(transaction.uuid)
                .setType(Transaction.Types.TRANSFER)
                .build();
        VerifyResponse response;
        try {
            response = stub.verify(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return -1;
        }

        return response.getResultValue();
    }

    public static boolean sendTransfer(String ip, int port, DatabaseEngine.Transaction transaction) throws Exception {
        Channel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        BlockChainMinerGrpc.BlockChainMinerBlockingStub stub = BlockChainMinerGrpc.newBlockingStub(channel);

        Transaction request = Transaction.newBuilder()
                .setType(Transaction.Types.TRANSFER)
                .setFromID(transaction.fromId)
                .setToID(transaction.toId)
                .setValue(transaction.value)
                .setMiningFee(transaction.miningFee)
                .setUUID(transaction.uuid)
                .setType(Transaction.Types.TRANSFER)
                .build();
        BooleanResponse response;
        try {
            response = stub.transfer(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            throw new Exception();
        }

        return response.getSuccess();
    }

    public static String sendGetBlock(String ip, int port, String hash) {
        Channel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        BlockChainMinerGrpc.BlockChainMinerBlockingStub stub = BlockChainMinerGrpc.newBlockingStub(channel);

        GetBlockRequest request = GetBlockRequest.newBuilder().setBlockHash(hash).build();
        JsonBlockString response;

        try {
            response = stub.getBlock(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return "";
        }

        return response.getJson();
    }

    public static void sendPushBlock(String ip, int port, String blockString) {
        Channel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        BlockChainMinerGrpc.BlockChainMinerBlockingStub stub = BlockChainMinerGrpc.newBlockingStub(channel);

        JsonBlockString request = JsonBlockString.newBuilder().setJson(blockString).build();
        Null response;

        try {
            response = stub.pushBlock(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendPushTransaction(String ip, int port, DatabaseEngine.Transaction transaction) {
        Channel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        BlockChainMinerGrpc.BlockChainMinerBlockingStub stub = BlockChainMinerGrpc.newBlockingStub(channel);

        Transaction request = Transaction.newBuilder()
                .setType(Transaction.Types.TRANSFER)
                .setFromID(transaction.fromId)
                .setToID(transaction.toId)
                .setValue(transaction.value)
                .setMiningFee(transaction.miningFee)
                .setUUID(transaction.uuid)
                .setType(Transaction.Types.TRANSFER)
                .build();
        Null response;

        try {
            response = stub.pushTransaction(request);
            return true;
        } catch (StatusRuntimeException e) {
            //e.printStackTrace();
            return false;
        }
    }

    public static DatabaseEngine.Server sendGetHeight(String ip, int port) {
        Channel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true).build();
        BlockChainMinerGrpc.BlockChainMinerBlockingStub stub = BlockChainMinerGrpc.newBlockingStub(channel);

        Null request = Null.newBuilder().build();
        GetHeightResponse response;

        try {
            response = stub.getHeight(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return new DatabaseEngine.Server("",0);
        }

        return new DatabaseEngine.Server(response.getLeafHash(), response.getHeight());
    }
}
