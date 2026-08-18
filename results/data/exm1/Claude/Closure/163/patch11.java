public void remove() {
  Node parent = nameNode.getParent();
  if (parent.isFunction() || parent.hasOneChild()) {
    NodeUtil.removeChild(parent.getParent(), parent);
  } else {
    Preconditions.checkState(parent.isVar());
    parent.removeChild(nameNode);
  }
}