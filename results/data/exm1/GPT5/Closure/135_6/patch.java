private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      return;
    }

    // Iterate safely over siblings to avoid iterator invalidation when replacing children.
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
        // Do not recurse into the replaced node.
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }