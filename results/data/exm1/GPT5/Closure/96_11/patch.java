private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    if (arguments.hasNext()) {
      arguments.next(); // skip the function name
    }

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node lastParameter = null;

    while (arguments.hasNext() && parameters.hasNext()) {
      lastParameter = parameters.next();
      Node argument = arguments.next();
      ordinal++;

      validator.expectArgumentMatchesParameter(t, argument,
          getJSType(argument), getJSType(lastParameter), call, ordinal);
    }

    // If there are remaining arguments and the function is var_args, then
    // validate the remaining arguments against the last parameter type.
    if (arguments.hasNext() && functionType.isVarArgs()) {
      JSType varArgType = (lastParameter != null) ? getJSType(lastParameter) : null;
      while (arguments.hasNext()) {
        Node argument = arguments.next();
        ordinal++;
        if (varArgType != null) {
          validator.expectArgumentMatchesParameter(t, argument,
              getJSType(argument), varArgType, call, ordinal);
        }
      }
    }

    int numArgs = call.getChildCount() - 1;
    int minArgs = functionType.getMinArguments();
    int maxArgs = functionType.getMaxArguments();
    if (minArgs > numArgs || maxArgs < numArgs) {
      report(t, call, WRONG_ARGUMENT_COUNT,
              validator.getReadableJSTypeName(call.getFirstChild(), false),
              String.valueOf(numArgs), String.valueOf(minArgs),
              maxArgs != Integer.MAX_VALUE ?
              " and no more than " + maxArgs + " argument(s)" : "");
    }
  }