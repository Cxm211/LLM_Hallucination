FunctionTypeBuilder inferFromOverriddenFunction(
      @Nullable FunctionType oldType, @Nullable Node paramsParent) {
    if (oldType == null) {
      return this;
    }

    if (paramsParent == null) {
      // Not a function literal. Copy over the return type and params from the old type.
      returnType = oldType.getReturnType();
      returnTypeInferred = oldType.isReturnTypeInferred();
      parametersNode = oldType.getParametersNode();
      if (parametersNode == null) {
        parametersNode = new FunctionParamBuilder(typeRegistry).build();
      }
    } else {
      // We're overriding with a function literal. Apply type information
      // to each parameter of the literal, but do not force the return type
      // from the old function type. The return type should be inferred from
      // the literal body or JSDoc, and then checked against the overridden
      // function separately.
      FunctionParamBuilder paramBuilder =
          new FunctionParamBuilder(typeRegistry);
      Iterator<Node> oldParams = oldType.getParameters().iterator();
      boolean warnedAboutArgList = false;
      boolean oldParamsListHitOptArgs = false;
      for (Node currentParam = paramsParent.getFirstChild();
           currentParam != null; currentParam = currentParam.getNext()) {
        if (oldParams.hasNext()) {
          Node oldParam = oldParams.next();
          Node newParam = paramBuilder.newParameterFromNode(oldParam);

          oldParamsListHitOptArgs = oldParamsListHitOptArgs ||
              oldParam.isVarArgs() ||
              oldParam.isOptionalArg();

          // The subclass method might write its var_args as individual
          // arguments.
          if (currentParam.getNext() != null && newParam.isVarArgs()) {
            newParam.setVarArgs(false);
            newParam.setOptionalArg(true);
          }
        } else {
          warnedAboutArgList |= addParameter(
              paramBuilder,
              typeRegistry.getNativeType(UNKNOWN_TYPE),
              warnedAboutArgList,
              codingConvention.isOptionalParameter(currentParam) ||
                  oldParamsListHitOptArgs,
              codingConvention.isVarArgsParameter(currentParam));
        }
      }

      // Clone any remaining params that aren't in the function literal.
      while (oldParams.hasNext()) {
        Node remainingOld = oldParams.next();
        paramBuilder.newParameterFromNode(remainingOld);
      }

      parametersNode = paramBuilder.build();
    }
    return this;
  }