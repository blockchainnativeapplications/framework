pragma solidity ^0.4.22;

contract TypeConverterTestContract {

    function uuidParameter (string) public pure{
    }

    function uuidReturnType () public pure returns (bytes16){
        return bytes16(0);
    }

    function byteReturnType () public pure returns (uint8){
        return 1;
    }

    function listParameter (bytes16[]) public pure{
    }

    function uuidListReturnType () public pure returns (bytes16[]){
        return new bytes16[](1);
    }

    function twoDimensionalListParameter (bytes16[5][]) public pure {
    }

    function arrayParameter (uint16[]) public pure{
    }

    function twoDimensionalArrayParameter (uint16[5][]) public pure {
    }
}