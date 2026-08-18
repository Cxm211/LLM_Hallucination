// ===== FIXED com.google.javascript.jscomp.FunctionTypeBuilder :: buildAndRegister() [lines 503-541] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-144-fixed/src/com/google/javascript/jscomp/FunctionTypeBuilder.java =====
  FunctionType buildAndRegister() {
    if (returnType == null) {
      returnType = typeRegistry.getNativeType(UNKNOWN_TYPE);
    }

    if (parametersNode == null) {
      throw new IllegalStateException(
          "All Function types must have params and a return type");
    }

    FunctionType fnType;
    if (isConstructor) {
      fnType = getOrCreateConstructor();
    } else if (isInterface) {
      fnType = typeRegistry.createInterfaceType(fnName, sourceNode);
      if (scope.isGlobal() && !fnName.isEmpty()) {
        typeRegistry.declareType(fnName, fnType.getInstanceType());
      }
      maybeSetBaseType(fnType);
    } else {
      fnType = new FunctionBuilder(typeRegistry)
          .withName(fnName)
          .withSourceNode(sourceNode)
          .withParamsNode(parametersNode)
          .withReturnType(returnType, returnTypeInferred)
          .withTypeOfThis(thisType)
          .withTemplateName(templateTypeName)
          .build();
      maybeSetBaseType(fnType);
    }

    if (implementedInterfaces != null) {
      fnType.setImplementedInterfaces(implementedInterfaces);
    }

    typeRegistry.clearTemplateTypeName();

    return fnType;
  }

// ===== FIXED com.google.javascript.jscomp.FunctionTypeBuilder :: inferReturnType(JSDocInfo) [lines 199-208] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-144-fixed/src/com/google/javascript/jscomp/FunctionTypeBuilder.java =====
  FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    returnType = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) : null;
    if (templateTypeName != null &&
        returnType != null &&
        returnType.restrictByNotNullOrUndefined().isTemplateType()) {
      reportError(TEMPLATE_TYPE_EXPECTED, fnName);
    }
    return this;
  }

// ===== FIXED com.google.javascript.rhino.jstype.FunctionType :: cloneWithNewReturnType(JSType, boolean) [lines 515-521] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-144-fixed/src/com/google/javascript/rhino/jstype/FunctionType.java =====
  FunctionType cloneWithNewReturnType(JSType newReturnType, boolean inferred) {
    return new FunctionType(
        registry, null, source,
        new ArrowType(
            registry, call.parameters, newReturnType, inferred),
        typeOfThis, null, false, false);
  }
