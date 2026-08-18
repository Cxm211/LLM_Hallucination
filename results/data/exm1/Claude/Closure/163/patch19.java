private void moveMethods(Collection<NameInfo> allNameInfo) {
  boolean hasStubDeclaration = idGenerator.hasGeneratedAnyIds();
  for (NameInfo nameInfo : allNameInfo) {
    if (!nameInfo.isReferenced()) {
      continue;
    }

    if (nameInfo.readsClosureVariables()) {
      continue;
    }

    JSModule deepestCommonModuleRef = nameInfo.getDeepestCommonModuleRef();
    if (deepestCommonModuleRef == null) {
      compiler.report(JSError.make(NULL_COMMON_MODULE_ERROR));
      continue;
    }

    Iterator<Symbol> declarations =
        nameInfo.getDeclarations().descendingIterator();
    while (declarations.hasNext()) {
      Symbol symbol = declarations.next();
      if (!(symbol instanceof Property)) {
        continue;
      }
      Property prop = (Property) symbol;

      Node value = prop.getValue();
      if (moduleGraph.dependsOn(deepestCommonModuleRef, prop.getModule()) &&
          value.isFunction()) {
        Node valueParent = value.getParent();
        if (valueParent.isGetterDef()
            || valueParent.isSetterDef()) {
          continue;
        }
        Node proto = prop.getPrototype();
        int stubId = idGenerator.newId();

        Node stubCall = IR.call(
            IR.name(STUB_METHOD_NAME),
            IR.number(stubId))
            .copyInformationFromForTree(value);
        stubCall.putBooleanProp(Node.FREE_CALL, true);

        valueParent.replaceChild(value, stubCall);

        Node unstubParent = compiler.getNodeForCodeInsertion(
            deepestCommonModuleRef);
        Node unstubCall = IR.call(
            IR.name(UNSTUB_METHOD_NAME),
            IR.number(stubId),
            value);
        unstubCall.putBooleanProp(Node.FREE_CALL, true);
        unstubParent.addChildToFront(
            IR.exprResult(
                IR.assign(
                    IR.getprop(
                        proto.cloneTree(),
                        IR.string(nameInfo.name)),
                    unstubCall))
                .copyInformationFromForTree(value));

        compiler.reportCodeChange();
      }
    }
  }

  if (!hasStubDeclaration && idGenerator.hasGeneratedAnyIds()) {
    Node declarations = compiler.parseSyntheticCode(STUB_DECLARATIONS);
    compiler.getNodeForCodeInsertion(null).addChildrenToFront(
        declarations.removeChildren());
  }
}