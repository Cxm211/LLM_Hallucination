private void replaceReferencesToThis(Node node, String name) {
    for (Node child : node.children()) {
      if (NodeUtil.isFunction(child)) {
        // Do not recurse into nested functions.
        continue;
      }

      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }