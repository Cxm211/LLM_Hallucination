private boolean inferTemplatedTypesForCall(
      Node n, FunctionType fnType) {
    final ImmutableList<TemplateType> keys = fnType.getTemplateTypeMap()
        .getTemplateKeys();
    if (keys.isEmpty()) {
      return false;
    }

    Map<TemplateType, JSType> inferred = 
        inferTemplateTypesFromParameters(fnType, n);

    TemplateTypeReplacer replacer = new TemplateTypeReplacer(
        registry, inferred);
    Node callTarget = n.getFirstChild();

    FunctionType replacementFnType = fnType.visit(replacer)
        .toMaybeFunctionType();
    Preconditions.checkNotNull(replacementFnType);

    if (replacer.madeChanges) {
      callTarget.setJSType(replacementFnType);
      n.setJSType(replacementFnType.getReturnType());
    }

    return replacer.madeChanges;
  }