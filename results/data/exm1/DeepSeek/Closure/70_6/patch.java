private void declareArguments(Node functionNode) {
      Node astParameters = functionNode.getFirstChild().getNext();
      Node body = astParameters.getNext();
      JSType type = functionNode.getJSType();
      if (type instanceof FunctionType) {
        FunctionType functionType = (FunctionType) type;
        Node jsDocParameters = functionType.getParametersNode();
        if (jsDocParameters != null) {
          Node jsDocParameter = jsDocParameters.getFirstChild();
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
      }
    }