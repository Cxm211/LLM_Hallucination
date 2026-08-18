void replace() {
  if (firstNode == null) {
    // Don't touch the base case ('goog').
    replacementNode = candidateDefinition;
    return;
  }

  // Handle the case where there is a duplicate definition for an explicitly
  // provided symbol.
  if (candidateDefinition != null && explicitNode != null) {
    explicitNode.detachFromParent();
    compiler.reportCodeChange();
    replacementNode = candidateDefinition;
  } else {
    // Handle the case where there's not a duplicate definition.
    replacementNode = createDeclarationNode();
    if (firstModule == minimumModule) {
      firstNode.getParent().addChildBefore(replacementNode, firstNode);
    } else {
      // In this case, the name was implicitly provided by two independent
      // modules. We need to move this code up to a common module.
      int indexOfDot = namespace.indexOf('.');
      if (indexOfDot == -1) {
        // Any old place is fine.
        compiler.getNodeForCodeInsertion(minimumModule)
            .addChildToBack(replacementNode);
      } else {
        // Add it after the parent namespace.
        ProvidedName parentName =
            providedNames.get(namespace.substring(0, indexOfDot));
        Preconditions.checkNotNull(parentName);
        Preconditions.checkNotNull(parentName.replacementNode);
        parentName.replacementNode.getParent().addChildAfter(
            replacementNode, parentName.replacementNode);
      }
    }
    if (explicitNode != null) {
      explicitNode.detachFromParent();
    }
    compiler.reportCodeChange();
  }
}