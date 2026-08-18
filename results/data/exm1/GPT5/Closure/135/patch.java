private void replaceReferencesToThis(Node node, String name) {
    // Traverse the current node's children, but do not recurse into nested functions,
    // since "this" inside nested functions refers to a different context.
    for (Node child : node.children()) {
      if (NodeUtil.isFunction(child)) {
        // Do not recurse into nested functions
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