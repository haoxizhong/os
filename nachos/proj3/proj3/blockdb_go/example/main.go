package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net"
	
	pb "../protobuf/go"

	"golang.org/x/net/context"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

const blockSize = 50

var data = make(map[string]int32)
var loglen int32

type server struct{}
// Database Interface 
func (s *server) Get(ctx context.Context, in *pb.GetRequest) (*pb.GetResponse, error) {
	return &pb.GetResponse{Value: data[in.UserID]}, nil
}
func (s *server) Put(ctx context.Context, in *pb.Request) (*pb.BooleanResponse, error) {
	loglen++
	data[in.UserID] = in.Value
	return &pb.BooleanResponse{Success: true}, nil
}
func (s *server) Deposit(ctx context.Context, in *pb.Request) (*pb.BooleanResponse, error) {
	loglen++
	data[in.UserID] += in.Value
	return &pb.BooleanResponse{Success: true}, nil
}
func (s *server) Withdraw(ctx context.Context, in *pb.Request) (*pb.BooleanResponse, error) {
	loglen++
	data[in.UserID] -= in.Value
	return &pb.BooleanResponse{Success: true}, nil
}
func (s *server) Transfer(ctx context.Context, in *pb.TransferRequest) (*pb.BooleanResponse, error) {
	loglen++
	data[in.FromID] -= in.Value
	data[in.ToID] += in.Value
	return &pb.BooleanResponse{Success: true}, nil
}
// Interface with test grader
func (s *server) LogLength(ctx context.Context, in *pb.Null) (*pb.GetResponse, error) {
	return &pb.GetResponse{Value: loglen}, nil
}

// Main function, RPC server initialization
func main() {
	// Read config
	address, outputDir := func() (string, string) {
		conf, err := ioutil.ReadFile("config.json")
		if err != nil {
			panic(err)
		}
		var dat map[string]interface{}
		err = json.Unmarshal(conf, &dat)
		if err != nil {
			panic(err)
		}
		dat = dat["1"].(map[string]interface{}) // should be dat[myNum] in the future
		return fmt.Sprintf("%s:%s", dat["ip"], dat["port"]), fmt.Sprintf("%s",dat["dataDir"])
	}()
	// Unused variable
	_=outputDir

	// Bind to port
	lis, err := net.Listen("tcp", address)
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	log.Printf("Listening: %s ...", address)

	// Create gRPC server
	s := grpc.NewServer()
	pb.RegisterBlockDatabaseServer(s, &server{})
	// Register reflection service on gRPC server.
	reflection.Register(s)

	// Start server
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
