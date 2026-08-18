FunctionTypeBuilder inferReturnType(@Nullable JSDocInfo info) {
    returnType = info != null && info.hasReturnType() ?
        info.getReturnType().evaluate(scope, typeRegistry) :
        typeRegistry.getNativeType(UNKNOWN_TYPE);
    return this;
  }