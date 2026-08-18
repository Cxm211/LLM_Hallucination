    Node processForInLoop(ForInLoop loopNode) {

        // Properly represent a for-in loop using an IN node as the iterator/object pair.
      return newNode(
          Token.FOR,
          newNode(Token.IN,
              transform(loopNode.getIterator()),
              transform(loopNode.getIteratedObject())),
          transformBlock(loopNode.getBody()));
    }