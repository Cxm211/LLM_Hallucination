FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
  if (info != null && info.hasReturnType()) {
    returnType = info.getReturnType().evaluate(scope, typeRegistry);
  } else {
    returnType = typeRegistry.getNativeType(VOID_TYPE);
  }
  if (templateTypeName != null &&
      returnType.restrictByNotNullOrUndefined().isTemplateType()) {
    reportError(TEMPLATE_TYPE_EXPECTED, fnName);
  }
  return this;
}