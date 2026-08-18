Node processForInLoop(ForInLoop loopNode) {

        // Return the bare minimum to put the AST in a valid state.
      Node forNode = newNode(
          Token.FOR,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          transformBlock(loopNode.getBody()));
      // Preserve 'for each' semantics if present in the source.
      if (loopNode.isForEach()) {
        forNode.setIsForEach(true);
      }
      return forNode;
    }