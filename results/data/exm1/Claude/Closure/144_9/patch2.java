private FunctionType getFunctionType(String name,
    Node rValue, JSDocInfo info, @Nullable Node lvalueNode) {
  FunctionType functionType = null;

  if (rValue != null && rValue.isQualifiedName()) {
    Var var = scope.getVar(rValue.getQualifiedName());
    if (var != null && var.getType() instanceof FunctionType) {
      functionType = (FunctionType) var.getType();
      if (functionType != null && functionType.isConstructor()) {
        typeRegistry.declareType(name, functionType.getInstanceType());
      }
    }
    return functionType;
  }

  Node owner = null;
  if (lvalueNode != null) {
    owner = getPrototypePropertyOwner(lvalueNode);
  }

  Node errorRoot = rValue == null ? lvalueNode : rValue;
  boolean isFnLiteral =
      rValue != null && rValue.getType() == Token.FUNCTION;
  Node fnRoot = isFnLiteral ? rValue : null;
  Node parametersNode = isFnLiteral ?
      rValue.getFirstChild().getNext() : null;

  if (functionType == null && info != null && info.hasType()) {
    JSType type = info.getType().evaluate(scope, typeRegistry);

    if (type != null) {
      type = type.restrictByNotNullOrUndefined();
      if (type.isFunctionType()) {
        functionType = (FunctionType) type;
        functionType.setJSDocInfo(info);
      }
    }
  }

  if (functionType == null) {
    if (info == null ||
        !FunctionTypeBuilder.isFunctionTypeDeclaration(info)) {
      if (lvalueNode != null && lvalueNode.getType() == Token.GETPROP &&
          lvalueNode.isQualifiedName()) {
        Var var = scope.getVar(
            lvalueNode.getFirstChild().getQualifiedName());
        if (var != null) {
          ObjectType ownerType = ObjectType.cast(var.getType());
          FunctionType propType = null;
          if (ownerType != null) {
            String propName = lvalueNode.getLastChild().getString();
            propType = findOverriddenFunction(ownerType, propName);
          }

          if (propType != null) {
            functionType =
                new FunctionTypeBuilder(
                    name, compiler, errorRoot, sourceName, scope)
                .setSourceNode(fnRoot)
                .inferFromOverriddenFunction(propType, parametersNode)
                .inferThisType(info, owner)
                .buildAndRegister();
          }
        }
      }
    }
  }

  if (functionType == null) {
    functionType =
        new FunctionTypeBuilder(name, compiler, errorRoot, sourceName,
            scope)
        .setSourceNode(fnRoot)
        .inferTemplateTypeName(info)
        .inferReturnType(info)
        .inferInheritance(info)
        .inferThisType(info, owner)
        .inferParameterTypes(parametersNode, info)
        .buildAndRegister();
  }

  if (rValue != null) {
    setDeferredType(rValue, functionType);
  }

  return functionType;
}