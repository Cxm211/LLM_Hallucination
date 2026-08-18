Node processForInLoop(ForInLoop loopNode) {
    Node node = newNode(
        Token.FOR,
        transform(loopNode.getIterator()),
        transform(loopNode.getIteratedObject()),
        transformBlock(loopNode.getBody()));
    node.setLineno(loopNode.getLineno());
    node.setCharno(loopNode.getAbsolutePosition());
    node.setLength(loopNode.getLength());
    return node;
}