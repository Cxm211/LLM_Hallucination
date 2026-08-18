private void replaceReferencesToThis(Node node, String name) {
    // If the current node is a function, traverse only its body, but do not
    // traverse into any nested functions.
    if (NodeUtil.isFunction(node)) {
      node = node.getLastChild();
    }

    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else if (!NodeUtil.isFunction(child)) {
        replaceReferencesToThis(child, name);
      }
    }
  }