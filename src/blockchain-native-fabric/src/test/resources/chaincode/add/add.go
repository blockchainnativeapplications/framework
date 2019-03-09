package main // package main is important, otherwise no executable but instead a linkable library is built

import (
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

type SimpleChaincode struct {
}

var logger = shim.NewLogger("add")

func (t *SimpleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("Init")
	return shim.Success(nil)
}

func (t *SimpleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()

	logger.Infof("Invoke: %s", function)

	switch function {

	case "add":
		sum := 0.0
		if len(args) < 2 {
			return shim.Error("add operation must include at least two arguments")
		}

		for i := 0; i < len(args); i++ {
			f, err := strconv.ParseFloat(args[i], 64)
			if err != nil {
				return shim.Error(fmt.Sprintf("Failed to convert '%s' to number: %s", args[i], err))
			}
			sum += f
		}

		str, err := json.Marshal(sum)
		if err != nil {
			return shim.Error(fmt.Sprintf("Failed to marshal sum: %s", err))
		}

		logger.Infof("Result: %s", str)

		return shim.Success(str)

	default:
		logger.Criticalf("Unsupported operation: %s", function)
		return shim.Error(fmt.Sprintf("Unsupported operation: %s", function))
	}
}

func main() {
	logger.SetLevel(shim.LogDebug)
	err := shim.Start(new(SimpleChaincode))
	if err != nil {
		logger.Criticalf("Error starting chaincode: %s", err)
	}
}