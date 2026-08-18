  private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node parameter = null;
    Node lastParameter = null;
    Node argument = null;

    // Validate each argument against the corresponding parameter. If the
    // function is variadic, validate extra arguments against the var_args
    // parameter type (the last parameter).
    while (arguments.hasNext()) {
      if (parameters.hasNext()) {
        parameter = parameters.next();
        lastParameter = parameter;
      } else if (functionType.getMaxArguments() == Integer.MAX_VALUE && lastParameter != null) {
        // Reuse the var_args parameter for additional arguments.
        parameter = lastParameter;
      } else {
        // Too many arguments for a non-variadic function. Stop type checks;
        // the count check below will report the error.
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
    if (minArgs > numArgs || maxArgs < numArgs) {
      report(t, call, WRONG_ARGUMENT_COUNT,
              validator.getReadableJSTypeName(call.getFirstChild(), false),
              String.valueOf(numArgs), String.valueOf(minArgs),
              maxArgs != Integer.MAX_VALUE ?
              " and no more than " + maxArgs + " argument(s)" : "");
    }
  }