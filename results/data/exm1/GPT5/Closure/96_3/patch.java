private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    // Collect parameters to allow referencing the last parameter for var_args
    java.util.List<Node> paramList = new java.util.ArrayList<Node>();
    for (Node p : functionType.getParameters()) {
      paramList.add(p);
    }

    boolean isVarArgs = (functionType.getMaxArguments() == Integer.MAX_VALUE);

    int ordinal = 0;
    Node parameter = null;
    Node argument = null;
    int paramCount = paramList.size();

    while (arguments.hasNext()) {
      argument = arguments.next();
      ordinal++;

      if (ordinal <= paramCount) {
        parameter = paramList.get(ordinal - 1);
      } else if (isVarArgs && paramCount > 0) {
        // For var_args, use the last parameter type for all extra arguments
        parameter = paramList.get(paramCount - 1);
      } else {
        // No parameter to match; stop type-matching and fall through to count check
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