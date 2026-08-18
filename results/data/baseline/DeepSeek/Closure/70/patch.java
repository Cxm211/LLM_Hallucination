private void declareArguments(Node functionNode) {
  Node astParameters = functionNode.getFirstChild().getNext();
  Node body = astParameters.getNext();
  FunctionType functionType = (FunctionType) functionNode.getJSType();
  if (functionType != null) {
    Node jsDocParameters = functionType.getParametersNode();
    if (jsDocParameters != null) {
      Node jsDocParameter = jsDocParameters.getFirstChild();
      for (Node astParameter : astParameters.children()) {
        // Handle rest parameters and default parameters
        Node slotParameter = astParameter;
        if (astParameter.isRest()) {
          slotParameter = astParameter.getFirstChild();
        } else if (astParameter.isDefault()) {
          slotParameter = astParameter.getFirstChild();
        }
        if (jsDocParameter != null) {
          defineSlot(slotParameter, functionNode,
              jsDocParameter.getJSType(), true);
          jsDocParameter = jsDocParameter.getNext();
        } else {
          defineSlot(slotParameter, functionNode, null, true);
        }
      }
    }
  }
}