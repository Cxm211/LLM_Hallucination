  private void findCalledFunctions(
      Node node, Set<String> changed) {
    Preconditions.checkArgument(changed != null);
    // For each referenced function, add a new reference
    int type = node.getType();
    if (type == Token.CALL || type == Token.NEW) {
      Node child = node.getFirstChild();
      if (child != null) {
        if (child.getType() == Token.NAME) {
          changed.add(child.getString());
        } else if (child.getType() == Token.GETPROP) {
          String qName = child.getQualifiedName();
          if (qName != null) {
            changed.add(qName);
          }
        }
      }
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  }