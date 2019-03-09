package main // package main is important, otherwise no executable but instead a linkable library is built

import (
	"fmt"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

type SimpleChaincode struct {
}

var logger = shim.NewLogger("hello")

func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("Init")
	_, args := stub.GetFunctionAndParameters()
	if len(args) > 0 {
		logger.Infof("Setting greeting: '%s'", args[0])
		stub.PutState("greeting", []byte(args[0]))
	}

	return shim.Success(nil)
}

func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()

	logger.Infof("- Invoke: %s", function)

	switch function {

	case "hello":

		greeting, error := stub.GetState("greeting")

		if error != nil {
			greeting = []byte("Hello")
		}

		name := "Stranger"
		if len(args) > 0 {
			name = args[0]
		}

		result := fmt.Sprintf("%s %s!", greeting, name)

		stub.SetEvent("greeted", []byte(name))

		return shim.Success([]byte(result))

	default:
		return shim.Success([]byte("Unsupported operation"))
	}
}

func main() {
	logger.SetLevel(shim.LogDebug)
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		logger.Criticalf("Error starting chaincode: %s", err)
	}
}