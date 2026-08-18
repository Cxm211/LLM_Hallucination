private boolean isQualifiedNameInferred(
        String qName, Node n, JSDocInfo info,
        Node rhsValue, JSType valueType) {
      if (valueType == null) {
        return true;
      }

      // Prototypes of constructors and interfaces are always declared.
      // However, if the base object is not a constructor/interface type,
      // then the prototype property should be inferred.
      if (qName != null && qName.endsWith(".prototype")) {
          // Check if the base is actually a constructor/interface
          int lastDot = qName.lastIndexOf('.');
          if (lastDot > 0) {
              String baseName = qName.substring(0, lastDot);
              JSType baseType = scope.getVar(baseName) != null ? scope.getVar(baseName).getType() : null;
              if (baseType != null && (baseType.isConstructor() || baseType.isInterface())) {
                  return false;
              }
          }
          // If base is not a constructor/interface, treat as inferred
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
        if (info != null) {
          return false;
        } else if (!scope.isDeclared(qName, false) &&
            n.isUnscopedQualifiedName()) {

          // Check if this is in a conditional block.
          // Functions assigned in conditional blocks are inferred.
          for (Node current = n.getParent();
               !(current.isScript() || current.isFunction());
               current = current.getParent()) {
            if (NodeUtil.isControlStructure(current)) {
              return true;
            }
          }

          // Check if this is assigned in an inner scope.
          // Functions assigned in inner scopes are inferred.
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