private boolean inferTemplatedTypesForCall(
      Node n, FunctionType fnType) {
    final ImmutableList<TemplateType> keys = fnType.getTemplateTypeMap()
        .getTemplateKeys();
    if (keys.isEmpty()) {
      return false;
    }

    // Try to infer the template types
    Map<TemplateType, JSType> inferred = 
        inferTemplateTypesFromParameters(fnType, n);

    // Replace all template types. If we couldn't find a replacement, we
    // replace it with UNKNOWN.
    TemplateTypeReplacer replacer = new TemplateTypeReplacer(
        registry, inferred);
    Node callTarget = n.getFirstChild();

    FunctionType replacementFnType = fnType.visit(replacer)
        .toMaybeFunctionType();
    Preconditions.checkNotNull(replacementFnType);

    callTarget.setJSType(replacementFnType);

    // Only update the call's result type if it would improve (i.e., not
    // overwrite a more precise existing type with unknown/null).
    JSType replacementReturn = replacementFnType.getReturnType();
    JSType existingReturn = n.getJSType();
    if (replacementReturn != null) {
      if (existingReturn == null || existingReturn.isUnknownType()) {
        n.setJSType(replacementReturn);
      } else if (!replacementReturn.isUnknownType()) {
        n.setJSType(replacementReturn);
      }
    }

    return replacer.madeChanges;
  }