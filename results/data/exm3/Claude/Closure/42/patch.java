Node processForInLoop(ForInLoop loopNode) {

      // Return the bare minimum to put the AST in a valid state.
    return newNode(
        loopNode.isForEach() ? Token.FOR_EACH : Token.FOR,
        transform(loopNode.getIterator()),
        transform(loopNode.getIteratedObject()),
        transformBlock(loopNode.getBody()));
  }