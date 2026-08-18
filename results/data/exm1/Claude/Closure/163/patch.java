private ProcessProperties() {
  symbolStack.push(new NameContext(globalNode));
}

public void enterScope(NodeTraversal t) {
  if (!symbolStack.isEmpty()) {
    symbolStack.peek().scope = t.getScope();
  }
}