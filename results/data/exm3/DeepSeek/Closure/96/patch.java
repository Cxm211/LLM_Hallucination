  private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    // Collect parameters into a list for easier access.
    List<Node> paramList = new ArrayList<>();
    for (Iterator<Node> it = functionType.getParameters().iterator(); it.hasNext();) {
      paramList.add(it.next());
    }
    int numParams = paramList.size();
    boolean isVarArgs = functionType.getMaxArguments() == Integer.MAX_VALUE;

    int ordinal = 0;
    int paramIndex = 0;
    while (arguments.hasNext()) {
      Node argument = arguments.next();
      ordinal++;
      Node parameter = null;
      if (paramIndex < numParams) {
        parameter = paramList.get(paramIndex);
        paramIndex++;
      } else if (isVarArgs && numParams > 0) {
        // For a var_args function, use the last parameter type for extra arguments.
        parameter = paramList.get(numParams - 1);
      } else {
        // No more parameters and not var_args, stop processing arguments.
        // The argument count error will be reported below.
        break;
      }
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