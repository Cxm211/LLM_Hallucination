Node processForInLoop(ForInLoop loopNode) {
    return newNode(
        Token.FOR_IN,
        transform(loopNode.getIterator()),
        transform(loopNode.getIteratedObject()),
        transformBlock(loopNode.getBody()));
}