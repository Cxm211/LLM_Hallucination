private void declareArguments(Node functionNode) {
      Node astParameters = functionNode.getFirstChild().getNext();
      FunctionType functionType = (FunctionType) functionNode.getJSType();
      Node jsDocParameters = functionType != null ? functionType.getParametersNode() : null;
      Node jsDocParameter = jsDocParameters != null ? jsDocParameters.getFirstChild() : null;
      for (Node astParameter : astParameters.children()) {
        JSType paramType = jsDocParameter != null ? jsDocParameter.getJSType() : null;
        defineSlot(astParameter, functionNode, paramType, true);
        if (jsDocParameter != null) {
          jsDocParameter = jsDocParameter.getNext();
        }
      }
    } // end declareArguments