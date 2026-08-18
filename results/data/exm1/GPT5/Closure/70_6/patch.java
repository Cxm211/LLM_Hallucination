private void declareArguments(Node functionNode) {
  Node astParameters = functionNode.getFirstChild().getNext();
  Node body = astParameters.getNext();
  FunctionType functionType = (FunctionType) functionNode.getJSType();
  Node jsDocParameters = null;
  if (functionType != null) {
    jsDocParameters = functionType.getParametersNode();
  }
  Node jsDocParameter = jsDocParameters == null ? null : jsDocParameters.getFirstChild();
  for (Node astParameter : astParameters.children()) {
    JSType paramType = null;
    if (jsDocParameter != null) {
      paramType = jsDocParameter.getJSType();
      jsDocParameter = jsDocParameter.getNext();
    }
    defineSlot(astParameter, functionNode, paramType, true);
  }
} // end declareArguments