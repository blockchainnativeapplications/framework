pragma solidity ^0.4.22;

contract HelloWorld {

    string greeting;

    constructor (string g) public{
        greeting = g;
    }

    function hello (string greeter) public view returns (string) {
        return string(abi.encodePacked(greeting, ' ', greeter, '!'));
    }
}