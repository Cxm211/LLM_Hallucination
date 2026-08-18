    public void applyAlias() {
      aliasReference.getParent().replaceChild(
          aliasReference, aliasDefinition.cloneTree());
    }