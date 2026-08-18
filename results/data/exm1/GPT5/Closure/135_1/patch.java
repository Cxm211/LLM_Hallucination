private void replaceReferencesToThis(Node node, String name) {
    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else if (NodeUtil.isFunction(child)) {
        // Do not replace inside nested functions
        continue;
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }