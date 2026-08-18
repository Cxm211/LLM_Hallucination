private boolean hasExceptionHandler(Node cfgNode) {
  for (Node child = cfgNode.getFirstChild(); child != null; child = child.getNext()) {
    if (child.getType() == Token.CATCH) {
      return true;
    }
  }
  return false;
}