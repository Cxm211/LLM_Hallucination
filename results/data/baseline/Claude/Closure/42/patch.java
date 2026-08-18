Node processForInLoop(ForInLoop loopNode) {

        // Return the bare minimum to put the AST in a valid state.
      Node node = newNode(
          Token.FOR,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          transformBlock(loopNode.getBody()));
      node.setIsForIn(true);
      return node;
    }