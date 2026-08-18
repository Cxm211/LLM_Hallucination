private boolean hasExceptionHandler(Node cfgNode) {
  return cfgNode.hasChildren() && cfgNode.getFirstChild().isBlock();
}