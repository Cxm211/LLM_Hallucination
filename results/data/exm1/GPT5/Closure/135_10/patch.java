private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      // Only traverse into the function body, not nested functions or parameters.
      Node body = node.getLastChild();
      if (body != null) {
        replaceReferencesToThis(body, name);
      }
      return;
    }

    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }