package iiis.systems.os.blockdb;

import iiis.systems.os.blockchaindb.*;
import io.grpc.Server;
import io.grpc.internal.Stream;
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
        String keyword = "";
        boolean debug = false;
        for (int a = 0; a < args.length; a++) {
            if (args[a].startsWith("--id=")) keyword = args[a].substring(5);
            if (args[a].startsWith("--debug")) debug = true;
        }
        System.out.println(debug);
        System.out.println(keyword);

        JSONObject config = Util.readJsonFile("config.json");
        if (debug) {
            Tester.init(config);
            //Tester.test1();
            Tester.test2();
            try {
                while (true) ;
            }
            catch (Exception e) {
            }
            return;
        }
        JSONObject myConfig = config.getJSONObject(keyword);
        String address = myConfig.getString("ip");
        int port = myConfig.getInt("port");

        DatabaseEngine.setup(config, keyword);

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
            boolean success = dbEngine.transfer(request.getFromID(), request.getToID(), request.getValue(), request.getMiningFee(), request.getUUID(), true);
            BooleanResponse response = BooleanResponse.newBuilder().setSuccess(success).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void verify(Transaction request, StreamObserver<VerifyResponse> responseObserver) {
            DatabaseEngine.Server value = dbEngine.verify(request.getFromID(), request.getToID(), request.getValue(), request.getMiningFee(), request.getUUID());
            VerifyResponse response = VerifyResponse.newBuilder().setResultValue(value.port).setBlockHash(value.address).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getHeight(Null request, StreamObserver<GetHeightResponse> responseObserver) {
            DatabaseEngine.Server value = dbEngine.getHeight();
            GetHeightResponse response = GetHeightResponse.newBuilder().setHeight(value.port).setLeafHash(value.address).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getBlock(GetBlockRequest request, StreamObserver<JsonBlockString> responseObserver) {
            String value = dbEngine.getBlock(request.getBlockHash());
            JsonBlockString response = JsonBlockString.newBuilder().setJson(value).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void pushBlock(JsonBlockString request, StreamObserver<Null> responseObserver) {
            dbEngine.pushBlock(request.getJson());
            Null response = Null.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void pushTransaction(Transaction request, StreamObserver<Null> responseObserver) {
            dbEngine.pushTransaction(request.getFromID(), request.getToID(), request.getValue(), request.getMiningFee(), request.getUUID());
            Null response = Null.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
