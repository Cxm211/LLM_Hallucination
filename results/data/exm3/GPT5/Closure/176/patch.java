private void updateScopeForTypeChange(
      FlowScope scope, Node left, JSType leftType, JSType resultType) {
    Preconditions.checkNotNull(resultType);
    switch (left.getType()) {
      case Token.NAME:
        String varName = left.getString();
        Var var = syntacticScope.getVar(varName);
        boolean isVarDeclaration = left.hasChildren();

        // When looking at VAR initializers, always update the flow scope with the
        // initializer/result type so that subsequent property accesses reflect the
        // actual runtime value at that program point.
        boolean isVarTypeBetter = true;
            // Makes it easier to check for NPEs.

        if (isVarTypeBetter) {
          redeclareSimpleVar(scope, left, resultType);
        }
        left.setJSType(isVarDeclaration || leftType == null ?
            resultType : null);

        if (var != null && var.isTypeInferred()) {
          JSType oldType = var.getType();
          var.setType(oldType == null ?
              resultType : oldType.getLeastSupertype(resultType));
        }
        break;
      case Token.GETPROP:
        String qualifiedName = left.getQualifiedName();
        if (qualifiedName != null) {
          scope.inferQualifiedSlot(left, qualifiedName,
              leftType == null ? unknownType : leftType,
              resultType);
        }

        left.setJSType(resultType);
        ensurePropertyDefined(left, resultType);
        break;
    }
  }