  private void replaceReferencesToThis(Node node, String name) {
    Node child = node.getFirstChild();
    while (child != null) {
      Node next = child.getNextSibling();
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else if (!NodeUtil.isFunction(child)) {
        replaceReferencesToThis(child, name);
      }
      child = next;
    }
  }