  private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    // Check if the call is a .call or .apply and skip the 'this' argument.
    Node callee = call.getFirstChild();
    boolean isCallOrApply = false;
    int skipCount = 0;
    if (callee.isGetProp()) {
      String prop = callee.getLastChild().getString();
      if ("call".equals(prop)) {
        isCallOrApply = true;
        skipCount = 1;
      } else if ("apply".equals(prop)) {
        isCallOrApply = true;
        skipCount = 2;
      }
    }

    for (int i = 0; i < skipCount && arguments.hasNext(); i++) {
      arguments.next();
    }

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = skipCount; // offset ordinal by skipped arguments to match original numbering
    Node parameter = null;
    Node argument = null;
    while (arguments.hasNext() &&
           parameters.hasNext()) {
      // If there are no parameters left in the list, then the while loop
      // above implies that this must be a var_args function.
        parameter = parameters.next();
      argument = arguments.next();
      ordinal++;

      validator.expectArgumentMatchesParameter(t, argument,
          getJSType(argument), getJSType(parameter), call, ordinal);
    }

    int numArgs = call.getChildCount() - 1 - skipCount;
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