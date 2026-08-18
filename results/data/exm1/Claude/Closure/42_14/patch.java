Node processForInLoop(ForInLoop loopNode) {
  return newNode(
      Token.FOR,
      transform(loopNode.getIterator()),
      transform(loopNode.getIteratedObject()),
      transformBlock(loopNode.getBody()),
      loopNode.isForEach() ? 1 : 0);
}