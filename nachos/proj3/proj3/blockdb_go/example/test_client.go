package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"
	"log"

	pb "../protobuf/go"

	"golang.org/x/net/context"
	"google.golang.org/grpc"
)

var address = func() string {
	conf, err := ioutil.ReadFile("config.json")
	if err != nil {
		panic(err)
	}
	var dat map[string]interface{}
	err = json.Unmarshal(conf, &dat)
	if err != nil {
		panic(err)
	}
	dat = dat["1"].(map[string]interface{})
	return fmt.Sprintf("%s:%s", dat["ip"], dat["port"])
}()

var (
	OpCode = flag.String("T", "", `DB transactions to perform:
	1 (or GET):      Show the balance of a given UserID. 
		Require option -user.
	2 (or PUT):      Set the balance of a given UserID. 
		Require option -user, -value.
	3 (or DEPOSIT):  Increase balance of a given UserID.
		Require option -user, -value.
	4 (or WITHDRAW): Decrease balance of a given UserID. 
		Require option -user, -value.
	5 (or TRANSFER): Transfer some money from one account to another.
		Require option -from, -to, -value.

	(Additional operations)
	LogLength:		 Check the length of transient log.
	`)
	UserID = flag.String("user", "00000000", "User account ID for the operation.")
	FromID = flag.String("from", "00000000", "From account (for Transfer)")
	ToID   = flag.String("to", "12345678", "To account (for Transfer)")
	Value  = flag.Int("value", 1, "Amount of transaction")
)

func main() {
	flag.Parse()

	// Set up a connection to the server.
	conn, err := grpc.Dial(address, grpc.WithInsecure())
	if err != nil {
		log.Fatalf("Cannot connect to server: %v", err)
	}
	defer conn.Close()
	//new client
	c := pb.NewBlockDatabaseClient(conn)

	switch *OpCode {
	case "":
		log.Fatal("Please specify an operation to perform (-T [1-5]). Run with -help to see a list of supported operations.")
	default:
		log.Fatal("Unknown operation.")
	case "1", "GET":
		if r, err := c.Get(context.Background(), &pb.GetRequest{UserID: *UserID}); err != nil {
			log.Printf("GET Error: %v", err)
		} else {
			log.Printf("GET Return: %d", r.Value)
		}
	case "2", "PUT":
		if r, err := c.Put(context.Background(), &pb.Request{UserID: *UserID, Value: int32(*Value)}); err != nil {
			log.Printf("PUT Error: %v", err)
		} else {
			log.Printf("PUT Return: %s", r)
		}
	case "3", "DEPOSIT":
		if r, err := c.Deposit(context.Background(), &pb.Request{UserID: *UserID, Value: int32(*Value)}); err != nil {
			log.Printf("DEPOSIT Error: %v", err)
		} else {
			log.Printf("DEPOSIT Return: %s", r)
		}
	case "4", "WITHDRAW":
		if r, err := c.Withdraw(context.Background(), &pb.Request{UserID: *UserID, Value: int32(*Value)}); err != nil {
			log.Printf("WITHDRAW Error: %v", err)
		} else {
			log.Printf("WITHDRAW Return: %s", r)
		}
	case "5", "TRANSFER":
		if r, err := c.Transfer(context.Background(), &pb.TransferRequest{FromID: *FromID, ToID: *ToID, Value: int32(*Value)}); err != nil {
			log.Printf("TRANSFER Error: %v", err)
		} else {
			log.Printf("TRANSFER Return: %s", r)
		}
	// Additional debug operations
	case "LogLength", "LOGLENGTH":
		if r, err := c.LogLength(context.Background(), &pb.Null{}); err != nil {
			log.Printf("LogLength Error: %v", err)
		} else {
			log.Printf("LogLength Return: %s", r)
		}
	}
}
