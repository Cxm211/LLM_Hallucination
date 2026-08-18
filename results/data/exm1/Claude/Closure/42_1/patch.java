Node processForInLoop(ForInLoop loopNode) {
    return newNode(
        Token.FOR,
        newNode(Token.EMPTY),
        newNode(
            Token.IN,
            transform(loopNode.getIterator()),
            transform(loopNode.getIteratedObject())),
        transformBlock(loopNode.getBody()));
}