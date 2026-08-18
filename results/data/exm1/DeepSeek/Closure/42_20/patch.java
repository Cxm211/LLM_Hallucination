    Node processForInLoop(ForInLoop loopNode) {

        // Return the bare minimum to put the AST in a valid state.
      Node iterator;
      if (loopNode.isVarDeclaration()) {
        iterator = newNode(Token.VAR, transform(loopNode.getIterator()));
      } else {
        iterator = transform(loopNode.getIterator());
      }
      return newNode(
          Token.FOR,
          iterator,
          transform(loopNode.getIteratedObject()),
          transformBlock(loopNode.getBody()));
    }