pragma solidity ^0.4.0;

contract SimpleStorage {
    int x;

    constructor() public{
        x = 0;
    }

    function get() public view returns (int){
        return x;
    }

    function set(int value) public {
        x = value;
    }
}
