Node processForInLoop(ForInLoop loopNode) {

    // Return the bare minimum to put the AST in a valid state.
  if (loopNode.isForEach()) {
    reportError("unsupported language extension: for each");
  }
  return newNode(
      Token.FOR,
      transform(loopNode.getIterator()),
      transform(loopNode.getIteratedObject()),
      transformBlock(loopNode.getBody()));
}