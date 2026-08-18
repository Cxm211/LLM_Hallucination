private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node parameter = null;
    Node argument = null;
    Node varArgParameter = null; // track the last parameter for var_args
    while (arguments.hasNext() &&
           parameters.hasNext()) {
        parameter = parameters.next();
        varArgParameter = parameter;
        argument = arguments.next();
        ordinal++;
        validator.expectArgumentMatchesParameter(t, argument,
            getJSType(argument), getJSType(parameter), call, ordinal);
    }

    // handle remaining arguments for var_args functions
    if (functionType.isVarArgs() && arguments.hasNext()) {
        while (arguments.hasNext()) {
            argument = arguments.next();
            ordinal++;
            validator.expectArgumentMatchesParameter(t, argument,
                getJSType(argument), getJSType(varArgParameter), call, ordinal);
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