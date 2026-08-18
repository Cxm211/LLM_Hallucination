  FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    JSType evaluatedReturnType = null;
    if (info != null && info.hasReturnType()) {
      evaluatedReturnType = info.getReturnType().evaluate(scope, typeRegistry);
    }
    returnType = evaluatedReturnType != null ? evaluatedReturnType :
        typeRegistry.getNativeType(UNKNOWN_TYPE);
    if (templateTypeName != null &&
        returnType.restrictByNotNullOrUndefined().isTemplateType()) {
      reportError(TEMPLATE_TYPE_EXPECTED, fnName);
    }
    return this;
  }