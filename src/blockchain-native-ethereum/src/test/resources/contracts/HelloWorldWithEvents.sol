pragma solidity ^0.4.22;

contract HelloWorldWithEvents {
    string greeting;

    constructor (string g) public{
        greeting = g;
    }

    event greeted(string name);

    function hello (string greeter) public returns (string) {
        emit greeted(greeter);
        return string(abi.encodePacked(greeting, ' ', greeter, '!'));
    }
}