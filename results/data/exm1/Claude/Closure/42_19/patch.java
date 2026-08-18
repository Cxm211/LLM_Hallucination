Node processForInLoop(ForInLoop loopNode) {

        // Return the bare minimum to put the AST in a valid state.
      return newNode(
          Token.FOR,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          traverseWithScope(loopNode.getBody()));
    }