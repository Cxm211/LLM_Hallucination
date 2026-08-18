private void visitParameterList(NodeTraversal t, Node call,
      FunctionType functionType) {
    Iterator<Node> arguments = call.children().iterator();
    arguments.next(); // skip the function name

    Iterator<Node> parameters = functionType.getParameters().iterator();
    int ordinal = 0;
    Node parameter = null;
    Node argument = null;
    while (arguments.hasNext()) {
      // If there are no parameters left in the list, then the while loop
      // above implies that this must be a var_args function.
      Node currentParam = null;
      if (parameters.hasNext()) {
        parameter = parameters.next();
        currentParam = parameter;
      } else if (functionType.getMaxArguments() == Integer.MAX_VALUE) {
        // Var-args: reuse the last parameter for remaining arguments.
        currentParam = parameter;
      } else {
        // Extra arguments for non-var-args functions are checked by
        // the arity checks below; skip type validation.
        ordinal++;
        continue;
      }
      argument = arguments.next();
      ordinal++;

      validator.expectArgumentMatchesParameter(t, argument,
          getJSType(argument), getJSType(currentParam), call, ordinal);
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