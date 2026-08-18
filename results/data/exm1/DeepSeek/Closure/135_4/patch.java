  private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      return;
    }

    // Copy children to avoid ConcurrentModificationException
    java.util.ArrayList<Node> children = new java.util.ArrayList<Node>();
    for (Node c : node.children()) {
      children.add(c);
    }
    for (Node child : children) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }