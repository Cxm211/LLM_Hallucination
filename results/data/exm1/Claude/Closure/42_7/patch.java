Node processForInLoop(ForInLoop loopNode) {
    Node node = newNode(
        Token.FOR,
        transform(loopNode.getIterator()),
        transform(loopNode.getIteratedObject()),
        transformBlock(loopNode.getBody()));
    node.setType(Token.FOR);
    node.putBooleanProp(Node.FOR_EACH, false);
    return node;
}