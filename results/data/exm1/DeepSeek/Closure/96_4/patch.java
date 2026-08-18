private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node parameter = null;
    Node argument = null;
    boolean hasVarArgs = false;
    while (arguments.hasNext() &&
           parameters.hasNext()) {
        parameter = parameters.next();
        if (parameter.isVarArgs()) {
            hasVarArgs = true;
            JSType varArgType = ((JSType) parameter.getJSType()).toMaybeArrayType().getElementsType();
            while (arguments.hasNext()) {
                argument = arguments.next();
                ordinal++;
                validator.expectArgumentMatchesParameter(t, argument,
                    getJSType(argument), varArgType, call, ordinal);
            }
            break;
        }
        argument = arguments.next();
        ordinal++;
        validator.expectArgumentMatchesParameter(t, argument,
            getJSType(argument), getJSType(parameter), call, ordinal);
    }

    int numArgs = call.getChildCount() - 1;
    int minArgs = functionType.getMinArguments();
    int maxArgs = functionType.getMaxArguments();
    if (minArgs > numArgs || (maxArgs != Integer.MAX_VALUE && maxArgs < numArgs)) {
      report(t, call, WRONG_ARGUMENT_COUNT,
              validator.getReadableJSTypeName(call.getFirstChild(), false),
              String.valueOf(numArgs), String.valueOf(minArgs),
              maxArgs != Integer.MAX_VALUE ?
              " and no more than " + maxArgs + " argument(s)" : "");
    }
  }