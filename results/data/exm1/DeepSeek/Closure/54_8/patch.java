    void maybeDeclareQualifiedName(NodeTraversal t, JSDocInfo info,
        Node n, Node parent, Node rhsValue) {
      Node ownerNode = n.getFirstChild();
      String ownerName = ownerNode.getQualifiedName();
      String qName = n.getQualifiedName();
      String propName = n.getLastChild().getString();
      Preconditions.checkArgument(qName != null && ownerName != null);

      JSType valueType = getDeclaredType(t.getSourceName(), info, n, rhsValue);
      if (valueType == null && rhsValue != null) {
        valueType = rhsValue.getJSType();
      }

      if ("prototype".equals(propName)) {
        Var qVar = scope.getVar(qName);
        if (qVar != null) {
          if (!qVar.isTypeInferred()) {
            // If the programmer has declared that F inherits from Super,
            // and they assign F.prototype to some arbitrary expression,
            // there's not much we can do. We just ignore the expression,
            // and hope they've annotated their code in a way to tell us
            // what props are going to be on that prototype.
            // But we still need to process the declaration if it is
            // something that can be used for type inference.
            // So we do not return early; instead we continue to allow
            // the type to be declared.
          }
          if (qVar.getScope() == scope) {
            scope.undeclare(qVar);
          }
        }
      }

      if (valueType == null) {
        if (parent.getType() == Token.EXPR_RESULT) {
          stubDeclarations.add(new StubDeclaration(
              n,
              t.getInput() != null && t.getInput().isExtern(),
              ownerName));
        }
        return;
      }

      boolean inferred = true;
      if (info != null) {
        inferred = !(info.hasType()
            || info.hasEnumParameterType()
            || (info.isConstant() && valueType != null
                && !valueType.isUnknownType())
            || FunctionTypeBuilder.isFunctionTypeDeclaration(info));
      }

      if (inferred) {
        inferred = !(rhsValue != null &&
            rhsValue.getType() == Token.FUNCTION &&
            (info != null || !scope.isDeclared(qName, false)));
      }

      if (!inferred) {
        ObjectType ownerType = getObjectSlot(ownerName);
        if (ownerType != null) {
          boolean isExtern = t.getInput() != null && t.getInput().isExtern();
          if ((!ownerType.hasOwnProperty(propName) ||
               ownerType.isPropertyTypeInferred(propName)) &&
              ((isExtern && !ownerType.isNativeObjectType()) ||
               !ownerType.isInstanceType())) {
            ownerType.defineDeclaredProperty(propName, valueType, n);
          }
        }
        defineSlot(n, parent, valueType, inferred);
      } else if (rhsValue != null &&
          rhsValue.getType() == Token.TRUE) {
        FunctionType ownerType =
            JSType.toMaybeFunctionType(getObjectSlot(ownerName));
        if (ownerType != null) {
          JSType ownerTypeOfThis = ownerType.getTypeOfThis();
          String delegateName = codingConvention.getDelegateSuperclassName();
          JSType delegateType = delegateName == null ?
              null : typeRegistry.getType(delegateName);
          if (delegateType != null &&
              ownerTypeOfThis.isSubtype(delegateType)) {
            defineSlot(n, parent, getNativeType(BOOLEAN_TYPE), true);
          }
        }
      }
    }