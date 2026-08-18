private void replaceReferencesToThis(Node node, String name) {
  if (NodeUtil.isFunction(node)) {
    return;
  }

  List<Node> childrenToProcess = new ArrayList<Node>();
  for (Node child : node.children()) {
    childrenToProcess.add(child);
  }

  for (Node child : childrenToProcess) {
    if (NodeUtil.isThis(child)) {
      Node newName = Node.newString(Token.NAME, name);
      node.replaceChild(child, newName);
    } else {
      replaceReferencesToThis(child, name);
    }
  }
}