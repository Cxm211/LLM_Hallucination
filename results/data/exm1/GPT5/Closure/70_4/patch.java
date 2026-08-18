private void declareArguments(Node functionNode) {
      Node astParameters = functionNode.getFirstChild().getNext();
      FunctionType functionType = (FunctionType) functionNode.getJSType();

      Node jsDocParameter = null;
      if (functionType != null) {
        Node jsDocParameters = functionType.getParametersNode();
        if (jsDocParameters != null) {
          jsDocParameter = jsDocParameters.getFirstChild();
        }
      }

      for (Node astParameter : astParameters.children()) {
        JSType paramType = (jsDocParameter != null) ? jsDocParameter.getJSType() : null;
        defineSlot(astParameter, functionNode, paramType, true);
        if (jsDocParameter != null) {
          jsDocParameter = jsDocParameter.getNext();
        }
      }
    } // end declareArguments