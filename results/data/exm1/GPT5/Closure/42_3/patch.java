Node processForInLoop(ForInLoop loopNode) {

        // Build a proper FOR-in node: [iterator] [iteratedObject] [body].
        Node iterator = transform(loopNode.getIterator());
        // Ensure the iterator is a single binding when declared with var.
        if (iterator.getType() == Token.VAR && iterator.getChildCount() > 1) {
          // Only the first name is valid in a for-in declaration.
          Node first = iterator.getFirstChild().detachFromParent();
          iterator = newNode(Token.VAR, first);
        }

        Node iteratedObject = transform(loopNode.getIteratedObject());
        Node body = transformBlock(loopNode.getBody());

        return newNode(Token.FOR, iterator, iteratedObject, body);
    }