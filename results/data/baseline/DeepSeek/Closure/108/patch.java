public void applyAlias() {
  Node aliasDefinition = aliasVar.getInitialValue();
  String aliasName = aliasVar.getName();
  String typeName = aliasReference.getString();
  String aliasExpanded = Preconditions.checkNotNull(aliasDefinition.getQualifiedName());
  Preconditions.checkState(typeName.startsWith(aliasName));
  Node replacementNode = aliasDefinition.cloneTree();
  aliasReference.getParent().replaceChild(aliasReference, replacementNode);
  compiler.reportCodeChange();
}