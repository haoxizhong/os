package iiis.systems.os.blockdb;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * Interface exported by the server.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.2.0)",
    comments = "Source: db.proto")
public final class BlockDatabaseGrpc {

  private BlockDatabaseGrpc() {}

  public static final String SERVICE_NAME = "blockdb.BlockDatabase";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.GetRequest,
      iiis.systems.os.blockdb.GetResponse> METHOD_GET =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Get"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.GetRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.GetResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Request,
      iiis.systems.os.blockdb.BooleanResponse> METHOD_PUT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Put"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Request.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.BooleanResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Request,
      iiis.systems.os.blockdb.BooleanResponse> METHOD_WITHDRAW =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Withdraw"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Request.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.BooleanResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Request,
      iiis.systems.os.blockdb.BooleanResponse> METHOD_DEPOSIT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Deposit"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Request.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.BooleanResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.TransferRequest,
      iiis.systems.os.blockdb.BooleanResponse> METHOD_TRANSFER =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "Transfer"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.TransferRequest.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.BooleanResponse.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<iiis.systems.os.blockdb.Null,
      iiis.systems.os.blockdb.GetResponse> METHOD_LOG_LENGTH =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "blockdb.BlockDatabase", "LogLength"),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.Null.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(iiis.systems.os.blockdb.GetResponse.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BlockDatabaseStub newStub(io.grpc.Channel channel) {
    return new BlockDatabaseStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BlockDatabaseBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new BlockDatabaseBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static BlockDatabaseFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new BlockDatabaseFutureStub(channel);
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static abstract class BlockDatabaseImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Return db[UserID]
     * </pre>
     */
    public void get(iiis.systems.os.blockdb.GetRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET, responseObserver);
    }

    /**
     * <pre>
     * Set db[UserID]=Value
     * </pre>
     */
    public void put(iiis.systems.os.blockdb.Request request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PUT, responseObserver);
    }

    /**
     * <pre>
     * Perform db[UserID]+=Value
     * </pre>
     */
    public void withdraw(iiis.systems.os.blockdb.Request request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_WITHDRAW, responseObserver);
    }

    /**
     * <pre>
     * Perform db[UserID]-=Value
     * Return Success=false if balance is insufficient
     * </pre>
     */
    public void deposit(iiis.systems.os.blockdb.Request request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DEPOSIT, responseObserver);
    }

    /**
     * <pre>
     * Perform db[FromID]-=Value and db[ToID]+=Value
     * Return Success=false if FromID is same as ToID or balance of FromID is insufficient
     * </pre>
     */
    public void transfer(iiis.systems.os.blockdb.TransferRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_TRANSFER, responseObserver);
    }

    /**
     * <pre>
     * Return the length of transient (non-block) log on disk
     * </pre>
     */
    public void logLength(iiis.systems.os.blockdb.Null request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LOG_LENGTH, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_GET,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.GetRequest,
                iiis.systems.os.blockdb.GetResponse>(
                  this, METHODID_GET)))
          .addMethod(
            METHOD_PUT,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Request,
                iiis.systems.os.blockdb.BooleanResponse>(
                  this, METHODID_PUT)))
          .addMethod(
            METHOD_WITHDRAW,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Request,
                iiis.systems.os.blockdb.BooleanResponse>(
                  this, METHODID_WITHDRAW)))
          .addMethod(
            METHOD_DEPOSIT,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Request,
                iiis.systems.os.blockdb.BooleanResponse>(
                  this, METHODID_DEPOSIT)))
          .addMethod(
            METHOD_TRANSFER,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.TransferRequest,
                iiis.systems.os.blockdb.BooleanResponse>(
                  this, METHODID_TRANSFER)))
          .addMethod(
            METHOD_LOG_LENGTH,
            asyncUnaryCall(
              new MethodHandlers<
                iiis.systems.os.blockdb.Null,
                iiis.systems.os.blockdb.GetResponse>(
                  this, METHODID_LOG_LENGTH)))
          .build();
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class BlockDatabaseStub extends io.grpc.stub.AbstractStub<BlockDatabaseStub> {
    private BlockDatabaseStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BlockDatabaseStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BlockDatabaseStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BlockDatabaseStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return db[UserID]
     * </pre>
     */
    public void get(iiis.systems.os.blockdb.GetRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Set db[UserID]=Value
     * </pre>
     */
    public void put(iiis.systems.os.blockdb.Request request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PUT, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Perform db[UserID]+=Value
     * </pre>
     */
    public void withdraw(iiis.systems.os.blockdb.Request request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_WITHDRAW, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Perform db[UserID]-=Value
     * Return Success=false if balance is insufficient
     * </pre>
     */
    public void deposit(iiis.systems.os.blockdb.Request request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DEPOSIT, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Perform db[FromID]-=Value and db[ToID]+=Value
     * Return Success=false if FromID is same as ToID or balance of FromID is insufficient
     * </pre>
     */
    public void transfer(iiis.systems.os.blockdb.TransferRequest request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_TRANSFER, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Return the length of transient (non-block) log on disk
     * </pre>
     */
    public void logLength(iiis.systems.os.blockdb.Null request,
        io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LOG_LENGTH, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class BlockDatabaseBlockingStub extends io.grpc.stub.AbstractStub<BlockDatabaseBlockingStub> {
    private BlockDatabaseBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BlockDatabaseBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BlockDatabaseBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BlockDatabaseBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return db[UserID]
     * </pre>
     */
    public iiis.systems.os.blockdb.GetResponse get(iiis.systems.os.blockdb.GetRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET, getCallOptions(), request);
    }

    /**
     * <pre>
     * Set db[UserID]=Value
     * </pre>
     */
    public iiis.systems.os.blockdb.BooleanResponse put(iiis.systems.os.blockdb.Request request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PUT, getCallOptions(), request);
    }

    /**
     * <pre>
     * Perform db[UserID]+=Value
     * </pre>
     */
    public iiis.systems.os.blockdb.BooleanResponse withdraw(iiis.systems.os.blockdb.Request request) {
      return blockingUnaryCall(
          getChannel(), METHOD_WITHDRAW, getCallOptions(), request);
    }

    /**
     * <pre>
     * Perform db[UserID]-=Value
     * Return Success=false if balance is insufficient
     * </pre>
     */
    public iiis.systems.os.blockdb.BooleanResponse deposit(iiis.systems.os.blockdb.Request request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DEPOSIT, getCallOptions(), request);
    }

    /**
     * <pre>
     * Perform db[FromID]-=Value and db[ToID]+=Value
     * Return Success=false if FromID is same as ToID or balance of FromID is insufficient
     * </pre>
     */
    public iiis.systems.os.blockdb.BooleanResponse transfer(iiis.systems.os.blockdb.TransferRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_TRANSFER, getCallOptions(), request);
    }

    /**
     * <pre>
     * Return the length of transient (non-block) log on disk
     * </pre>
     */
    public iiis.systems.os.blockdb.GetResponse logLength(iiis.systems.os.blockdb.Null request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LOG_LENGTH, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Interface exported by the server.
   * </pre>
   */
  public static final class BlockDatabaseFutureStub extends io.grpc.stub.AbstractStub<BlockDatabaseFutureStub> {
    private BlockDatabaseFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private BlockDatabaseFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BlockDatabaseFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new BlockDatabaseFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Return db[UserID]
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.GetResponse> get(
        iiis.systems.os.blockdb.GetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET, getCallOptions()), request);
    }

    /**
     * <pre>
     * Set db[UserID]=Value
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.BooleanResponse> put(
        iiis.systems.os.blockdb.Request request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PUT, getCallOptions()), request);
    }

    /**
     * <pre>
     * Perform db[UserID]+=Value
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.BooleanResponse> withdraw(
        iiis.systems.os.blockdb.Request request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_WITHDRAW, getCallOptions()), request);
    }

    /**
     * <pre>
     * Perform db[UserID]-=Value
     * Return Success=false if balance is insufficient
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.BooleanResponse> deposit(
        iiis.systems.os.blockdb.Request request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DEPOSIT, getCallOptions()), request);
    }

    /**
     * <pre>
     * Perform db[FromID]-=Value and db[ToID]+=Value
     * Return Success=false if FromID is same as ToID or balance of FromID is insufficient
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.BooleanResponse> transfer(
        iiis.systems.os.blockdb.TransferRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_TRANSFER, getCallOptions()), request);
    }

    /**
     * <pre>
     * Return the length of transient (non-block) log on disk
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<iiis.systems.os.blockdb.GetResponse> logLength(
        iiis.systems.os.blockdb.Null request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LOG_LENGTH, getCallOptions()), request);
    }
  }

  private static final int METHODID_GET = 0;
  private static final int METHODID_PUT = 1;
  private static final int METHODID_WITHDRAW = 2;
  private static final int METHODID_DEPOSIT = 3;
  private static final int METHODID_TRANSFER = 4;
  private static final int METHODID_LOG_LENGTH = 5;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BlockDatabaseImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BlockDatabaseImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET:
          serviceImpl.get((iiis.systems.os.blockdb.GetRequest) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse>) responseObserver);
          break;
        case METHODID_PUT:
          serviceImpl.put((iiis.systems.os.blockdb.Request) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse>) responseObserver);
          break;
        case METHODID_WITHDRAW:
          serviceImpl.withdraw((iiis.systems.os.blockdb.Request) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse>) responseObserver);
          break;
        case METHODID_DEPOSIT:
          serviceImpl.deposit((iiis.systems.os.blockdb.Request) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse>) responseObserver);
          break;
        case METHODID_TRANSFER:
          serviceImpl.transfer((iiis.systems.os.blockdb.TransferRequest) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.BooleanResponse>) responseObserver);
          break;
        case METHODID_LOG_LENGTH:
          serviceImpl.logLength((iiis.systems.os.blockdb.Null) request,
              (io.grpc.stub.StreamObserver<iiis.systems.os.blockdb.GetResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class BlockDatabaseDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return iiis.systems.os.blockdb.DBProto.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BlockDatabaseGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BlockDatabaseDescriptorSupplier())
              .addMethod(METHOD_GET)
              .addMethod(METHOD_PUT)
              .addMethod(METHOD_WITHDRAW)
              .addMethod(METHOD_DEPOSIT)
              .addMethod(METHOD_TRANSFER)
              .addMethod(METHOD_LOG_LENGTH)
              .build();
        }
      }
    }
    return result;
  }
}
