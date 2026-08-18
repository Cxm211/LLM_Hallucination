  private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node parameter = null;
    Node argument = null;
    boolean isVarArgs = functionType.getMaxArguments() == Integer.MAX_VALUE;
    while (arguments.hasNext()) {
      argument = arguments.next();
      if (parameters.hasNext()) {
        parameter = parameters.next();
      } else {
        if (!isVarArgs) {
          // No more parameters and not varargs; stop validating here.
          break;
        }
        // For varargs, continue to validate against the last parameter type.
        // 'parameter' remains as the last formal parameter node.
      }
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