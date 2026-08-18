private void replaceReferencesToThis(Node node, String name) {
    for (Node child = node.getFirstChild(); child != null; ) {
      Node next = child.getNext();
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else if (!NodeUtil.isFunction(child)) {
        replaceReferencesToThis(child, name);
      }
      child = next;
    }
  }