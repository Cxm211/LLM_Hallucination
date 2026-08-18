public void applyAliasReference() {
  aliasReference.getParent().replaceChild(
      aliasReference, aliasDefinition.cloneTree());
}