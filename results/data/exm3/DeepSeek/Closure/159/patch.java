  private void findCalledFunctions(
      Node node, Set<String> changed) {
    Preconditions.checkArgument(changed != null);
    // For each referenced function, add a new reference
    if (node.getType() == Token.CALL) {
      Node child = node.getFirstChild();
      if (child.getType() == Token.NAME) {
        changed.add(child.getString());
      } else if (child.getType() == Token.GETPROP) {
        Node obj = child.getFirstChild();
        Node prop = obj.getNext();
        if (obj != null && obj.getType() == Token.NAME
            && prop != null && prop.getType() == Token.STRING) {
          String propName = prop.getString();
          if ("call".equals(propName) || "apply".equals(propName)) {
            changed.add(obj.getString());
          }
        }
      }
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  }