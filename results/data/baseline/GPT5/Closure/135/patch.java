private void replaceReferencesToThis(Node node, String name) {
  for (Node child : node.children()) {
    if (NodeUtil.isFunction(child)) {
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