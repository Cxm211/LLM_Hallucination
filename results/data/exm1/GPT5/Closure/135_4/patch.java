private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isThis(node)) {
      Node parent = node.getParent();
      if (parent != null) {
        Node newName = Node.newString(Token.NAME, name);
        parent.replaceChild(node, newName);
      }
      return;
    }

    if (NodeUtil.isFunction(node)) {
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