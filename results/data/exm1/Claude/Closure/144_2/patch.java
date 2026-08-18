FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    returnType = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) :
        typeRegistry.getNativeType(UNKNOWN_TYPE);
    if (templateTypeName != null) {
      JSType restrictedType = returnType.restrictByNotNullOrUndefined();
      if (restrictedType != null && restrictedType.isTemplateType()) {
        reportError(TEMPLATE_TYPE_EXPECTED, fnName);
      }
    }
    return this;
  }