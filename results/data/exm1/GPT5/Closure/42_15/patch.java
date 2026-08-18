    Node processForInLoop(ForInLoop loopNode) {

        // Properly create a FOR-IN node with iterator, iterable, and body.
      return newNode(
          Token.FOR_IN,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          transformBlock(loopNode.getBody()));
    }