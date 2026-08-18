public void applyAlias() {
  Node replacement = aliasDefinition.cloneTree();
  replacement.copyInformationFromForTree(aliasReference);
  aliasReference.getParent().replaceChild(aliasReference, replacement);
}