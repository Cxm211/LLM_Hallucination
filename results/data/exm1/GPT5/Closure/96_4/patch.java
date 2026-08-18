  private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node parameter = null;
    Node argument = null;

    // Track the last parameter for varargs, if applicable.
    Node varArgsParameter = null;

    while (arguments.hasNext() && parameters.hasNext()) {
      parameter = parameters.next();
      // If this is the last declared parameter and the function is varargs,
      // remember it so that we can type-check any remaining arguments against it.
      if (!parameters.hasNext() && functionType.isVarArgs()) {
        varArgsParameter = parameter;
      }
      argument = arguments.next();
      ordinal++;

      validator.expectArgumentMatchesParameter(t, argument,
          getJSType(argument), getJSType(parameter), call, ordinal);
    }

    // If there are remaining arguments and the function is varargs, validate
    // them against the varargs parameter type.
    if (arguments.hasNext() && varArgsParameter != null) {
      while (arguments.hasNext()) {
        argument = arguments.next();
        ordinal++;
        validator.expectArgumentMatchesParameter(t, argument,
            getJSType(argument), getJSType(varArgsParameter), call, ordinal);
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