private void findCalledFunctions(
      Node node, Set<String> changed) {
    Preconditions.checkArgument(changed != null);
    // For each referenced function, add a new reference
    if (node.getType() == Token.CALL) {
      Node child = node.getFirstChild();
      if (child.getType() == Token.NAME) {
        changed.add(child.getString());
      } else if (child.getType() == Token.GETPROP) {
        // Handle calls like fn.call(...) or fn.apply(...)
        Node target = child.getFirstChild();
        Node prop = child.getLastChild();
        if (prop != null && target != null
            && ("call".equals(prop.getString()) || "apply".equals(prop.getString()))
            && target.getType() == Token.NAME) {
          changed.add(target.getString());
        }
      }
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  }