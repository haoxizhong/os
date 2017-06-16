package iiis.systems.os.blockdb;

import iiis.systems.os.blockchaindb.BlockChainMinerGrpc;
import iiis.systems.os.blockchaindb.GetRequest;
import iiis.systems.os.blockchaindb.GetResponse;
import io.grpc.*;

import java.util.HashMap;

/**
 * Created by zhonghaoxi on 17-6-10.
 */
public class Sender {
    private Channel channel;
    private BlockChainMinerGrpc.BlockChainMinerBlockingStub stub;
    private String ip = "";
    private int port;

    public void init()
    {
        channel = ManagedChannelBuilder.forAddress(ip,port).usePlaintext(true).build();
        stub = BlockChainMinerGrpc.newBlockingStub(channel);
    }

    public int sendGetRequest(String userId) {
        GetRequest request = GetRequest.newBuilder().setUserID(userId).build();
        GetResponse response;
        try
        {
            response = stub.get(request);
        }
        catch (StatusRuntimeException e) {
            e.printStackTrace();
            return -2;
        }
        System.out.println(response.getValue());
        return response.getValue();
    }

    public void work(String address,int port,String dataDir) {
        ip = address;
        this.port = port;
        init();

        sendGetRequest("4");
    }
}
