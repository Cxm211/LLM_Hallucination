    Node processForInLoop(ForInLoop loopNode) {
        // Return the bare minimum to put the AST in a valid state.
        Node result = newNode(
            Token.FOR,
            transform(loopNode.getIterator()),
            transform(loopNode.getIteratedObject()),
            transformBlock(loopNode.getBody()));
        result.putBooleanProp(Node.FOR_IN_LOOP, true);
        return result;
    }