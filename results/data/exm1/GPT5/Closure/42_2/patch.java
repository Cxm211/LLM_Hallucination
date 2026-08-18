Node processForInLoop(ForInLoop loopNode) {

        // Properly represent a for-in loop with an IN node.
      return newNode(
          Token.FOR,
          newNode(
              Token.IN,
              transform(loopNode.getIterator()),
              transform(loopNode.getIteratedObject())),
          transformBlock(loopNode.getBody()));
    }