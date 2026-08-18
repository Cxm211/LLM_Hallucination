private void replaceReferencesToThis(Node node, String name) {
  if (NodeUtil.isFunction(node)) {
    return;
  }

  List<Node> childList = new ArrayList<Node>();
  for (Node child : node.children()) {
    childList.add(child);
  }
  
  for (Node child : childList) {
    if (NodeUtil.isThis(child)) {
      Node newName = Node.newString(Token.NAME, name);
      node.replaceChild(child, newName);
    } else {
      replaceReferencesToThis(child, name);
    }
  }
}