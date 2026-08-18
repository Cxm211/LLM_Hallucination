  FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    JSType type = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) : null;
    returnType = type != null ? type : typeRegistry.getNativeType(UNKNOWN_TYPE);
    if (templateTypeName != null &&
        returnType.restrictByNotNullOrUndefined().isTemplateType()) {
      reportError(TEMPLATE_TYPE_EXPECTED, fnName);
    }
    return this;
  }