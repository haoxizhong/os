package iiis.systems.os.blockdb;

import iiis.systems.os.blockchaindb.*;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BlockDatabaseServer {
    private Server server;

    private void start(String address, int port) throws IOException {
        server = NettyServerBuilder.forAddress(new InetSocketAddress(address, port))
                .addService(new BlockChainDatabaseImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                BlockDatabaseServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, JSONException, InterruptedException {
        JSONObject config = Util.readJsonFile("config.json");
        config = (JSONObject) config.get("1");
        String address = config.getString("ip");
        int port = Integer.parseInt(config.getString("port"));
        String dataDir = config.getString("dataDir");

        DatabaseEngine.setup(dataDir);

        final BlockDatabaseServer server = new BlockDatabaseServer();
        server.start(address, port);
        server.blockUntilShutdown();
    }

    static class BlockChainDatabaseImpl extends BlockChainMinerGrpc.BlockChainMinerImplBase {
        private final DatabaseEngine dbEngine = DatabaseEngine.getInstance();

        @Override
        public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
            int value = dbEngine.get(request.getUserID());
            GetResponse response = GetResponse.newBuilder().setValue(value).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }


        @Override
        public void transfer(Transaction request, StreamObserver<BooleanResponse> responseObserver) {
            boolean success = dbEngine.transfer(request.getFromID(), request.getToID(), request.getValue());
            BooleanResponse response = BooleanResponse.newBuilder().setSuccess(success).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
