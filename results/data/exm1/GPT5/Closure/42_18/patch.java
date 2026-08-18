Node processForInLoop(ForInLoop loopNode) {

        // Return the bare minimum to put the AST in a valid state.
      Node forNode = newNode(
          Token.FOR,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          transformBlock(loopNode.getBody()));
      // Mark this node explicitly as a for-in loop.
      forNode.putBooleanProp(Node.IS_FOR_IN, true);
      return forNode;
    }