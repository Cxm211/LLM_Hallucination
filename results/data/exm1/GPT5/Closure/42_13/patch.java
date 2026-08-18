Node processForInLoop(ForInLoop loopNode) {

        // Properly transform a for-in loop: for (<iterator> in <object>) { <body> }
        Node iterator = transform(loopNode.getIterator());
        Node iteratedObject = transform(loopNode.getIteratedObject());
        Node body = transformBlock(loopNode.getBody());

        // Ensure the iterator is valid for a for-in. If a VAR declaration has multiple
        // names (which is invalid in for-in), keep only the first one to maintain a
        // valid AST shape.
        if (iterator.getType() == Token.VAR) {
          Node first = iterator.getFirstChild();
          if (first != null && first.getNext() != null) {
            // Keep only the first declaration.
            iterator = newNode(Token.VAR, first.cloneTree());
          }
        }

        return newNode(Token.FOR, iterator, iteratedObject, body);
    }