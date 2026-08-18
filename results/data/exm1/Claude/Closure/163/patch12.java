public Node getFunctionNode() {
  Node parent = nameNode.getParent();

  if (parent.isFunction()) {
    return parent;
  } else {
    return nameNode.getChildAtIndex(1);
  }
}