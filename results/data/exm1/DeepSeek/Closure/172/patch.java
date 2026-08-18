private boolean isQualifiedNameInferred(
    String qName, Node n, JSDocInfo info,
    Node rhsValue, JSType valueType) {
  if (valueType == null) {
    return true;
  }

  // Prototypes of constructors and interfaces are always declared.
  if (qName != null && qName.endsWith(".prototype")) {
    return false;
  }

  boolean inferred = true;
  if (info != null) {
    inferred = !(info.hasType()
        || info.hasEnumParameterType()
        || (isConstantSymbol(info, n) && valueType != null
            && !valueType.isUnknownType())
        || FunctionTypeBuilder.isFunctionTypeDeclaration(info));
  }

  if (inferred && rhsValue != null && rhsValue.isFunction()) {
    // If the function is assigned in a conditional block or inner scope,
    // it should be considered inferred.
    if (info != null || (!scope.isDeclared(qName, false) &&
        n.isUnscopedQualifiedName())) {

      // Check if this is in a conditional block.
      for (Node current = n.getParent();
           !(current.isScript() || current.isFunction());
           current = current.getParent()) {
        if (NodeUtil.isControlStructure(current)) {
          return true;
        }
      }

      // Check if this is assigned in an inner scope.
      AstFunctionContents contents =
          getFunctionAnalysisResults(scope.getRootNode());
      if (contents == null ||
          !contents.getEscapedQualifiedNames().contains(qName)) {
        return false;
      }
    }
  }
  return inferred;
}