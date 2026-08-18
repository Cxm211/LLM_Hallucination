private boolean hasExceptionHandler(Node cfgNode) {
  if (cfgNode == null) {
    return false;
  }
  
  Node parent = cfgNode;
  while (parent != null) {
    if (parent.isTry()) {
      Node tryBlock = parent.getFirstChild();
      Node catchBlock = tryBlock.getNext();
      
      if (catchBlock != null && catchBlock.isCatch()) {
        return true;
      }
    }
    parent = parent.getParent();
  }
  
  return false;
}