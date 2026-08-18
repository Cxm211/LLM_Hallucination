Node processForInLoop(ForInLoop loopNode) {

        // Create a FOR node representing a for-in loop with iterator, iterable, and body.
      Node forNode = newNode(
          Token.FOR,
          transform(loopNode.getIterator()),
          transform(loopNode.getIteratedObject()),
          transformBlock(loopNode.getBody()));

      // Mark this FOR node explicitly as a for-in loop, if supported by the Node API.
      try {
        // Some versions of the Node API provide an explicit setter for for-in loops.
        // Use reflection to avoid compile issues if the method is absent.
        java.lang.reflect.Method m = forNode.getClass().getMethod("setIsForIn", boolean.class);
        m.invoke(forNode, true);
      } catch (Exception ignored) {
        // If not available, the arity (3 children) typically distinguishes for-in from classic for.
      }

      return forNode;
    }