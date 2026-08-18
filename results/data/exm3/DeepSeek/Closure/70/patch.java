    private void declareArguments(Node functionNode) {
      Node astParameters = functionNode.getFirstChild().getNext();
      Node body = astParameters.getNext();
      FunctionType functionType = (FunctionType) functionNode.getJSType();
      Node jsDocParameters = null;
      Node jsDocParameter = null;
      if (functionType != null) {
        jsDocParameters = functionType.getParametersNode();
        if (jsDocParameters != null) {
          jsDocParameter = jsDocParameters.getFirstChild();
        }
      }
      for (Node astParameter : astParameters.children()) {
        if (jsDocParameter != null) {
          defineSlot(astParameter, functionNode,
              jsDocParameter.getJSType(), true);
          jsDocParameter = jsDocParameter.getNext();
        } else {
          defineSlot(astParameter, functionNode, null, true);
        }
      }
    }