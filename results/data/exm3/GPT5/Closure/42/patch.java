Node processForInLoop(ForInLoop loopNode) {

        // Reject unsupported 'for each' loops.
        if (loopNode != null && loopNode.isForEach()) {
          throw new RuntimeException("unsupported language extension: for each");
        }

        // Return the bare minimum to put the AST in a valid state.
      return newNode(
          Token.FOR,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          transformBlock(loopNode.getBody()));
    }