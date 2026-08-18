Node processForInLoop(ForInLoop loopNode) {
    Node iterator = transform(loopNode.getIterator());
    Node iteratedObject = transform(loopNode.getIteratedObject());
    Node body = transformBlock(loopNode.getBody());
    
    return newNode(
        Token.FOR,
        iterator,
        iteratedObject,
        body);
}