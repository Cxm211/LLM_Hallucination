private boolean inferTemplatedTypesForCall(
      Node n, FunctionType fnType) {
    final ImmutableList<TemplateType> keys = fnType.getTemplateTypeMap()
        .getTemplateKeys();
    if (keys.isEmpty()) {
      return false;
    }

    Map<TemplateType, JSType> inferred = 
        inferTemplateTypesFromParameters(fnType, n);
    if (inferred == null) {
      inferred = Collections.emptyMap();
    }

    TemplateTypeReplacer replacer = new TemplateTypeReplacer(
        registry, inferred);
    Node callTarget = n.getFirstChild();

    JSType visited = fnType.visit(replacer);
    FunctionType replacementFnType = (visited == null) ? null : visited.toMaybeFunctionType();
    if (replacementFnType == null) {
      return false;
    }

    callTarget.setJSType(replacementFnType);
    n.setJSType(replacementFnType.getReturnType());

    return replacer.madeChanges;
  }