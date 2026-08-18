Node processForInLoop(ForInLoop loopNode) {
        Node body = transformBlock(loopNode.getBody());
        if (body == null) {
          body = new Node(Token.BLOCK);
        }
        return newNode(
          Token.FOR,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          body);
    }