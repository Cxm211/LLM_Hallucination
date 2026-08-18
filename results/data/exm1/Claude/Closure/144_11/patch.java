FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    returnType = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) :
        typeRegistry.getNativeType(UNKNOWN_TYPE);
    if (templateTypeName != null &&
        returnType.restrictByNotNullOrUndefined().isTemplateType() &&
        returnType.toMaybeTemplateType().getReferenceName().equals(templateTypeName)) {
      reportError(TEMPLATE_TYPE_EXPECTED, fnName);
    }
    return this;
  }