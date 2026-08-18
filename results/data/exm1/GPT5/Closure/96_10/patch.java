  private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    // Collect parameters so we can handle var_args by reusing the
    // last parameter type for extra arguments.
    List<Node> params = new java.util.ArrayList<Node>();
    for (Node p : functionType.getParameters()) {
      params.add(p);
    }

    int ordinal = 0;
    int index = 0;
    Node parameter = null;
    while (arguments.hasNext()) {
      Node argument = arguments.next();
      ordinal++;

      if (index < params.size()) {
        parameter = params.get(index);
      } else if (functionType.getMaxArguments() == Integer.MAX_VALUE && !params.isEmpty()) {
        // Varargs: use the last parameter for type checking extra args.
        parameter = params.get(params.size() - 1);
      } else {
        // No matching parameter (too many args, non-varargs). Stop type checks.
        break;
      }

      validator.expectArgumentMatchesParameter(t, argument,
          getJSType(argument), getJSType(parameter), call, ordinal);
      index++;
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