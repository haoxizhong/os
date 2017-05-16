print "Testrun starting"

import os
import random

#os.system("./start.sh &")

def run_client(operation,user,value=0,user2=""):
	if operation == "GET":
		print operation,user
		os.system("go run ./example/test_client.go -T="+operation+" -user="+user)
	elif operation == "TRANSFER":
		print operation,user,user2,value
		os.system("go run ./example/test_client.go -T="+operation+" -from="+user+" -to="+user2+" -value="+str(value))
	else:
		print operation,user,value
		os.system("go run ./example/test_client.go -T="+operation+" -user="+user+" -value="+str(value))


def init():
	for a in range(0,5):
		run_client("GET","User-"+str(a),random.randint(1,100))


if __name__ == "__main__":
	init()

	run_client("GET","User-0")
