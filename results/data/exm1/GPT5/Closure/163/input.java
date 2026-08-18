// buggy code
    private ProcessProperties() {
      symbolStack.push(new NameContext(globalNode));
    }

    public void enterScope(NodeTraversal t) {
      symbolStack.peek().scope = t.getScope();
          // NOTE(nicksantos): We use the same anonymous node for all
          // functions that do not have reasonable names. I can't remember
          // at the moment why we do this. I think it's because anonymous
          // nodes can never have in-edges. They're just there as a placeholder
          // for scope information, and do not matter in the edge propagation.
    }

    public void exitScope(NodeTraversal t) {
    }

    public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      // Process prototype assignments to non-functions.
      if (isPrototypePropertyAssign(n)) {
        symbolStack.push(new NameContext(getNameInfoForName(
                n.getFirstChild().getLastChild().getString(), PROPERTY)));
      } else if (isGlobalFunctionDeclaration(t, n)) {
        String name = parent.isName() ?
            parent.getString() /* VAR */ :
            n.getFirstChild().getString() /* named function */;
        symbolStack.push(new NameContext(getNameInfoForName(name, VAR)));
      } else if (n.isFunction()) {
        symbolStack.push(new NameContext(anonymousNode));
      }
      return true;
    }

    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isGetProp()) {
        String propName = n.getFirstChild().getNext().getString();

          if (propName.equals("prototype")) {
          processPrototypeParent(t, parent);
          } else if (compiler.getCodingConvention().isExported(propName)) {
            addGlobalUseOfSymbol(propName, t.getModule(), PROPERTY);
          } else {
            // Do not mark prototype prop assigns as a 'use' in the global scope.
          addSymbolUse(propName, t.getModule(), PROPERTY);
        }

      } else if (n.isObjectLit() &&
        // Make sure that we're not handling object literals being
        // assigned to a prototype, as in:
        // Foo.prototype = {bar: 3, baz: 5};
          !(parent.isAssign() &&
            parent.getFirstChild().isGetProp() &&
            parent.getFirstChild().getLastChild().getString().equals(
                "prototype"))) {

        // var x = {a: 1, b: 2}
        // should count as a use of property a and b.
        for (Node propNameNode = n.getFirstChild(); propNameNode != null;
             propNameNode = propNameNode.getNext()) {
          // May be STRING, GET, or SET, but NUMBER isn't interesting.
          if (!propNameNode.isQuotedString()) {
            addSymbolUse(propNameNode.getString(), t.getModule(), PROPERTY);
          }
        }
      } else if (n.isName()) {
        String name = n.getString();

        Var var = t.getScope().getVar(name);
        if (var != null) {
          // Only process global functions.
          if (var.isGlobal()) {
            if (var.getInitialValue() != null &&
                var.getInitialValue().isFunction()) {
              if (t.inGlobalScope()) {
                if (!processGlobalFunctionDeclaration(t, n, parent,
                        parent.getParent())) {
                  addGlobalUseOfSymbol(name, t.getModule(), VAR);
                }
              } else {
                addSymbolUse(name, t.getModule(), VAR);
              }
            }

          // If it is not a global, it might be accessing a local of the outer
          // scope. If that's the case the functions between the variable's
          // declaring scope and the variable reference scope cannot be moved.
          } else if (var.getScope() != t.getScope()){
            for (int i = symbolStack.size() - 1; i >= 0; i--) {
              NameContext context = symbolStack.get(i);
              if (context.scope == var.getScope()) {
                break;
              }

              context.name.readClosureVariables = true;
            }
          }
        }
      }

      // Process prototype assignments to non-functions.
      if (isPrototypePropertyAssign(n) ||
          isGlobalFunctionDeclaration(t, n) ||
          n.isFunction()) {
        symbolStack.pop();
      }
    }

    private void addSymbolUse(String name, JSModule module, SymbolType type) {
      NameInfo info = getNameInfoForName(name, type);
      NameInfo def = null;
      // Skip all anonymous nodes. We care only about symbols with names.
      for (int i = symbolStack.size() - 1; i >= 0; i--) {
        def = symbolStack.get(i).name;
        if (def != anonymousNode) {
          break;
        }
      }
      if (!def.equals(info)) {
        symbolGraph.connect(def, module, info);
      }
    }

    private boolean isGlobalFunctionDeclaration(NodeTraversal t, Node n) {
      // Make sure we're either in the global scope, or the function
      // we're looking at is the root of the current local scope.

      return t.inGlobalScope() &&
          (NodeUtil.isFunctionDeclaration(n) ||
           n.isFunction() &&
           n.getParent().isName());
    }

    private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && n.isGetProp()
          && assign.getParent().isExprResult()) {
        boolean isChainedProperty =
            n.getFirstChild().isGetProp();

    /**
     * Returns the name of a prototype property being assigned to this r-value.
     *
     * Returns null if this is not the R-value of a prototype property, or if
     * the R-value is used in multiple expressions (i.e., if there's
     * a prototype property assignment in a more complex expression).
     */
        if (isChainedProperty) {
          Node child = n.getFirstChild().getFirstChild().getNext();

          if (child.isString() &&
              child.getString().equals("prototype")) {
            return true;
          }
        }
      }


      return false;
    }

    private boolean processGlobalFunctionDeclaration(NodeTraversal t,
        Node nameNode, Node parent, Node gramps) {
      Node firstChild = nameNode.getFirstChild();

      if (// Check for a named FUNCTION.
          isGlobalFunctionDeclaration(t, parent) ||
          // Check for a VAR declaration.
          firstChild != null &&
          isGlobalFunctionDeclaration(t, firstChild)) {
        String name = nameNode.getString();
        getNameInfoForName(name, VAR).getDeclarations().add(
            new GlobalFunction(nameNode, parent, gramps, t.getModule()));

        // If the function name is exported, we should create an edge here
        // so that it's never removed.
        if (compiler.getCodingConvention().isExported(name) ||
            anchorUnusedVars) {
          addGlobalUseOfSymbol(name, t.getModule(), VAR);
        }

        return true;
      }
      return false;
    }

    private void processPrototypeParent(NodeTraversal t, Node n) {

      switch (n.getType()) {
        // Foo.prototype.getBar = function() { ... }
        case Token.GETPROP:
          Node dest = n.getFirstChild().getNext();
          Node parent = n.getParent();
          Node grandParent = parent.getParent();

          if (dest.isString() &&
              NodeUtil.isExprAssign(grandParent) &&
              NodeUtil.isVarOrSimpleAssignLhs(n, parent)) {
            String name = dest.getString();
            Property prop = new AssignmentProperty(
                grandParent,
                t.getModule());
            getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
          }
          break;

        // Foo.prototype = { "getBar" : function() { ... } }
        case Token.ASSIGN:
          Node map = n.getFirstChild().getNext();
          if (map.isObjectLit()) {
            for (Node key = map.getFirstChild();
                 key != null; key = key.getNext()) {
              // May be STRING, GET, or SET,
              String name = key.getString();
              Property prop = new LiteralProperty(
                  key, key.getFirstChild(), map, n,
                  t.getModule());
              getNameInfoForName(name, PROPERTY).getDeclarations().add(prop);
            }
          }
          break;
      }
    }

    public boolean traverseEdge(NameInfo start, JSModule edge, NameInfo dest) {
      if (start.isReferenced()) {
        JSModule startModule = start.getDeepestCommonModuleRef();
        if (startModule != null &&
            moduleGraph.dependsOn(startModule, edge)) {
          return dest.markReference(startModule);
        } else {
          return dest.markReference(edge);
        }
      }
      return false;
    }

    GlobalFunction(Node nameNode, Node parent, Node gramps, JSModule module) {
      Preconditions.checkState(
          parent.isVar() ||
          NodeUtil.isFunctionDeclaration(parent));
      this.nameNode = nameNode;
      this.module = module;
    }

    public void remove() {
      Node parent = nameNode.getParent();
      if (parent.isFunction() || parent.hasOneChild()) {
        NodeUtil.removeChild(parent.getParent(), parent);
      } else {
        Preconditions.checkState(parent.isVar());
        parent.removeChild(nameNode);
      }
    }

    public Node getFunctionNode() {
      Node parent = nameNode.getParent();

      if (parent.isFunction()) {
        return parent;
      } else {
        // we are the name of a var node, so the function is name's second child
        return nameNode.getChildAtIndex(1);
      }
    }

    AssignmentProperty(Node node, JSModule module) {
      this.exprNode = node;
      this.module = module;
    }

    public void remove() {
      NodeUtil.removeChild(exprNode.getParent(), exprNode);
    }

    public JSModule getModule() {
      return module;
    }

    LiteralProperty(Node key, Node value, Node map, Node assign,
        JSModule module) {
      this.key = key;
      this.value = value;
      this.map = map;
      this.assign = assign;
      this.module = module;
    }

    public void remove() {
      map.removeChild(key);
    }

    NameContext(NameInfo name) {
      this.name = name;
    }

  private void moveMethods(Collection<NameInfo> allNameInfo) {
    boolean hasStubDeclaration = idGenerator.hasGeneratedAnyIds();
    for (NameInfo nameInfo : allNameInfo) {
      if (!nameInfo.isReferenced()) {
        // The code below can't do anything with unreferenced name
        // infos.  They should be skipped to avoid NPE since their
        // deepestCommonModuleRef is null.
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

        // We should only move a property across modules if:
        // 1) We can move it deeper in the module graph, and
        // 2) it's a function, and
        // 3) it is not a get or a set, and
        // 4) the class is available in the global scope.
        //
        // #1 should be obvious. #2 is more subtle. It's possible
        // to copy off of a prototype, as in the code:
        // for (var k in Foo.prototype) {
        //   doSomethingWith(Foo.prototype[k]);
        // }
        // This is a common way to implement pseudo-multiple inheritance in JS.
        //
        // So if we move a prototype method into a deeper module, we must
        // replace it with a stub function so that it preserves its original
        // behavior.

        Node value = prop.getValue();
        if (moduleGraph.dependsOn(deepestCommonModuleRef, prop.getModule()) &&
            value.isFunction()) {
          Node valueParent = value.getParent();
          if (valueParent.isGetterDef()
              || valueParent.isSetterDef()) {
            // TODO(johnlenz): a GET or SET can't be deferred like a normal
            // FUNCTION property definition as a mix-in would get the result
            // of a GET instead of the function itself.
            continue;
          }
          Node proto = prop.getPrototype();
          int stubId = idGenerator.newId();

          // example: JSCompiler_stubMethod(id);
          Node stubCall = IR.call(
              IR.name(STUB_METHOD_NAME),
              IR.number(stubId))
              .copyInformationFromForTree(value);
          stubCall.putBooleanProp(Node.FREE_CALL, true);

          // stub out the method in the original module
          // A.prototype.b = JSCompiler_stubMethod(id);
          valueParent.replaceChild(value, stubCall);

          // unstub the function body in the deeper module
          Node unstubParent = compiler.getNodeForCodeInsertion(
              deepestCommonModuleRef);
          Node unstubCall = IR.call(
              IR.name(UNSTUB_METHOD_NAME),
              IR.number(stubId),
              value);
          unstubCall.putBooleanProp(Node.FREE_CALL, true);
          unstubParent.addChildToFront(
              // A.prototype.b = JSCompiler_unstubMethod(id, body);
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
      // Declare stub functions in the top-most module.
      Node declarations = compiler.parseSyntheticCode(STUB_DECLARATIONS);
      compiler.getNodeForCodeInsertion(null).addChildrenToFront(
          declarations.removeChildren());
    }
  }

// relevant test
// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testBasicMapping
  public void testBasicMapping() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{ \"file\" : \"testcode\"," +
                   " \"count\": 1 }\n" +

                   "[]\n" +

                   "\n" +
                   "[]\n" +

                   "\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput1
  public void testFunctionNameOutput1() throws Exception {
    checkSourceMap("function f() {}",
                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,2,3,3]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,13]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput2
  public void testFunctionNameOutput2() throws Exception {
    checkSourceMap("a.b.c = function () {};",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[3,2,2,1,1,0,4,4,4,4,4,4,4,4,5,5,6,6]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0]\n" +
                   "[\"testcode\",1,0,\"c\"]\n" +
                   "[\"testcode\",1,0,\"b\"]\n" +
                   "[\"testcode\",1,0,\"a\"]\n" +
                   "[\"testcode\",1,8,\"a.b.c\"]\n" +
                   "[\"testcode\",1,17]\n" +
                   "[\"testcode\",1,20]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput3
  public void testFunctionNameOutput3() throws Exception {
    checkSourceMap("var q = function () {};",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,1,1,2,2,2,2,2,2,2,2,3,3,4,4]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0]\n" +
                   "[\"testcode\",1,4,\"q\"]\n" +
                   "[\"testcode\",1,8,\"q\"]\n" +
                   "[\"testcode\",1,17]\n" +
                   "[\"testcode\",1,20]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput4
  public void testFunctionNameOutput4() throws Exception {
    checkSourceMap("({ 'q' : function () {} })",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,1,1,1,0,2,2,2,2,2,2,2,2,3,3,4,4,0,0]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,1]\n" +
                   "[\"testcode\",1,3]\n" +
                   "[\"testcode\",1,9,\"q\"]\n" +
                   "[\"testcode\",1,18]\n" +
                   "[\"testcode\",1,21]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,3,3,3,2,4,4,4,2,5,7,7,7,6,8,8,8,6," +
                   "9,9,9,6,10,11,11,11,11,11,11,11,12,12,12,12,5]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",1,23]\n" +
                   "[\"testcode\",1,23,\"foo\"]\n" +
                   "[\"testcode\",1,29,\"foo\"]\n" +
                   "[\"testcode\",1,35,\"bar\"]\n" +
                   "[\"testcode\",1,41]\n" +
                   "[\"testcode\",1,44]\n" +
                   "[\"testcode\",1,51,\"foo\"]\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;
    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,0,2,2,2,0,3,3,3,0,0,4,4,4,0,5,5,5,0," +
                   "6,6,6,0,0,0,0,0,0,0,0,0,7,7,7,7,0]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,23,\"foo\"]\n" +
                   "[\"testcode\",1,29,\"foo\"]\n" +
                   "[\"testcode\",1,35,\"bar\"]\n" +
                   "[\"testcode\",1,51,\"foo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,3,3,3,2,4,4,4,2,5,7,7,7,6,8,8,8," +
                   "6,9,9,9,6,10,10,10,11,11,11,11,11,11,11,12,12,12," +
                   "12,5]\n" +

                   "\n" +
                   "[]\n" +
                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",5,0]\n" +
                   "[\"testcode\",5,0,\"foo\"]\n" +
                   "[\"testcode\",5,6,\"foo\"]\n" +
                   "[\"testcode\",5,12,\"bar\"]\n" +
                   "[\"testcode\",5,18,\"foo\"]\n" +
                   "[\"testcode\",6,0]\n" +
                   "[\"testcode\",6,7,\"foo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0]\n" +

                   "\n" +
                   "[]\n" +
                   "\n" +
                   "[\"c:\\\\myfile.js\",1,0,\"foo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "" +
                   "{ \"file\" : \"testcode\", \"count\": 1 }\n" +
                   "[0,0,0,1,1,1,1,2,2,2,2]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"c:\\\\myfile.js\",1,0,\"foo\"]\n" +
                   "[\"c:\\\\myfile.js\",1,7,\"boo\"]\n" +
                   "[\"c:\\\\myfile.js\",1,14,\"goo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("c:\\myfile.js",
                   "\n" +
                   "var foo=a + 'this is a really long line that will force the"
                   + " mapping to span multiple lines 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + "' + c + d + e;",

                   "" +
                   "{ \"file\" : \"testcode\", \"count\": 6 }\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[0,0,0,0,1,1,1,1,2,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3]\n" +
                   "[4,1,5,1,6]\n" +
                   "\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "\n" +
                   "[\"c:\\\\myfile.js\",4,0]\n" +
                   "[\"c:\\\\myfile.js\",4,4,\"foo\"]\n" +
                   "[\"c:\\\\myfile.js\",4,8,\"a\"]\n" +
                   "[\"c:\\\\myfile.js\",4,12]\n" +
                   "[\"c:\\\\myfile.js\",4,1314,\"c\"]\n" +
                   "[\"c:\\\\myfile.js\",4,1318,\"d\"]\n" +
                   "[\"c:\\\\myfile.js\",4,1322,\"e\"]\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "" +
        "{ \"file\" : \"testcode\", \"count\": 6 }\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[-1,-1,-1,-1,0,0,0,0,1]\n" +
        "[2,0,3,0,4]\n" +
        "\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "\n" +
        "[\"c:\\\\myfile.js\",4,4,\"foo\"]\n" +
        "[\"c:\\\\myfile.js\",4,8,\"a\"]\n" +
        "[\"c:\\\\myfile.js\",4,1314,\"c\"]\n" +
        "[\"c:\\\\myfile.js\",4,1318,\"d\"]\n" +
        "[\"c:\\\\myfile.js\",4,1322,\"e\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testBasicMapping
  public void testBasicMapping() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testBasicMappingGoldenOutput
  public void testBasicMappingGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__() { }",

                   
                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAkBEBEB\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,18],\n" +
                   "[0,1,21],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"__BASIC__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testLiteralMappingsGoldenOutput
  public void testLiteralMappingsGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                   "var __VAR__ = '__STR__'; }",

                   
                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAkBABkBA/kCA+ADMBcBgBA9\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,18],\n" +
                   "[0,1,19,1],\n" +
                   "[0,1,31,2],\n" +
                   "[0,1,43],\n" +
                   "[0,1,45],\n" +
                   "[0,1,49,3],\n" +
                   "[0,1,59],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[" +
                       "\"__BASIC__\",\"__PARAM1__\",\"__PARAM2__\"," +
                       "\"__VAR__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"\"],\n" +
                   "\"mappings\":[],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap(
        "function f(foo, bar) { foo = foo + bar + 2; return foo; }",

        "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"cAEBABIBA/ICA+ADICA/ICA+IDA9AEYBMBA5\"],\n" +
        "\"mappings\":[[0,1,0,0],\n" +
        "[0,1,9,0],\n" +
        "[0,1,10],\n" +
        "[0,1,11,1],\n" +
        "[0,1,16,2],\n" +
        "[0,1,21],\n" +
        "[0,1,23],\n" +
        "[0,1,23,1],\n" +
        "[0,1,29,1],\n" +
        "[0,1,35,2],\n" +
        "[0,1,41],\n" +
        "[0,1,44],\n" +
        "[0,1,51,1],\n" +
        "],\n" +
        "\"sources\":[\"testcode\"],\n" +
        "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
        "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAEBA/ICA+IDE9IEA8IFA7IGg6MHA5\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,11,1],\n" +
                   "[0,1,16,2],\n" +
                   "[0,1,23,1],\n" +
                   "[0,1,29,1],\n" +
                   "[0,1,35,2],\n" +
                   "[0,1,51,1],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[" +
                       "\"cAEBABIBA/ICA+ADICA/ICA+IDA9IEYBMBA5\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,10],\n" +
                   "[0,1,11,1],\n" +
                   "[0,1,16,2],\n" +
                   "[0,1,21],\n" +
                   "[0,5,0],\n" +
                   "[0,5,0,1],\n" +
                   "[0,5,6,1],\n" +
                   "[0,5,12,2],\n" +
                   "[0,5,18,1],\n" +
                   "[0,6,0],\n" +
                   "[0,6,7,1],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"IA\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "],\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"IAMBMB\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,7,1],\n" +
                   "[0,1,14,2],\n" +
                   "],\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\",\"boo\",\"goo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("c:\\myfile.js",
                   "\n" +
                   "var foo=a + 'this is a really long line that will force the"
                   + " mapping to span multiple lines 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + "' + c + d + e;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":6,\n" +
                   "\"lineMaps\":[\"\",\n" +
                   "\"\",\n" +
                   "\"\",\n" +
                   "\"\",\n" +
                   "\"MAMBABA/!!AUSC\",\n" +
                   "\"AEA9AEA8AF\"],\n" +
                   "\"mappings\":[[0,4,0],\n" +
                   "[0,4,4,0],\n" +
                   "[0,4,8,1],\n" +
                   "[0,4,12],\n" +
                   "[0,4,1314,2],\n" +
                   "[0,4,1318,3],\n" +
                   "[0,4,1322,4],\n" +
                   "],\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
                   "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":6,\n" +
        "\"lineMaps\":[\"\",\n" +
        "\"\",\n" +
        "\"\",\n" +
        "\"\",\n" +
        "\"M/MBAB\",\n" +
        "\"ACA+ADA9AE\"],\n" +
        "\"mappings\":[[0,4,4,0],\n" +
        "[0,4,8,1],\n" +
        "[0,4,1314,2],\n" +
        "[0,4,1318,3],\n" +
        "[0,4,1322,4],\n" +
        "],\n" +
        "\"sources\":[\"c:\\\\myfile.js\"],\n" +
        "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
        "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testEncodingRelativeId
  public void testEncodingRelativeId() {
    assertEquals(0, getRelativeId(0, 0));
    assertEquals(64 + (-1), getRelativeId(-1, 0));
    assertEquals(64 + (-32), getRelativeId(0, 32));
    assertEquals(31, getRelativeId(31, 0));
    assertEquals(4096 + (-33), getRelativeId(0, 33));
    assertEquals(32, getRelativeId(32, 0));
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testEncodingIdLength
  public void testEncodingIdLength() {
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(0, 0));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(-1, 0));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(0, 32));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(31, 0));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(0, 33));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(32, 0));

    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(2047, 0));
    assertEquals(3, LineMapEncoder.getRelativeMappingIdLength(2048, 0));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(0, 2048));
    assertEquals(3, LineMapEncoder.getRelativeMappingIdLength(0, 2049));
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testEncoding
  public void testEncoding() throws IOException {
    assertEquals("AA", getEntry(0, 0, 1));
    assertEquals("EA", getEntry(0, 0, 2));
    assertEquals("8A", getEntry(0, 0, 16));
    assertEquals("!AQA", getEntry(0, 0, 17));
    assertEquals("!ARA", getEntry(0, 0, 18));
    assertEquals("!A+A", getEntry(0, 0, 63));
    assertEquals("!A/A", getEntry(0, 0, 64));
    assertEquals("!!ABAA", getEntry(0, 0, 65));
    assertEquals("!!A//A", getEntry(0, 0, 4096));
    assertEquals("!!!ABAAA", getEntry(0, 0, 4097));

    assertEquals("Af", getEntry(31, 0, 1));
    assertEquals("BAg", getEntry(32, 0, 1));
    assertEquals("AB", getEntry(32, 31, 1));

    assertEquals("!AQf", getEntry(31, 0, 17));
    assertEquals("!BQAg", getEntry(32, 0, 17));
    assertEquals("!AQB", getEntry(32, 31, 17));

    assertEquals("!A/B", getEntry(32, 31, 64));
    assertEquals("!!ABAB", getEntry(32, 31, 65));
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicMapping1
  public void testBasicMapping1() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicMappingGoldenOutput
  public void testBasicMappingGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__() { }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,UAAS,EAAG;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"__BASIC__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicMapping2
  public void testBasicMapping2() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__) {}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testLiteralMappingsGoldenOutput
  public void testLiteralMappingsGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                   "var __VAR__ = '__STR__'; }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,UAAS,CAACC,UAAD,CAAaC,UAAb," +
                       "CAAyB,CAAE,IAAIC,QAAU,SAAhB;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"__BASIC__\",\"__PARAM1__\",\"__PARAM2__\"," +
                       "\"__VAR__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testMultilineMapping2
  public void testMultilineMapping2() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = 1;\n" +
                    "var __ANO__ = 2;\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\";\",\n" +
                   "\"sources\":[],\n" +
                   "\"names\":[]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput0a
  public void testGoldenOutput0a() throws Exception {
    
    checkSourceMap("a;",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"a\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,EAAC,CAACC,GAAD,CAAMC,GAAN," +
                       "CAAW,CAAED,GAAA,CAAMA,GAAN,CAAYC,GAAZ,CAAkB,CAAG," +
                       "OAAOD,IAA9B;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,EAATA,CAAWC,GAAXD,CAAgBE," +
                       "GAAhBF,EAAuBC,GAAvBD,CAA6BC,GAA7BD,CAAmCE,GAAnCF," +
                       "SAAmDC,IAAnDD;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,EAAC,CAACC,GAAD,CAAMC,GAAN," +
                       "CAAW,CAIrBD,GAAA,CAAMA,GAAN,CAAYC,GAAZ,CAAkBD," +
                       "GAClB,OAAOA,IALc;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA;\",\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,GAAOC,IAAOC;\",\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\",\"boo\",\"goo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap(
        "c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "{\n" +
        "\"version\":3,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":6,\n" +
        "\"mappings\":\"A;;;;AAGA,IAAIA,IAAIC,CAAJD,CAAQ,mxCAARA;AAA8xCE," +
            "CAA9xCF,CAAkyCG,CAAlyCH,CAAsyCI;\",\n" +
        "\"sources\":[\"c:\\\\myfile.js\"],\n" +
        "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
        "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "{\n" +
        "\"version\":3,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":6,\n" +
        "\"mappings\":\"A;;;;IAGIA,IAAIC,CAAJD;AAA8xCE,CAA9xCF,CAAkyCG," +
            "CAAlyCH,CAAsyCI;\",\n" +
        "\"sources\":[\"c:\\\\myfile.js\"],\n" +
        "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
        "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testWriteMetaMap
  public void testWriteMetaMap() throws IOException {
    StringWriter out = new StringWriter();
    String name = "./app.js";
    List<SourceMapSection> appSections = Lists.newArrayList(
        SourceMapSection.forURL("src1", 0, 0),
        SourceMapSection.forURL("src2", 100, 10),
        SourceMapSection.forURL("src3", 150, 5));

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
    generator.appendIndexMapTo(out, name, appSections);

    assertEquals(
            "{\n" +
            "\"version\":3,\n" +
            "\"file\":\"./app.js\",\n" +
            "\"sections\":[\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":0,\n" +
            "\"column\":0\n" +
            "},\n" +
            "\"url\":\"src1\"\n" +
            "},\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":100,\n" +
            "\"column\":10\n" +
            "},\n" +
            "\"url\":\"src2\"\n" +
            "},\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":150,\n" +
            "\"column\":5\n" +
            "},\n" +
            "\"url\":\"src3\"\n" +
            "}\n" +
            "]\n" +
            "}\n",
            out.toString());
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testWriteMetaMap2
  public void testWriteMetaMap2() throws IOException {
    StringWriter out = new StringWriter();
    String name = "./app.js";
    List<SourceMapSection> appSections = Lists.newArrayList(
        
        SourceMapSection.forMap(getEmptyMapFor("./part.js"), 0, 0),
        SourceMapSection.forURL("src2", 100, 10));

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
    generator.appendIndexMapTo(out, name, appSections);

    assertEquals(
            "{\n" +
            "\"version\":3,\n" +
            "\"file\":\"./app.js\",\n" +
            "\"sections\":[\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":0,\n" +
            "\"column\":0\n" +
            "},\n" +
            "\"map\":{\n" +
              "\"version\":3,\n" +
              "\"file\":\"./part.js\",\n" +
              "\"lineCount\":1,\n" +
              "\"mappings\":\";\",\n" +
              "\"sources\":[],\n" +
              "\"names\":[]\n" +
            "}\n" +
            "\n" +
            "},\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":100,\n" +
            "\"column\":10\n" +
            "},\n" +
            "\"url\":\"src2\"\n" +
            "}\n" +
            "]\n" +
            "}\n",
            out.toString());
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testParseSourceMetaMap
  public void testParseSourceMetaMap() throws Exception {
    final String INPUT1 = "file1";
    final String INPUT2 = "file2";
    LinkedHashMap<String, String> inputs = Maps.newLinkedHashMap();
    inputs.put(INPUT1, "var __FOO__ = 1;");
    inputs.put(INPUT2, "var __BAR__ = 2;");
    RunResult result1 = compile(inputs.get(INPUT1), INPUT1);
    RunResult result2 = compile(inputs.get(INPUT2), INPUT2);

    final String MAP1 = "map1";
    final String MAP2 = "map2";
    final LinkedHashMap<String, String> maps = Maps.newLinkedHashMap();
    maps.put(MAP1, result1.sourceMapFileContent);
    maps.put(MAP2, result2.sourceMapFileContent);

    List<SourceMapSection> sections = Lists.newArrayList();

    StringBuilder output = new StringBuilder();
    FilePosition offset = appendAndCount(output, result1.generatedSource);
    sections.add(SourceMapSection.forURL(MAP1, 0, 0));
    output.append(result2.generatedSource);
    sections.add(
        SourceMapSection.forURL(MAP2, offset.getLine(), offset.getColumn()));

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
    StringBuilder mapContents = new StringBuilder();
    generator.appendIndexMapTo(mapContents, "out.js", sections);

    check(inputs, output.toString(), mapContents.toString(),
      new SourceMapSupplier() {
        @Override
        public String getSourceMap(String url){
          return maps.get(url);
      }});
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testSourceMapMerging
  public void testSourceMapMerging() throws Exception {
    final String INPUT1 = "file1";
    final String INPUT2 = "file2";
    LinkedHashMap<String, String> inputs = Maps.newLinkedHashMap();
    inputs.put(INPUT1, "var __FOO__ = 1;");
    inputs.put(INPUT2, "var __BAR__ = 2;");
    RunResult result1 = compile(inputs.get(INPUT1), INPUT1);
    RunResult result2 = compile(inputs.get(INPUT2), INPUT2);

    StringBuilder output = new StringBuilder();
    FilePosition offset = appendAndCount(output, result1.generatedSource);
    output.append(result2.generatedSource);

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();

    generator.mergeMapSection(0, 0, result1.sourceMapFileContent);
    generator.mergeMapSection(offset.getLine(), offset.getColumn(),
        result2.sourceMapFileContent);

    StringBuilder mapContents = new StringBuilder();
    generator.appendTo(mapContents, "out.js");

    check(inputs, output.toString(), mapContents.toString());
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithNoNewNodes
  public void testPassWithNoNewNodes() {
    String js = "var str = 'g4'; ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNew
  public void testPassWithOneNew() {
    String js =
        "var goog = {};" +
        "goog.require('foo.bar.goo'); var bar = new foo.bar.goo();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNewOuterClass
  public void testPassWithOneNewOuterClass() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.Bar'); var bar = new goog.foo.Bar.Baz();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNewOuterClassWithUpperPrefix
  public void testPassWithOneNewOuterClassWithUpperPrefix() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.IDBar'); var bar = new goog.foo.IDBar.Baz();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithOneNew
  public void testFailWithOneNew() {
    String[] js = new String[] {"var foo = {}; var bar = new foo.bar();"};
    String warning = "'foo.bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithTwoNewNodes
  public void testPassWithTwoNewNodes() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.Bar');goog.require('goog.foo.Baz');" +
        "var str = new goog.foo.Bar('g4'), num = new goog.foo.Baz(5); ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithNestedNewNodes
  public void testPassWithNestedNewNodes() {
    String js =
        "var goog = {}; goog.require('goog.foo.Bar'); " +
        "var str = new goog.foo.Bar(new goog.foo.Bar('5')); ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithNestedNewNodes
  public void testFailWithNestedNewNodes() {
    String[] js =
        new String[] {"var goog = {}; goog.require('goog.foo.Bar'); "
            + "var str = new goog.foo.Bar(new goog.foo.Baz('5')); "};
    String warning = "'goog.foo.Baz' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithLocalFunctions
  public void testPassWithLocalFunctions() {
    String js =
        " function tempCtor() {}; var foo = new tempCtor();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithLocalVariables
  public void testPassWithLocalVariables() {
    String js =
        " var nodeCreator = function() {};"
            + "var newNode = new nodeCreator();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithLocalVariableInMoreThanOneFile
  public void testFailWithLocalVariableInMoreThanOneFile() {
    
    
    String localVar =
        " function tempCtor() {}" +
        "function baz(){" + "  function tempCtor() {}; "
            + "var foo = new tempCtor();}";
    String[] js = new String[] {localVar, " var foo = new tempCtor();"};
    String warning = "'tempCtor' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesMetaTraditionalFunctionForm
  public void testNewNodesMetaTraditionalFunctionForm() {
    
    
    
    String js =
        " function Bar(){}; "
            + "Bar.prototype.bar = function(){ return new Bar();};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesMeta
  public void testNewNodesMeta() {
    String js =
        "var goog = {};" +
        "goog.ui.Option = function(){};"
            + "goog.ui.Option.optionDecorator = function(){"
            + "  return new goog.ui.Option(); };";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testShouldWarnWhenInstantiatingObjectsDefinedInGlobalScope
  public void testShouldWarnWhenInstantiatingObjectsDefinedInGlobalScope() {
    
    
    String good =
        " function Bar(){}; "
            + "Bar.prototype.bar = function(){return new Bar();};";
    String bad = " function Foo(){ var bar = new Bar();}";
    String[] js = new String[] {good, bad};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testShouldWarnWhenInstantiatingGlobalClassesFromGlobalScope
  public void testShouldWarnWhenInstantiatingGlobalClassesFromGlobalScope() {
    
    
    String good =
      " function Baz(){}; "
          + "Baz.prototype.bar = function(){return new Baz();};";
    String bad = "var baz = new Baz()";
    String[] js = new String[] {good, bad};
    String warning = "'Baz' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testIgnoresNativeObject
  public void testIgnoresNativeObject() {
    String externs = " function String(val) {}";
    String js = "var str = new String('4');";
    test(externs, js, js, null, null);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesWithMoreThanOneFile
  public void testNewNodesWithMoreThanOneFile() {
    
    String[] js = new String[] {
        "var goog = {};" +
        " function Bar() {}" +
        "goog.require('Bar');",
        "var bar = new Bar();"};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithoutWarningsAndMultipleFiles
  public void testPassWithoutWarningsAndMultipleFiles() {
    String[] js = new String[] {
        "var goog = {};" +
        "goog.require('Foo'); var foo = new Foo();",
        "goog.require('Bar'); var bar = new Bar();"};
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithWarningsAndMultipleFiles
  public void testFailWithWarningsAndMultipleFiles() {
    
    String[] js = new String[] {
        "var goog = {};" +
        " function Bar() {}" +
        "goog.require('Bar');",
        "var bar = new Bar();"};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testCanStillCallNumberWithoutNewOperator
  public void testCanStillCallNumberWithoutNewOperator() {
    String externs = " function Number(opt_value) {}";
    String js = "var n = Number('42');";
    test(externs, js, js, null, null);
    js = "var n = Number();";
    test(externs, js, js, null, null);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testRequiresAreCaughtBeforeProcessed
  public void testRequiresAreCaughtBeforeProcessed() {
    String js = "var foo = {}; var bar = new foo.bar.goo();";
    JSSourceFile input = JSSourceFile.fromCode("foo.js", js);
    Compiler compiler = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkRequires = CheckLevel.WARNING;
    opts.closurePass = true;

    Result result = compiler.compile(new JSSourceFile[] {},
        new JSSourceFile[] {input}, opts);
    JSError[] warnings = result.warnings;
    assertNotNull(warnings);
    assertTrue(warnings.length > 0);

    String expectation = "'foo.bar.goo' used but not goog.require'd";

    for (JSError warning : warnings) {
      if (expectation.equals(warning.description)) {
        return;
      }
    }

    fail("Could not find the following warning:" + expectation);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNoWarningsForThisConstructor
  public void testNoWarningsForThisConstructor() {
    String js =
      "var goog = {};" +
      "goog.Foo = function() {};" +
      "goog.Foo.bar = function() {" +
      "  return new this.constructor; " +
      "};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testBug2062487
  public void testBug2062487() {
    testSame(
      "var goog = {};" +
      "goog.Foo = function() {" +
      "   this.x_ = function() {};" +
      "  this.y_ = new this.x_();" +
      "};");
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testIgnoreDuplicateWarningsForSingleClasses
  public void testIgnoreDuplicateWarningsForSingleClasses(){
    
    String[] js = new String[]{
      "var goog = {};" +
      "goog.Foo = function() {};" +
      "goog.Foo.bar = function(){" +
      "  var first = new goog.Forgot();" +
      "  var second = new goog.Forgot();" +
      "};"};
    String warning = "'goog.Forgot' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrint
  public void testPrint() {
    assertPrint("10 + a + b", "10+a+b");
    assertPrint("10 + (30*50)", "10+30*50");
    assertPrint("with(x) { x + 3; }", "with(x)x+3");
    assertPrint("\"aa'a\"", "\"aa'a\"");
    assertPrint("\"aa\\\"a\"", "'aa\"a'");
    assertPrint("function foo()\n{return 10;}", "function foo(){return 10}");
    assertPrint("a instanceof b", "a instanceof b");
    assertPrint("typeof(a)", "typeof a");
    assertPrint(
        "var foo = x ? { a : 1 } : {a: 3, b:4, \"default\": 5, \"foo-bar\": 6}",
        "var foo=x?{a:1}:{a:3,b:4,\"default\":5,\"foo-bar\":6}");

    
    assertPrint("function foo(){throw 'error';}",
        "function foo(){throw\"error\";}");
    
    assertPrint("if (true) function foo(){return}",
        "if(true){function foo(){return}}");

    assertPrint("var x = 10; { var y = 20; }", "var x=10;var y=20");

    assertPrint("while (x-- > 0);", "while(x-- >0);");
    assertPrint("x-- >> 1", "x-- >>1");

    assertPrint("(function () {})(); ",
        "(function(){})()");

    
    assertPrint("var a,b,c,d;a || (b&& c) && (a || d)",
        "var a,b,c,d;a||b&&c&&(a||d)");
    assertPrint("var a,b,c; a || (b || c); a * (b * c); a | (b | c)",
        "var a,b,c;a||b||c;a*b*c;a|b|c");
    assertPrint("var a,b,c; a / b / c;a / (b / c); a - (b - c);",
        "var a,b,c;a/b/c;a/(b/c);a-(b-c)");
    assertPrint("var a,b; a = b = 3;",
        "var a,b;a=b=3");
    assertPrint("var a,b,c,d; a = (b = c = (d = 3));",
        "var a,b,c,d;a=b=c=d=3");
    assertPrint("var a,b,c; a += (b = c += 3);",
        "var a,b,c;a+=b=c+=3");
    assertPrint("var a,b,c; a *= (b -= c);",
        "var a,b,c;a*=b-=c");

    
    assertPrint("'<script>'", "\"<script>\"");
    assertPrint("'</script>'", "\"<\\/script>\"");
    assertPrint("\"</script> </SCRIPT>\"", "\"<\\/script> <\\/SCRIPT>\"");

    assertPrint("'-->'", "\"--\\>\"");
    assertPrint("']]>'", "\"]]\\>\"");
    assertPrint("' --></script>'", "\" --\\><\\/script>\"");

    assertPrint("/--> <\\/script>/g", "/--\\> <\\/script>/g");

    
    
    assertPrint("'<!-- I am a string -->'", "\"<\\!-- I am a string --\\>\"");

    
    assertPrint("a ? delete b[0] : 3", "a?delete b[0]:3");
    assertPrint("(delete a[0])/10", "delete a[0]/10");

    

    
    assertPrint("new A", "new A");
    assertPrint("new A()", "new A");
    assertPrint("new A('x')", "new A(\"x\")");

    
    assertPrint("new A().a()", "(new A).a()");
    assertPrint("(new A).a()", "(new A).a()");

    
    assertPrint("new A('y').a()", "(new A(\"y\")).a()");

    
    assertPrint("new A.B", "new A.B");
    assertPrint("new A.B()", "new A.B");
    assertPrint("new A.B('z')", "new A.B(\"z\")");

    
    assertPrint("(new A.B).a()", "(new A.B).a()");
    assertPrint("new A.B().a()", "(new A.B).a()");
    
    assertPrint("new A.B('w').a()", "(new A.B(\"w\")).a()");

    
    assertPrint("x + +y", "x+ +y");
    assertPrint("x - (-y)", "x- -y");
    assertPrint("x++ +y", "x++ +y");
    assertPrint("x-- -y", "x-- -y");
    assertPrint("x++ -y", "x++-y");

    
    assertPrint("foo:for(;;){break foo;}", "foo:for(;;)break foo");
    assertPrint("foo:while(1){continue foo;}", "foo:while(1)continue foo");

    
    assertPrint("({})", "({})");
    assertPrint("var x = {};", "var x={}");
    assertPrint("({}).x", "({}).x");
    assertPrint("({})['x']", "({})[\"x\"]");
    assertPrint("({}) instanceof Object", "({})instanceof Object");
    assertPrint("({}) || 1", "({})||1");
    assertPrint("1 || ({})", "1||{}");
    assertPrint("({}) ? 1 : 2", "({})?1:2");
    assertPrint("0 ? ({}) : 2", "0?{}:2");
    assertPrint("0 ? 1 : ({})", "0?1:{}");
    assertPrint("typeof ({})", "typeof{}");
    assertPrint("f({})", "f({})");

    
    assertPrint("(function(){})", "(function(){})");
    assertPrint("(function(){})()", "(function(){})()");
    assertPrint("(function(){})instanceof Object",
        "(function(){})instanceof Object");
    assertPrint("(function(){}).bind().call()",
        "(function(){}).bind().call()");
    assertPrint("var x = function() { };", "var x=function(){}");
    assertPrint("var x = function() { }();", "var x=function(){}()");
    assertPrint("(function() {}), 2", "(function(){}),2");

    
    assertPrint("(function f(){})", "(function f(){})");

    
    assertPrint("function f(){}", "function f(){}");

    
    assertPrint("({ 'a': 4, '\\u0100': 4 })", "({\"a\":4,\"\\u0100\":4})");
    assertPrint("({ a: 4, '\\u0100': 4 })", "({a:4,\"\\u0100\":4})");

    
    assertPrint("if (true) { alert();}", "if(true)alert()");
    assertPrint("if (false) {} else {alert(\"a\");}",
        "if(false);else alert(\"a\")");
    assertPrint("for(;;) { alert();};", "for(;;)alert()");

    assertPrint("do { alert(); } while(true);",
        "do alert();while(true)");
    assertPrint("myLabel: { alert();}",
        "myLabel:alert()");
    assertPrint("myLabel: for(;;) continue myLabel;",
        "myLabel:for(;;)continue myLabel");

    
    assertPrint("if (true) var x; x = 4;", "if(true)var x;x=4");

    
    assertPrint("\\u00fb", "\\u00fb");
    assertPrint("\\u00fa=1", "\\u00fa=1");
    assertPrint("function \\u00f9(){}", "function \\u00f9(){}");
    assertPrint("x.\\u00f8", "x.\\u00f8");
    assertPrint("x.\\u00f8", "x.\\u00f8");
    assertPrint("abc\\u4e00\\u4e01jkl", "abc\\u4e00\\u4e01jkl");

    
    assertPrint("! ! true", "!!true");
    assertPrint("!(!(true))", "!!true");
    assertPrint("typeof(void(0))", "typeof void 0");
    assertPrint("typeof(void(!0))", "typeof void!0");
    assertPrint("+ - + + - + 3", "+-+ +-+3"); 
    assertPrint("+(--x)", "+--x");
    assertPrint("-(++x)", "-++x");

    
    assertPrint("-(--x)", "- --x");
    assertPrint("!(~~5)", "!~~5");
    assertPrint("~(a/b)", "~(a/b)");

    
    assertPrint("new (foo.bar()).factory(baz)", "new (foo.bar().factory)(baz)");
    assertPrint("new (bar()).factory(baz)", "new (bar().factory)(baz)");
    assertPrint("new (new foobar(x)).factory(baz)",
        "new (new foobar(x)).factory(baz)");

    
    assertPrint("a ? b : (c ? d : e)", "a?b:c?d:e");
    assertPrint("a ? (b ? c : d) : e", "a?b?c:d:e");
    assertPrint("(a ? b : c) ? d : e", "(a?b:c)?d:e");

    
    assertPrint("if (x) if (y); else;", "if(x)if(y);else;");

    
    assertPrint("a,b,c", "a,b,c");
    assertPrint("(a,b),c", "a,b,c");
    assertPrint("a,(b,c)", "a,b,c");
    assertPrint("x=a,b,c", "x=a,b,c");
    assertPrint("x=(a,b),c", "x=(a,b),c");
    assertPrint("x=a,(b,c)", "x=a,b,c");
    assertPrint("x=a,y=b,z=c", "x=a,y=b,z=c");
    assertPrint("x=(a,y=b,z=c)", "x=(a,y=b,z=c)");
    assertPrint("x=[a,b,c,d]", "x=[a,b,c,d]");
    assertPrint("x=[(a,b,c),d]", "x=[(a,b,c),d]");
    assertPrint("x=[(a,(b,c)),d]", "x=[(a,b,c),d]");
    assertPrint("x=[a,(b,c,d)]", "x=[a,(b,c,d)]");
    assertPrint("var x=(a,b)", "var x=(a,b)");
    assertPrint("var x=a,b,c", "var x=a,b,c");
    assertPrint("var x=(a,b),c", "var x=(a,b),c");
    assertPrint("var x=a,b=(c,d)", "var x=a,b=(c,d)");
    assertPrint("foo(a,b,c,d)", "foo(a,b,c,d)");
    assertPrint("foo((a,b,c),d)", "foo((a,b,c),d)");
    assertPrint("foo((a,(b,c)),d)", "foo((a,b,c),d)");
    assertPrint("f(a+b,(c,d,(e,f,g)))", "f(a+b,(c,d,e,f,g))");
    assertPrint("({}) , 1 , 2", "({}),1,2");
    assertPrint("({}) , {} , {}", "({}),{},{}");

    
    assertPrint("if (x){}", "if(x);");
    assertPrint("if(x);", "if(x);");
    assertPrint("if(x)if(y);", "if(x)if(y);");
    assertPrint("if(x){if(y);}", "if(x)if(y);");
    assertPrint("if(x){if(y){};;;}", "if(x)if(y);");
    assertPrint("if(x){;;function y(){};;}", "if(x){function y(){}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintArray
  public void testPrintArray() {
    assertPrint("[void 0, void 0]", "[void 0,void 0]");
    assertPrint("[undefined, undefined]", "[undefined,undefined]");
    assertPrint("[ , , , undefined]", "[,,,undefined]");
    assertPrint("[ , , , 0]", "[,,,0]");
  }

// com.google.javascript.jscomp.CodePrinterTest::testHook
  public void testHook() {
    assertPrint("a ? b = 1 : c = 2", "a?b=1:c=2");
    assertPrint("x = a ? b = 1 : c = 2", "x=a?b=1:c=2");
    assertPrint("(x = a) ? b = 1 : c = 2", "(x=a)?b=1:c=2");

    assertPrint("x, a ? b = 1 : c = 2", "x,a?b=1:c=2");
    assertPrint("x, (a ? b = 1 : c = 2)", "x,a?b=1:c=2");
    assertPrint("(x, a) ? b = 1 : c = 2", "(x,a)?b=1:c=2");

    assertPrint("a ? (x, b) : c = 2", "a?(x,b):c=2");
    assertPrint("a ? b = 1 : (x,c)", "a?b=1:(x,c)");

    assertPrint("a ? b = 1 : c = 2 + x", "a?b=1:c=2+x");
    assertPrint("(a ? b = 1 : c = 2) + x", "(a?b=1:c=2)+x");
    assertPrint("a ? b = 1 : (c = 2) + x", "a?b=1:(c=2)+x");

    assertPrint("a ? (b?1:2) : 3", "a?b?1:2:3");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintInOperatorInForLoop
  public void testPrintInOperatorInForLoop() {
    
    
    
    assertPrint("var a={}; for (var i = (\"length\" in a); i;) {}",
        "var a={};for(var i=(\"length\"in a);i;);");
    assertPrint("var a={}; for (var i = (\"length\" in a) ? 0 : 1; i;) {}",
        "var a={};for(var i=(\"length\"in a)?0:1;i;);");
    assertPrint("var a={}; for (var i = (\"length\" in a) + 1; i;) {}",
        "var a={};for(var i=(\"length\"in a)+1;i;);");
    assertPrint("var a={};for (var i = (\"length\" in a|| \"size\" in a);;);",
        "var a={};for(var i=(\"length\"in a)||(\"size\"in a);;);");
    assertPrint("var a={};for (var i = a || a || (\"size\" in a);;);",
        "var a={};for(var i=a||a||(\"size\"in a);;);");

    
    assertPrint("var a={}; for (var i = -(\"length\" in a); i;) {}",
        "var a={};for(var i=-(\"length\"in a);i;);");
    assertPrint("var a={};function b_(p){ return p;};" +
        "for(var i=1,j=b_(\"length\" in a);;) {}",
        "var a={};function b_(p){return p}" +
            "for(var i=1,j=b_(\"length\"in a);;);");

    
    assertPrint("var a={}; for (;(\"length\" in a);) {}",
        "var a={};for(;\"length\"in a;);");
  }

// com.google.javascript.jscomp.CodePrinterTest::testLiteralProperty
  public void testLiteralProperty() {
    assertPrint("(64).toString()", "(64).toString()");
  }

// com.google.javascript.jscomp.CodePrinterTest::testAmbiguousElseClauses
  public void testAmbiguousElseClauses() {
    assertPrintNode("if(x)if(y);else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK),

                    
                    new Node(Token.BLOCK)))));

    assertPrintNode("if(x){if(y);}else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK))),

            
            new Node(Token.BLOCK)));

    assertPrintNode("if(x)if(y);else{if(z);}else;",
        new Node(Token.IF,
            Node.newString(Token.NAME, "x"),
            new Node(Token.BLOCK,
                new Node(Token.IF,
                    Node.newString(Token.NAME, "y"),
                    new Node(Token.BLOCK),
                    new Node(Token.BLOCK,
                        new Node(Token.IF,
                            Node.newString(Token.NAME, "z"),
                            new Node(Token.BLOCK))))),

            
            new Node(Token.BLOCK)));
  }

// com.google.javascript.jscomp.CodePrinterTest::testLineBreak
  public void testLineBreak() {
    
    assertLineBreak("function a() {}\n" +
        "function b() {}",
        "function a(){}\n" +
        "function b(){}\n");

    
    assertLineBreak("var a = {};\n" +
        "a.foo = function () {}\n" +
        "function b() {}",
        "var a={};a.foo=function(){};\n" +
        "function b(){}\n");

    
    assertLineBreak("var a = {\n" +
        "  b: function() {},\n" +
        "  c: function() {}\n" +
        "};\n" +
        "alert(a);",

        "var a={b:function(){},\n" +
        "c:function(){}};\n" +
        "alert(a)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter
  public void testPrettyPrinter() {
    
    
    assertPrettyPrint("(function(){})();","(function() {\n})();\n");
    assertPrettyPrint("var a = (function() {});alert(a);",
        "var a = function() {\n};\nalert(a);\n");

    
    
    assertPrettyPrint("if (1) {}",
        "if(1) {\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert(\"\");}",
        "if(1) {\n" +
        "  alert(\"\")\n" +
        "}\n");
    assertPrettyPrint("if (1)alert(\"\");",
        "if(1) {\n" +
        "  alert(\"\")\n" +
        "}\n");
    assertPrettyPrint("if (1) {alert();alert();}",
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    
    assertPrettyPrint("label: alert();",
        "label:alert();\n");

    
    assertPrettyPrint("if (1) alert();",
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");
    assertPrettyPrint("for (;;) alert();",
        "for(;;) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint("while (1) alert();",
        "while(1) {\n" +
        "  alert()\n" +
        "}\n");

    
    assertPrettyPrint("if (1) {} else {alert(a);}",
        "if(1) {\n" +
        "}else {\n  alert(a)\n}\n");

    
    assertPrettyPrint("if (1) alert(a); else alert(b);",
        "if(1) {\n" +
        "  alert(a)\n" +
        "}else {\n" +
        "  alert(b)\n" +
        "}\n");

    
    assertPrettyPrint("for(;;) { alert();}",
        "for(;;) {\n" +
         "  alert()\n" +
         "}\n");
    assertPrettyPrint("for(;;) {}",
        "for(;;) {\n" +
        "}\n");
    assertPrettyPrint("for(;;) { alert(); alert(); }",
        "for(;;) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    
    assertPrettyPrint("do { alert(); } while(true);",
        "do {\n" +
        "  alert()\n" +
        "}while(true);\n");

    
    assertPrettyPrint("myLabel: { alert();}",
        "myLabel: {\n" +
        "  alert()\n" +
        "}\n");

    
    
    assertPrettyPrint("myLabel: for(;;) continue myLabel;",
        "myLabel:for(;;) {\n" +
        "  continue myLabel\n" +
        "}\n");

    assertPrettyPrint("var a;", "var a;\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter2
  public void testPrettyPrinter2() {
    assertPrettyPrint(
        "if(true) f();",
        "if(true) {\n" +
        "  f()\n" +
        "}\n");

    assertPrettyPrint(
        "if (true) { f() } else { g() }",
        "if(true) {\n" +
        "  f()\n" +
        "}else {\n" +
        "  g()\n" +
        "}\n");

    assertPrettyPrint(
        "if(true) f(); for(;;) g();",
        "if(true) {\n" +
        "  f()\n" +
        "}\n" +
        "for(;;) {\n" +
        "  g()\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter3
  public void testPrettyPrinter3() {
    assertPrettyPrint(
        "try {} catch(e) {}if (1) {alert();alert();}",
        "try {\n" +
        "}catch(e) {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "try {} finally {}if (1) {alert();alert();}",
        "try {\n" +
        "}finally {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "try {} catch(e) {} finally {} if (1) {alert();alert();}",
        "try {\n" +
        "}catch(e) {\n" +
        "}finally {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrettyPrinter4
  public void testPrettyPrinter4() {
    assertPrettyPrint(
        "function f() {}if (1) {alert();}",
        "function f() {\n" +
        "}\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "var f = function() {};if (1) {alert();}",
        "var f = function() {\n" +
        "};\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {})();if (1) {alert();}",
        "(function() {\n" +
        "})();\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");

    assertPrettyPrint(
        "(function() {alert();alert();})();if (1) {alert();}",
        "(function() {\n" +
        "  alert();\n" +
        "  alert()\n" +
        "})();\n" +
        "if(1) {\n" +
        "  alert()\n" +
        "}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotations
  public void testTypeAnnotations() {
    assertTypeAnnotations(
        " function Foo(){}",
        "\n"
        + "function Foo() {\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsTypeDef
  public void testTypeAnnotationsTypeDef() {
    
    
    
    assertTypeAnnotations(
        " goog.java.Long;\n"
        + "\n"
        + "function f(a){};\n",
        "goog.java.Long;\n"
        + "\n"
        + "function f(a) {\n}\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsAssign
  public void testTypeAnnotationsAssign() {
    assertTypeAnnotations(" var Foo = function(){}",
        "\n"
        + "var Foo = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsNamespace
  public void testTypeAnnotationsNamespace() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMemberSubclass
  public void testTypeAnnotationsMemberSubclass() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsInterface
  public void testTypeAnnotationsInterface() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMultipleInterface
  public void testTypeAnnotationsMultipleInterface() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo1 = function(){};"
        + " a.Foo2 = function(){};"
        + ""
        + "a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo1 = function() {\n};\n"
        + "\n"
        + "a.Foo2 = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsMember
  public void testTypeAnnotationsMember() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){}"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) { return 3; };"
        + ""
        + "a.Foo.prototype.bar = '';",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.Foo.prototype.foo = function(foo) {\n  return 3\n};\n"
        + "\n"
        + "a.Foo.prototype.bar = \"\";\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsImplements
  public void testTypeAnnotationsImplements() {
    assertTypeAnnotations("var a = {};"
        + " a.Foo = function(){};\n"
        + " a.I = function(){};\n"
        + " a.I2 = function(){};\n"
        + " a.Bar = function(){}",
        "var a = {};\n"
        + "\n"
        + "a.Foo = function() {\n};\n"
        + "\n"
        + "a.I = function() {\n};\n"
        + "\n"
        + "a.I2 = function() {\n};\n"
        + "\n"
        + "a.Bar = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsDispatcher1
  public void testTypeAnnotationsDispatcher1() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "\n" +
        "a.Foo = function(){}",
        "var a = {};\n" +
        "\n" +
        "a.Foo = function() {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTypeAnnotationsDispatcher2
  public void testTypeAnnotationsDispatcher2() {
    assertTypeAnnotations(
        "var a = {};\n" +
        "\n" +
        "a.Foo = function(){}\n" +
        "\n" +
        "a.Foo.prototype.foo = function() {};",

        "var a = {};\n" +
        "\n" +
        "a.Foo = function() {\n" +
        "};\n" +
        "\n" +
        "a.Foo.prototype.foo = function() {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testU2UFunctionTypeAnnotation
  public void testU2UFunctionTypeAnnotation() {
    assertTypeAnnotations(
        " var x = function() {}",
        "\nvar x = function() {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testEmitUnknownParamTypesAsAllType
  public void testEmitUnknownParamTypesAsAllType() {
    assertTypeAnnotations(
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testOptionalTypesAnnotation
  public void testOptionalTypesAnnotation() {
    assertTypeAnnotations(
        "\n" +
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testVariableArgumentsTypesAnnotation
  public void testVariableArgumentsTypesAnnotation() {
    assertTypeAnnotations(
        "\n" +
        "var a = function(x) {}",
        "\n" +
        "var a = function(x) {\n};\n");
  }

// com.google.javascript.jscomp.CodePrinterTest::testTempConstructor
  public void testTempConstructor() {
    assertTypeAnnotations(
        "var x = function() {\n\nfunction t1() {}\n" +
        " \nfunction t2() {}\n" +
        " t1.prototype = t2.prototype}",
        "\nvar x = function() {\n" +
        "  \n" +
        "function t1() {\n  }\n" +
        "  \n" +
        "function t2() {\n  }\n" +
        "  t1.prototype = t2.prototype\n};\n"
    );
  }

// com.google.javascript.jscomp.CodePrinterTest::testSubtraction
  public void testSubtraction() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("x - -4");
    assertEquals(0, compiler.getErrorCount());

    assertEquals(
        "x- -4",
        new CodePrinter.Builder(n).setLineLengthThreshold(
            CodePrinter.DEFAULT_LINE_LENGTH_THRESHOLD).build());
  }

// com.google.javascript.jscomp.CodePrinterTest::testFunctionWithCall
  public void testFunctionWithCall() {
    assertPrint(
        "var user = new function() {"
        + "alert(\"foo\")}",
        "var user=new function(){"
        + "alert(\"foo\")}");
    assertPrint(
        "var user = new function() {"
        + "this.name = \"foo\";"
        + "this.local = function(){alert(this.name)};}",
        "var user=new function(){"
        + "this.name=\"foo\";"
        + "this.local=function(){alert(this.name)}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testLineLength
  public void testLineLength() {
    
    assertLineLength("var aba,bcb,cdc",
        "var aba,bcb," +
        "\ncdc");

    
    assertLineLength(
        "\"foo\"+\"bar,baz,bomb\"+\"whee\"+\";long-string\"\n+\"aaa\"",
        "\"foo\"+\"bar,baz,bomb\"+" +
        "\n\"whee\"+\";long-string\"+" +
        "\n\"aaa\"");

    
    assertLineLength("var abazaba=1234",
        "var abazaba=" +
        "\n1234");

    
    assertLineLength("var abab=1;var bab=2",
        "var abab=1;" +
        "\nvar bab=2");

    
    assertLineLength("var a=/some[reg](ex),with.*we?rd|chars/i;var b=a",
        "var a=/some[reg](ex),with.*we?rd|chars/i;" +
        "\nvar b=a");

    
    assertLineLength("var a=\"foo,{bar};baz\";var b=a",
        "var a=\"foo,{bar};baz\";" +
        "\nvar b=a");

    
    assertLineLength("var a=\"a\";a++;var b=\"bbb\";",
        "var a=\"a\";a++;\n" +
        "var b=\"bbb\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testParsePrintParse
  public void testParsePrintParse() {
    testReparse("3;");
    testReparse("var a = b;");
    testReparse("var x, y, z;");
    testReparse("try { foo() } catch(e) { bar() }");
    testReparse("try { foo() } catch(e) { bar() } finally { stuff() }");
    testReparse("try { foo() } finally { stuff() }");
    testReparse("throw 'me'");
    testReparse("function foo(a) { return a + 4; }");
    testReparse("function foo() { return; }");
    testReparse("var a = function(a, b) { foo(); return a + b; }");
    testReparse("b = [3, 4, 'paul', \"Buchhe it\",,5];");
    testReparse("v = (5, 6, 7, 8)");
    testReparse("d = 34.0; x = 0; y = .3; z = -22");
    testReparse("d = -x; t = !x + ~y;");
    testReparse("'hi';  stuff(a,b) \n" +
            " foo(); 
            " bar();");
    testReparse("a = b++ + ++c; a = b++-++c; a = - --b; a = - ++b;");
    testReparse("a++; b= a++; b = ++a; b = a--; b = --a; a+=2; b-=5");
    testReparse("a = (2 + 3) * 4;");
    testReparse("a = 1 + (2 + 3) + 4;");
    testReparse("x = a ? b : c; x = a ? (b,3,5) : (foo(),bar());");
    testReparse("a = b | c || d ^ e " +
            "&& f & !g != h << i <= j < k >>> l > m * n % !o");
    testReparse("a == b; a != b; a === b; a == b == a;" +
            " (a == b) == a; a == (b == a);");
    testReparse("if (a > b) a = b; if (b < 3) a = 3; else c = 4;");
    testReparse("if (a == b) { a++; } if (a == 0) { a++; } else { a --; }");
    testReparse("for (var i in a) b += i;");
    testReparse("for (var i = 0; i < 10; i++){ b /= 2;" +
            " if (b == 2)break;else continue;}");
    testReparse("for (x = 0; x < 10; x++) a /= 2;");
    testReparse("for (;;) a++;");
    testReparse("while(true) { blah(); }while(true) blah();");
    testReparse("do stuff(); while(a>b);");
    testReparse("[0, null, , true, false, this];");
    testReparse("s.replace(/absc/, 'X').replace(/ab/gi, 'Y');");
    testReparse("new Foo; new Bar(a, b,c);");
    testReparse("with(foo()) { x = z; y = t; } with(bar()) a = z;");
    testReparse("delete foo['bar']; delete foo;");
    testReparse("var x = { 'a':'paul', 1:'3', 2:(3,4) };");
    testReparse("switch(a) { case 2: case 3: stuff(); break;" +
        "case 4: morestuff(); break; default: done();}");
    testReparse("x = foo['bar'] + foo['my stuff'] + foo[bar] + f.stuff;");
    testReparse("a.v = b.v; x['foo'] = y['zoo'];");
    testReparse("'test' in x; 3 in x; a in x;");
    testReparse("'foo\"bar' + \"foo'c\" + 'stuff\\n and \\\\more'");
    testReparse("x.__proto__;");
  }

// com.google.javascript.jscomp.CodePrinterTest::testDoLoopIECompatiblity
  public void testDoLoopIECompatiblity() {
    
    assertPrint("function f(){if(e1){do foo();while(e2)}else foo()}",
        "function f(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("function f(){if(e1)do foo();while(e2)else foo()}",
        "function f(){if(e1){do foo();while(e2)}else foo()}");

    assertPrint("if(x){do{foo()}while(y)}else bar()",
        "if(x){do foo();while(y)}else bar()");

    assertPrint("if(x)do{foo()}while(y);else bar()",
        "if(x){do foo();while(y)}else bar()");

    assertPrint("if(x){do{foo()}while(y)}",
        "if(x){do foo();while(y)}");

    assertPrint("if(x)do{foo()}while(y);",
        "if(x){do foo();while(y)}");

    assertPrint("if(x)A:do{foo()}while(y);",
        "if(x){A:do foo();while(y)}");

    assertPrint("var i = 0;a: do{b: do{i++;break b;} while(0);} while(0);",
        "var i=0;a:do{b:do{i++;break b}while(0)}while(0)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testFunctionSafariCompatiblity
  public void testFunctionSafariCompatiblity() {
    
    assertPrint("function f(){if(e1){function goo(){return true}}else foo()}",
        "function f(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("function f(){if(e1)function goo(){return true}else foo()}",
        "function f(){if(e1){function goo(){return true}}else foo()}");

    assertPrint("if(e1){function goo(){return true}}",
        "if(e1){function goo(){return true}}");

    assertPrint("if(e1)function goo(){return true}",
        "if(e1){function goo(){return true}}");

    assertPrint("if(e1)A:function goo(){return true}",
        "if(e1){A:function goo(){return true}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testExponents
  public void testExponents() {
    assertPrintNumber("1", 1);
    assertPrintNumber("10", 10);
    assertPrintNumber("100", 100);
    assertPrintNumber("1E3", 1000);
    assertPrintNumber("1E4", 10000);
    assertPrintNumber("1E5", 100000);
    assertPrintNumber("-1", -1);
    assertPrintNumber("-10", -10);
    assertPrintNumber("-100", -100);
    assertPrintNumber("-1E3", -1000);
    assertPrintNumber("-12341234E4", -123412340000L);
    assertPrintNumber("1E18", 1000000000000000000L);
    assertPrintNumber("1E5", 100000.0);
    assertPrintNumber("100000.1", 100000.1);

    assertPrintNumber("1.0E-6", 0.000001);
  }

// com.google.javascript.jscomp.CodePrinterTest::testDirectEval
  public void testDirectEval() {
    assertPrint("eval('1');", "eval(\"1\")");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIndirectEval
  public void testIndirectEval() {
    Node n = parse("eval('1');");
    assertPrintNode("eval(\"1\")", n);
    n.getFirstChild().getFirstChild().getFirstChild().putBooleanProp(
        Node.DIRECT_EVAL, false);
    assertPrintNode("(0,eval)(\"1\")", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall1
  public void testFreeCall1() {
    assertPrint("foo(a);", "foo(a)");
    assertPrint("x.foo(a);", "x.foo(a)");
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall2
  public void testFreeCall2() {
    Node n = parse("foo(a);");
    assertPrintNode("foo(a)", n);
    Node call =  n.getFirstChild().getFirstChild();
    assertTrue(call.isCall());
    call.putBooleanProp(Node.FREE_CALL, true);
    assertPrintNode("foo(a)", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testFreeCall3
  public void testFreeCall3() {
    Node n = parse("x.foo(a);");
    assertPrintNode("x.foo(a)", n);
    Node call =  n.getFirstChild().getFirstChild();
    assertTrue(call.isCall());
    call.putBooleanProp(Node.FREE_CALL, true);
    assertPrintNode("(0,x.foo)(a)", n);
  }

// com.google.javascript.jscomp.CodePrinterTest::testPrintScript
  public void testPrintScript() {
    
    
    Node ast = new Node(Token.SCRIPT,
        new Node(Token.EXPR_RESULT, Node.newString("f")),
        new Node(Token.EXPR_RESULT, Node.newString("g")));
    String result = new CodePrinter.Builder(ast).setPrettyPrint(true).build();
    assertEquals("\"f\";\n\"g\";\n", result);
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit
  public void testObjectLit() {
    assertPrint("({x:1})", "({x:1})");
    assertPrint("var x=({x:1})", "var x={x:1}");
    assertPrint("var x={'x':1}", "var x={\"x\":1}");
    assertPrint("var x={1:1}", "var x={1:1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit2
  public void testObjectLit2() {
    assertPrint("var x={1:1}", "var x={1:1}");
    assertPrint("var x={'1':1}", "var x={1:1}");
    assertPrint("var x={'1.0':1}", "var x={\"1.0\":1}");
    assertPrint("var x={1.5:1}", "var x={\"1.5\":1}");

  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit3
  public void testObjectLit3() {
    assertPrint("var x={3E9:1}",
                "var x={3E9:1}");
    assertPrint("var x={'3000000000':1}", 
                "var x={3E9:1}");
    assertPrint("var x={'3000000001':1}",
                "var x={3000000001:1}");
    assertPrint("var x={'6000000001':1}",  
                "var x={6000000001:1}");
    assertPrint("var x={\"12345678901234567\":1}",  
                "var x={\"12345678901234567\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testObjectLit4
  public void testObjectLit4() {
    
    assertPrint(
        "var x={\"123456789012345671234567890123456712345678901234567\":1}",
        "var x={\"123456789012345671234567890123456712345678901234567\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testGetter
  public void testGetter() {
    assertPrint("var x = {}", "var x={}");
    assertPrint("var x = {get a() {return 1}}", "var x={get a(){return 1}}");
    assertPrint(
      "var x = {get a() {}, get b(){}}",
      "var x={get a(){},get b(){}}");

    assertPrint(
      "var x = {get 'a'() {return 1}}",
      "var x={get \"a\"(){return 1}}");

    assertPrint(
      "var x = {get 1() {return 1}}",
      "var x={get 1(){return 1}}");

    assertPrint(
      "var x = {get \"()\"() {return 1}}",
      "var x={get \"()\"(){return 1}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testSetter
  public void testSetter() {
    assertPrint("var x = {}", "var x={}");
    assertPrint(
       "var x = {set a(y) {return 1}}",
       "var x={set a(y){return 1}}");

    assertPrint(
      "var x = {get 'a'() {return 1}}",
      "var x={get \"a\"(){return 1}}");

    assertPrint(
      "var x = {set 1(y) {return 1}}",
      "var x={set 1(y){return 1}}");

    assertPrint(
      "var x = {set \"(x)\"(y) {return 1}}",
      "var x={set \"(x)\"(y){return 1}}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testNegCollapse
  public void testNegCollapse() {
    
    
    assertPrint("var x = - - 2;", "var x=2");
    assertPrint("var x = - (2);", "var x=-2");
  }

// com.google.javascript.jscomp.CodePrinterTest::testStrict
  public void testStrict() {
    String result = parsePrint("var x", false, false, 0, false, true);
    assertEquals("'use strict';var x", result);
  }

// com.google.javascript.jscomp.CodePrinterTest::testArrayLiteral
  public void testArrayLiteral() {
    assertPrint("var x = [,];","var x=[,]");
    assertPrint("var x = [,,];","var x=[,,]");
    assertPrint("var x = [,s,,];","var x=[,s,,]");
    assertPrint("var x = [,s];","var x=[,s]");
    assertPrint("var x = [s,];","var x=[s]");
  }

// com.google.javascript.jscomp.CodePrinterTest::testZero
  public void testZero() {
    assertPrint("var x ='\\0';", "var x=\"\\x00\"");
    assertPrint("var x ='\\x00';", "var x=\"\\x00\"");
    assertPrint("var x ='\\u0000';", "var x=\"\\x00\"");
    assertPrint("var x ='\\u00003';", "var x=\"\\x003\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testUnicode
  public void testUnicode() {
    assertPrint("var x ='\\x0f';", "var x=\"\\u000f\"");
    assertPrint("var x ='\\x68';", "var x=\"h\"");
    assertPrint("var x ='\\x7f';", "var x=\"\\u007f\"");
  }

// com.google.javascript.jscomp.CodePrinterTest::testUnicodeKeyword
  public void testUnicodeKeyword() {
    
    assertPrint("var \\u0069\\u0066 = 1;", "var i\\u0066=1");
    
    assertPrint("var v\\u0061\\u0072 = 1;", "var va\\u0072=1");
    
    assertPrint("var w\\u0068\\u0069\\u006C\\u0065 = 1;"
        + "\\u0077\\u0068il\\u0065 = 2;"
        + "\\u0077h\\u0069le = 3;",
        "var whil\\u0065=1;whil\\u0065=2;whil\\u0065=3");
  }

// com.google.javascript.jscomp.CodePrinterTest::testNumericKeys
  public void testNumericKeys() {
    assertPrint("var x = {010: 1};", "var x={8:1}");
    assertPrint("var x = {'010': 1};", "var x={\"010\":1}");

    assertPrint("var x = {0x10: 1};", "var x={16:1}");
    assertPrint("var x = {'0x10': 1};", "var x={\"0x10\":1}");

    
    assertPrint("var x = {.2: 1};", "var x={\"0.2\":1}");
    assertPrint("var x = {'.2': 1};", "var x={\".2\":1}");

    assertPrint("var x = {0.2: 1};", "var x={\"0.2\":1}");
    assertPrint("var x = {'0.2': 1};", "var x={\"0.2\":1}");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue582
  public void testIssue582() {
    assertPrint("var x = -0.0;", "var x=-0.0");
  }

// com.google.javascript.jscomp.CodePrinterTest::testIssue601
  public void testIssue601() {
    assertPrint("'\\v' == 'v'", "\"\\v\"==\"v\"");
    assertPrint("'\\u000B' == '\\v'", "\"\\x0B\"==\"\\v\"");
    assertPrint("'\\x0B' == '\\v'", "\"\\x0B\"==\"\\v\"");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering1
  public void testWarningGuardOrdering1() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering2
  public void testWarningGuardOrdering2() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering3
  public void testWarningGuardOrdering3() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering4
  public void testWarningGuardOrdering4() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_warning=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOffByDefault
  public void testCheckGlobalThisOffByDefault() {
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithAdvancedMode
  public void testCheckGlobalThisOnWithAdvancedMode() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithErrorFlag
  public void testCheckGlobalThisOnWithErrorFlag() {
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOff
  public void testCheckGlobalThisOff() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testReflectedMethods
  public void testReflectedMethods() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;",
        "function a() {}" +
        "a.prototype.a = function(e, d) { alert(d); };" +
        "var b = goog.c.b(a, {a: 1}),c;" +
        "for (c in b) { b[c].call(b); }" +
        "window.Foo = a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOffByDefault
  public void testTypeParsingOffByDefault() {
    testSame(" function f(a) { return a; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOnWithVerbose
  public void testTypeParsingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=checkTypes");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    args.add("--warning_level=DEFAULT");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    args.add("--jscomp_warning=checkTypes");
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    args.add("--warning_level=DEFAULT");
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    args.add("--warning_level=VERBOSE");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=undefinedVars");
    testSame("x = 3;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForQuiet
  public void testCheckSymbolsOverrideForQuiet() {
    args.add("--warning_level=QUIET");
    args.add("--jscomp_error=undefinedVars");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties1
  public void testCheckUndefinedProperties1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties2
  public void testCheckUndefinedProperties2() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=missingProperties");
    test("var x = {}; var y = x.bar;", CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties3
  public void testCheckUndefinedProperties3() {
    args.add("--warning_level=VERBOSE");
    test("function f() {var x = {}; var y = x.bar;}",
        TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function f(a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag
  public void testDefineFlag() {
    args.add("--define=FOO");
    args.add("--define=\"BAR=5\"");
    args.add("--D"); args.add("CCC");
    args.add("-D"); args.add("DDD");
    test(" var FOO = false;" +
         " var BAR = 3;" +
         " var CCC = false;" +
         " var DDD = false;",
         "var FOO = !0, BAR = 5, CCC = !0, DDD = !0;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag2
  public void testDefineFlag2() {
    args.add("--define=FOO='x\"'");
    test(" var FOO = \"a\";",
         "var FOO = \"x\\\"\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag3
  public void testDefineFlag3() {
    args.add("--define=FOO=\"x'\"");
    test(" var FOO = \"a\";",
         "var FOO = \"x'\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testQuietMode
  public void testQuietMode() {
    args.add("--warning_level=DEFAULT");
    test(" var x;",
         RhinoErrorReporter.PARSE_ERROR);
    args.add("--warning_level=QUIET");
    testSame(" var x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives
  public void testProcessClosurePrimitives() {
    test("var goog = {}; goog.provide('goog.dom');",
         "var goog = {dom:{}};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGetMsgWiring
  public void testGetMsgWiring() throws Exception {
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');",
         "var goog={getMsg:function(a){return a}}, " +
         "MSG_FOO=goog.getMsg('foo');");
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("var goog = {}; goog.getMsg = function(x) { return x; };" +
         " var MSG_FOO = goog.getMsg('foo');" +
         "window['foo'] = MSG_FOO;",
         "window.foo = 'foo';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCssNameWiring
  public void testCssNameWiring() throws Exception {
    test("var goog = {}; goog.getCssName = function() {};" +
         "goog.setCssNameMapping = function() {};" +
         "goog.setCssNameMapping({'goog': 'a', 'button': 'b'});" +
         "var a = goog.getCssName('goog-button');" +
         "var b = goog.getCssName('css-button');" +
         "var c = goog.getCssName('goog-menu');" +
         "var d = goog.getCssName('css-menu');",
         "var goog = { getCssName: function() {}," +
         "             setCssNameMapping: function() {} }," +
         "    a = 'a-b'," +
         "    b = 'css-b'," +
         "    c = 'a-menu'," +
         "    d = 'css-menu';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70
  public void testIssue70() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue81
  public void testIssue81() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    useStringComparison = true;
    test("eval('1'); var x = eval; x('2');",
         "eval(\"1\");(0,eval)(\"2\");");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue115
  public void testIssue115() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--jscomp_off=es5Strict");
    args.add("--warning_level=VERBOSE");
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue297
  public void testIssue297() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}",
         "function f(b) {" +
         " var a;" +
         " return ((a=b.id) && (a=parseInt(a.substr(1))) && 0<a);" +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue504
  public void testIssue504() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("void function() { alert('hi'); }();",
         "alert('hi');", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601
  public void testIssue601() {
    args.add("--compilation_level=WHITESPACE_ONLY");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "function f(){return'\\v'=='v'}window['f']=f");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601b
  public void testIssue601b() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\v' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\v'=='v'}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue601c
  public void testIssue601c() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { return '\\u000B' == 'v'; } window['f'] = f;",
         "window.f=function(){return'\\u000B'=='v'}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    test("function foo(a) {}",
         "function foo() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {}" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "throw (new function() {}).a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {}" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "throw (new function Foo() {}).$x$;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag1
  public void testBooleanFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag2
  public void testBooleanFlag2() {
    args.add("--debug");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    assertFalse(
        createCommandLineRunner(
            new String[] {"function f() {}"}).shouldRunCompiler());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting1
  public void testExternsLifting1() throws Exception{
    String code = " function f() {}";
    test(new String[] {code},
         new String[] {});

    assertEquals(2, lastCompiler.getExternsForTesting().size());

    CompilerInput extern = lastCompiler.getExternsForTesting().get(1);
    assertNull(extern.getModule());
    assertTrue(extern.isExtern());
    assertEquals(code, extern.getCode());

    assertEquals(1, lastCompiler.getInputsForTesting().size());

    CompilerInput input = lastCompiler.getInputsForTesting().get(0);
    assertNotNull(input.getModule());
    assertFalse(input.isExtern());
    assertEquals("", input.getCode());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting2
  public void testExternsLifting2() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {" function f() {}", "f(3);"},
         new String[] {"f(3);"},
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOff
  public void testSourceSortingOff() {
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         }, ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn
  public void testSourceSortingOn() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps1
  public void testSourceSortingCircularDeps1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps2
  public void testSourceSortingCircularDeps2() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('roses.lime.juice');",
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');",
          "goog.provide('gimlet');" +
          "     goog.require('gin'); goog.require('roses.lime.juice');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn1
  public void testSourcePruningOn1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn2
  public void testSourcePruningOn2() {
    args.add("--closure_entry_point=guinness");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var guinness = {};"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn3
  public void testSourcePruningOn3() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn4
  public void testSourcePruningOn4() {
    args.add("--closure_entry_point=scotch");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn5
  public void testSourcePruningOn5() {
    args.add("--closure_entry_point=shiraz");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         Compiler.MISSING_ENTRY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn6
  public void testSourcePruningOn6() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testForwardDeclareDroppedTypes
  public void testForwardDeclareDroppedTypes() {
    args.add("--manage_closure_dependencies=true");

    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}",
          "goog.provide('Scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         });

    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         },
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--create_source_map=%outname%.map");
    testSame("var x = 3;");
    assertEquals("/path/to/out.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion2
  public void testSourceMapExpansion2() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat1
  public void testSourceMapFormat1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.DEFAULT,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat2
  public void testSourceMapFormat2() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--source_map_format=V3");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.V3,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testModuleWrapperBaseNameExpansion
  public void testModuleWrapperBaseNameExpansion() throws Exception {
    useModules = ModulePattern.CHAIN;
    args.add("--module_wrapper=m0:%s 
    testSame(new String[] {
      "var x = 3;",
      "var y = 4;"
    });

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.writeModuleOutput(
        builder,
        lastCompiler.getModuleGraph().getRootModule());
    assertEquals("var x=3; 
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
  public void testCharSetExpansion() {
    testSame("");
    assertEquals("US-ASCII", lastCompiler.getOptions().outputCharset);
    args.add("--charset=UTF-8");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m1}\n" +
        "i2\n" +
        "\n" +
        "{m3:m2}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testStarModuleManifest
  public void testStarModuleManifest() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestOrBundleTo(
        lastCompiler.getModuleGraph(), builder, true);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m0}\n" +
        "i2\n" +
        "\n" +
        "{m3:m0}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag
  public void testVersionFlag() {
    args.add("--version");
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag2
  public void testVersionFlag2() {
    lastArg = "--version";
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testPrintAstFlag
  public void testPrintAstFlag() {
    args.add("--print_ast=true");
    testSame("");
    assertEquals(
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"BLOCK\"];\n" +
        "  node1 [label=\"SCRIPT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> RETURN [label=\"SYN_BLOCK\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n\n",
        new String(outReader.toByteArray()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSyntheticExterns
  public void testSyntheticExterns() {
    externs = ImmutableList.of(
        JSSourceFile.fromCode("externs", "myVar.property;"));
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         "var theirVar={},myVar={},yourVar={};");

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var myVar = {};",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGoogAssertStripping
  public void testGoogAssertStripping() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("goog.asserts.assert(false)",
         "");
    args.add("--debug");
    test("goog.asserts.assert(false)", "goog.$asserts$.$assert$(!1)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testMissingReturnCheckOnWithVerbose
  public void testMissingReturnCheckOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {f()} f();",
        CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGenerateExports
  public void testGenerateExports() {
    args.add("--generate_exports=true");
    test(" foo.prototype.x = function() {};",
        "foo.prototype.x=function(){};"+
        "goog.exportSymbol(\"foo.prototype.x\",foo.prototype.x);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDepreciationWithVerbose
  public void testDepreciationWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {}; f()",
       CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTwoParseErrors
  public void testTwoParseErrors() {
    
    
    Compiler compiler = compile(new String[] {
      "var a b;",
      "var b c;"
    });
    assertEquals(2, compiler.getErrors().length);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES3ByDefault
  public void testES3ByDefault() {
    test("var x = f.function", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5
  public void testES5() {
    args.add("--language_in=ECMASCRIPT5");
    test("var x = f.function", "var x = f.function");
    test("var let", "var let");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5Strict
  public void testES5Strict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    test("var x = f.function", "'use strict';var x = f.function");
    test("var let", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrict
  public void testES5StrictUseStrict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5StrictUseStrictMultipleInputs
  public void testES5StrictUseStrictMultipleInputs() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    Compiler compiler = compile(new String[] {"var x = f.function",
        "var y = f.function", "var z = f.function"});
    String outputSource = compiler.toSource();
    assertEquals("'use strict'", outputSource.substring(0, 12));
    assertEquals(outputSource.substring(13).indexOf("'use strict'"), -1);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordDefault
  public void testWithKeywordDefault() {
    test("var x = {}; with (x) {}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWithKeywordWithEs5ChecksOff
  public void testWithKeywordWithEs5ChecksOff() {
    args.add("--jscomp_off=es5Strict");
    testSame("var x = {}; with (x) {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testNoSrCFilesWithManifest
  public void testNoSrCFilesWithManifest() throws IOException {
    args.add("--use_only_custom_externs=true");
    args.add("--output_manifest=test.MF");
    CommandLineRunner runner = createCommandLineRunner(new String[0]);
    String expectedMessage = "";
    try {
      runner.doRun();
    } catch (FlagUsageException e) {
      expectedMessage = e.getMessage();
    }
    assertEquals(expectedMessage, "Bad --js flag. " +
      "Manifest files cannot be generated when the input is from stdin.");
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderColumnAfterResetDummy
  public void testCodeBuilderColumnAfterResetDummy() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("");
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderColumnAfterReset
  public void testCodeBuilderColumnAfterReset() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    String js = "foo();\ngoo();";
    cb.append(js);
    assertEquals(js, cb.toString());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.reset();

    assertTrue(cb.toString().isEmpty());
    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCodeBuilderAppend
  public void testCodeBuilderAppend() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("foo();");
    assertEquals(0, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());

    cb.append("goo();");

    assertEquals(0, cb.getLineIndex());
    assertEquals(12, cb.getColumnIndex());

    
    cb.append("blah();\ngoo();");

    assertEquals(1, cb.getLineIndex());
    assertEquals(6, cb.getColumnIndex());
  }

// com.google.javascript.jscomp.CompilerTest::testCyclicalDependencyInInputs
  public void testCyclicalDependencyInInputs() {
    JSSourceFile[] inputs = {
        JSSourceFile.fromCode(
            "gin", "goog.provide('gin'); goog.require('tonic'); var gin = {};"),
        JSSourceFile.fromCode("tonic",
            "goog.provide('tonic'); goog.require('gin'); var tonic = {};"),
        JSSourceFile.fromCode(
            "mix", "goog.require('gin'); goog.require('tonic');")};
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    options.manageClosureDependencies = true;
    Compiler compiler = new Compiler();
    compiler.init(new JSSourceFile[0], inputs, options);
    compiler.parseInputs();
    assertEquals(compiler.externAndJsRoot, compiler.jsRoot.getParent());
    assertEquals(compiler.externAndJsRoot, compiler.externsRoot.getParent());
    assertNotNull(compiler.externAndJsRoot);
  }

// com.google.javascript.jscomp.CompilerTest::testLocalUndefined
  public void testLocalUndefined() throws Exception {
    
    
    
    
    
    
    
    
    CompilerOptions options = new CompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
        options);
    Compiler compiler = new Compiler();
    JSSourceFile externs = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js",
        "(function (undefined) { alert(undefined); })();");
    compiler.compile(externs, input, options);
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod1
  public void testMovePrototypeMethod1() {
    testSame(createModuleChain(
                 "function Foo() {}" +
                 "Foo.prototype.bar = function() {};",
                 
                 "(new Foo).bar()"));

    canMoveExterns = true;
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.bar = function() {};",
             
             "(new Foo).bar()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.bar = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.bar = JSCompiler_unstubMethod(0, function() {});" +
             "(new Foo).bar()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod2
  public void testMovePrototypeMethod2() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype = { method: function() {} };",
             
             "(new Foo).method()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype = { method: JSCompiler_stubMethod(0) };",
             
             "Foo.prototype.method = " +
             "    JSCompiler_unstubMethod(0, function() {});" +
             "(new Foo).method()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeMethod3
  public void testMovePrototypeMethod3() {
    testSame(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype = { get method() {} };",
             
             "(new Foo).method()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMovePrototypeRecursiveMethod
  public void testMovePrototypeRecursiveMethod() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() { this.baz(); };",
             
             "(new Foo).baz()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, " +
             "    function() { this.baz(); });" +
             "(new Foo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testCantMovePrototypeProp
  public void testCantMovePrototypeProp() {
    testSame(createModuleChain(
                 "function Foo() {}" +
                 "Foo.prototype.baz = goog.nullFunction;",
                 
                 "(new Foo).baz()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsInRightOrder
  public void testMoveMethodsInRightOrder() {
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() { return 1; };" +
             "Foo.prototype.baz = function() { return 2; };",
             
             "(new Foo).baz()"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(1);" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(1, function() { return 1; });" +
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(0, function() { return 2; });" +
             "(new Foo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsInRightOrder2
  public void testMoveMethodsInRightOrder2() {
    JSModule[] m = createModules(
        "function Foo() {}" +
        "Foo.prototype.baz = function() { return 1; };" +
        "function Goo() {}" +
        "Goo.prototype.baz = function() { return 2; };",
        
        "",
        
        "(new Foo).baz()",
        
        "",
        
        "(new Goo).baz()");

    m[1].addDependency(m[0]);
    m[2].addDependency(m[1]);
    m[3].addDependency(m[2]);
    m[4].addDependency(m[2]);

    test(m,
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(1);" +
             "function Goo() {}" +
             "Goo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "",
             
             "Foo.prototype.baz = " +
             "JSCompiler_unstubMethod(1, function() { return 1; });" +
             "Goo.prototype.baz = " +
             "JSCompiler_unstubMethod(0, function() { return 2; });" +
             "(new Foo).baz()",
             
             "",
             
             "(new Goo).baz()"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsUsedInTwoModules
  public void testMoveMethodsUsedInTwoModules() {
    testSame(createModuleStar(
                 "function Foo() {}" +
                 "Foo.prototype.baz = function() {};",
                 
                 "(new Foo).baz()",
                 
                 "(new Foo).baz()"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testMoveMethodsUsedInTwoModules2
  public void testMoveMethodsUsedInTwoModules2() {
    JSModule[] modules = createModules(
        "function Foo() {}" +
        "Foo.prototype.baz = function() {};",
        
        "", 
        
        "(new Foo).baz() + 1",
        
        "(new Foo).baz() + 2");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);
    test(modules,
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});",
             
             "(new Foo).baz() + 1",
             
             "(new Foo).baz() + 2"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testTwoMethods
  public void testTwoMethods() {}

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testTwoMethods2
  public void testTwoMethods2() {
    
    
    test(createModuleChain(
             "function Foo() {}" +
             "Foo.prototype.baz = function() {};",
             
             "(new Foo).callBaz()",
             
             "Foo.prototype.callBaz = function() { this.baz(); }"),
         new String[] {
             STUB_DECLARATIONS +
             "function Foo() {}" +
             "Foo.prototype.baz = JSCompiler_stubMethod(0);",
             
             "(new Foo).callBaz()",
             
             "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});" +
             "Foo.prototype.callBaz = function() { this.baz(); };"
         });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testGlobalFunctionsInGraph
  public void testGlobalFunctionsInGraph() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function() {};" +
            "function x() { return (new Foo).baz(); }",
            
            "x();"),
        new String[] {
          STUB_DECLARATIONS +
          "function Foo() {}" +
          "Foo.prototype.baz = JSCompiler_stubMethod(0);" +
          "function x() { return (new Foo).baz(); }",
          
          "Foo.prototype.baz = JSCompiler_unstubMethod(0, function() {});" +
          "x();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads1
  public void testClosureVariableReads1() {
    testSame(createModuleChain(
            "function Foo() {}" +
            "(function() {" +
            "var x = 'x';" +
            "Foo.prototype.baz = function() {x};" +
            "})();",
            
            "var y = new Foo(); y.baz();"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads2
  public void testClosureVariableReads2() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.b1 = function() {" +
            "  var x = 1;" +
            "  Foo.prototype.b2 = function() {" +
            "    Foo.prototype.b3 = function() {" +
            "      x;" +
            "    }" +
            "  }" +
            "};",
            
            "var y = new Foo(); y.b1();",
            
            "y = new Foo(); z.b2();",
            
            "y = new Foo(); z.b3();"
            ),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.b1 = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.b1 = JSCompiler_unstubMethod(0, function() {" +
           "  var x = 1;" +
           "  Foo.prototype.b2 = function() {" +
           "    Foo.prototype.b3 = function() {" +
           "      x;" +
           "    }" +
           "  }" +
           "});" +
           "var y = new Foo(); y.b1();",
           
           "y = new Foo(); z.b2();",
           
           "y = new Foo(); z.b3();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testClosureVariableReads3
  public void testClosureVariableReads3() {}

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testNoClosureVariableReads1
  public void testNoClosureVariableReads1() {
    test(createModuleChain(
            "function Foo() {}" +
            "var x = 'x';" +
            "Foo.prototype.baz = function(){x};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "var x = 'x';" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(0, function(){x});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testNoClosureVariableReads2
  public void testNoClosureVariableReads2() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function(){var x = 1;x};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(" +
           "    0, function(){var x = 1; x});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testInnerFunctionClosureVariableReads
  public void testInnerFunctionClosureVariableReads() {
    test(createModuleChain(
            "function Foo() {}" +
            "Foo.prototype.baz = function(){var x = 1;" +
            "  return function(){x}};",
            
            "var y = new Foo(); y.baz();"),
         new String[] {
           STUB_DECLARATIONS +
           "function Foo() {}" +
           "Foo.prototype.baz = JSCompiler_stubMethod(0);",
           
           "Foo.prototype.baz = JSCompiler_unstubMethod(" +
           "    0, function(){var x = 1; return function(){x}});" +
           "var y = new Foo(); y.baz();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testIssue600
  public void testIssue600() {
    testSame(
        createModuleChain(
            "var jQuery1 = (function() {\n" +
            "  var jQuery2 = function() {};\n" +
            "  var theLoneliestNumber = 1;\n" +
            "  jQuery2.prototype = {\n" +
            "    size: function() {\n" +
            "      return theLoneliestNumber;\n" +
            "    }\n" +
            "  };\n" +
            "  return jQuery2;\n" +
            "})();\n",

            "(function() {" +
            "  var div = jQuery1('div');" +
            "  div.size();" +
            "})();"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testIssue600b
  public void testIssue600b() {
    testSame(
        createModuleChain(
            "var jQuery1 = (function() {\n" +
            "  var jQuery2 = function() {};\n" +
            "  jQuery2.prototype = {\n" +
            "    size: function() {\n" +
            "      return 1;\n" +
            "    }\n" +
            "  };\n" +
            "  return jQuery2;\n" +
            "})();\n",

            "(function() {" +
            "  var div = jQuery1('div');" +
            "  div.size();" +
            "})();"));
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testIssue600c
  public void testIssue600c() {
    test(
        createModuleChain(
            "var jQuery2 = function() {};\n" +
            "jQuery2.prototype = {\n" +
            "  size: function() {\n" +
            "    return 1;\n" +
            "  }\n" +
            "};\n",

            "(function() {" +
            "  var div = jQuery2('div');" +
            "  div.size();" +
            "})();"),
        new String[] {
            STUB_DECLARATIONS +
            "var jQuery2 = function() {};\n" +
            "jQuery2.prototype = {\n" +
            "  size: JSCompiler_stubMethod(0)\n" +
            "};\n",
            "jQuery2.prototype.size=" +
            "    JSCompiler_unstubMethod(0,function(){return 1});" +
            "(function() {" +
            "  var div = jQuery2('div');" +
            "  div.size();" +
            "})();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testIssue600d
  public void testIssue600d() {
    test(
        createModuleChain(
            "var jQuery2 = function() {};\n" +
            "(function() {" +
            "  jQuery2.prototype = {\n" +
            "    size: function() {\n" +
            "      return 1;\n" +
            "    }\n" +
            "  };\n" +
            "})();",

            "(function() {" +
            "  var div = jQuery2('div');" +
            "  div.size();" +
            "})();"),
        new String[] {
            STUB_DECLARATIONS +
            "var jQuery2 = function() {};\n" +
            "(function() {" +
            "  jQuery2.prototype = {\n" +
            "    size: JSCompiler_stubMethod(0)\n" +
            "  };\n" +
            "})();",
            "jQuery2.prototype.size=" +
            "    JSCompiler_unstubMethod(0,function(){return 1});" +
            "(function() {" +
            "  var div = jQuery2('div');" +
            "  div.size();" +
            "})();"
        });
  }

// com.google.javascript.jscomp.CrossModuleMethodMotionTest::testIssue600e
  public void testIssue600e() {
    testSame(
        createModuleChain(
            "var jQuery2 = function() {};\n" +
            "(function() {" +
            "  var theLoneliestNumber = 1;\n" +
            "  jQuery2.prototype = {\n" +
            "    size: function() {\n" +
            "      return theLoneliestNumber;\n" +
            "    }\n" +
            "  };\n" +
            "})();",

            "(function() {" +
            "  var div = jQuery2('div');" +
            "  div.size();" +
            "})();"));
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbol
  public void testExportSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolDefinedInVar
  public void testExportSymbolDefinedInVar() throws Exception {
    compileAndCheck("var a = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportProperty
  public void testExportProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'cprop', a.b.c)",
                    "var a = {};\n" +
                    "a.b = {};\n" +
                    "\n" +
                    "a.b.cprop = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple
  public void testExportMultiple() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('a.b', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var a = {};\n" +
                    "\n" +
                    "a.b = function(p1) {\n};\n" +
                    "\n" +
                    "a.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "a.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple2
  public void testExportMultiple2() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('hello', a);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var hello = {};\n" +
                    "hello.b = {};\n" +
                    "\n" +
                    "hello.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "hello.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple3
  public void testExportMultiple3() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('prefix', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);",

                    "\n" +
                    "var prefix = function(p1) {\n};\n" +
                    "\n" +
                    "prefix.c = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol
  public void testExportNonStaticSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = {}; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "var foobar = {};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol2
  public void testExportNonStaticSymbol2() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = null; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c())",
                    "var foobar = {};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonexistentProperty
  public void testExportNonexistentProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'none', a.b.none)",
                    "var a = {};\n" +
                    "a.b = {};\n" +
                    "a.b.none = {};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithTypeAnnotation
  public void testExportSymbolWithTypeAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithoutTypeCheck
  public void testExportSymbolWithoutTypeCheck() {
    
    
    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructor
  public void testExportSymbolWithConstructor() {
    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructorWithoutTypeCheck
  public void testExportSymbolWithConstructorWithoutTypeCheck() {
    
    
    
    
    

    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments
  public void testExportFunctionWithOptionalArguments() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithVariableArguments
  public void testExportFunctionWithVariableArguments() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportEnum
   public void testExportEnum() {
     compileAndCheck(
         " var E = {A:1, B:2};" +
         "goog.exportSymbol('E', E);",
         
         
         "var E = {};\n"
     );
   }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportDontEmitPrototypePathPrefix
  public void testExportDontEmitPrototypePathPrefix() {
    compileAndCheck(
        "\n" +
        "var Foo = function() {};" +
        "\n" +
        "Foo.prototype.m = function() {return 6;};\n" +
        "goog.exportSymbol('Foo', Foo);\n" +
        "goog.exportProperty(Foo.prototype, 'm', Foo.prototype.m);",
        "\n" +
        "var Foo = function() {\n};\n" +
        "\n" +
        "Foo.prototype.m = function() {\n};\n"
    );
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testUseExportsAsExterns
  public void testUseExportsAsExterns() {
    String librarySource =
    "\n" +
    "var InternalName = function(a) {" +
    "};" +
    "goog.exportSymbol('ExternalName', InternalName)";

    String clientSource =
      "var a = new ExternalName(6);\n" +
      "\n" +
      "var b = function(x) {};";

    Result libraryCompileResult = compileAndExportExterns(librarySource);

    assertEquals(0, libraryCompileResult.warnings.length);
    assertEquals(0, libraryCompileResult.errors.length);

    String generatedExterns = libraryCompileResult.externExport;

    Result clientCompileResult = compileAndExportExterns(clientSource,
        generatedExterns);

    assertEquals(0, clientCompileResult.warnings.length);
    assertEquals(0, clientCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testWarnOnExportFunctionWithUnknownReturnType
  public void testWarnOnExportFunctionWithUnknownReturnType() {
    String librarySource =
      "var InternalName = function() {" +
      "  return 6;" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(1, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testDontWarnOnExportConstructorWithUnknownReturnType
  public void testDontWarnOnExportConstructorWithUnknownReturnType() {
    String librarySource =
      "\n " +
      "var InternalName = function() {" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(0, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testWarnOnExportFunctionWithUnknownParameterTypes
  public void testWarnOnExportFunctionWithUnknownParameterTypes() {
    
    String librarySource =
      "\n " +
      "var InternalName = function(a,b,c) {" +
      "  return 6;" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(2, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties
  public void testAnalyzeUnusedPrototypeProperties() {
    
    test(" \n" +
        "function e(){} \n" +
        "e.prototype.a = function(){};" +
        "e.prototype.b = function(){};" +
        "var x = new e; x.a()",

        "function e(){}" +
        " e.prototype.a = function(){};" +
        "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties2
  public void testAnalyzeUnusedPrototypeProperties2() {
    
    
    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties3
  public void testAnalyzeUnusedPrototypeProperties3() {
    
    
    test(" \n" +
        "function e(){} \n" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           
           "var x = new e; x.a()");

    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAliasing
  public void testAliasing() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testStatement
  public void testStatement() {
    test(" \n" +
         "" +
        "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAnalyzePrototypeProperties
  public void testAnalyzePrototypeProperties() {
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.b = function(){};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "var x = new e; x.a()");

    
    test("function e(){}" +
           "e.prototype = {a: function(){}, b: function(){}};" +
           "var x=new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}};" +
           "var x = new e; x.a()");

    
    
    test("function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e; x.a()");
    test("function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()",
         "function e(){}" +
           "e.prototype = {a: function(){}, bExtern: function(){}};" +
           "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing1
  public void testAliasing1() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x = new e; x.method1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "var x = new e; x.method1()");

    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "e.prototype.alias2 = e.prototype.method2;" +
           "var x=new e; x.alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "var x = new e; x.alias1()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing2
  public void testAliasing2() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).method1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "(new e).method1()");

    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).alias1()",
         "function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.alias1 = e.prototype.method1;" +
           "(new e).alias1()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing3
  public void testAliasing3() {
    
    test("function e(){}" +
           "e.prototype.method1 = function(){};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype['alias1'] = e.prototype.method1;" +
           "e.prototype['alias2'] = e.prototype.method2;",
         "function e(){}" +
           "e.prototype.method1=function(){};" +
           "e.prototype.method2=function(){};" +
           "e.prototype[\"alias1\"]=e.prototype.method1;" +
           "e.prototype[\"alias2\"]=e.prototype.method2;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing4
  public void testAliasing4() {
    
    test("function e(){}" +
           "e.prototype['alias1'] = e.prototype.method1 = function(){};" +
           "e.prototype['alias2'] = e.prototype.method2 = function(){};",
         "function e(){}" +
           "e.prototype[\"alias1\"]=e.prototype.method1=function(){};" +
           "e.prototype[\"alias2\"]=e.prototype.method2=function(){};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing5
  public void testAliasing5() {
    
    
    test("function e(){}" +
           "e.prototype.method1 = function(){this.method2()};" +
           "e.prototype.method2 = function(){};" +
           
           "e.prototype['alias1'] = e.prototype.method1;",
         "function e(){}" +
           "e.prototype.method1=function(){this.method2()};" +
           "e.prototype.method2=function(){};" +
           "e.prototype[\"alias1\"]=e.prototype.method1;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing6
  public void testAliasing6() {
    
    
    test("function e(){}" +
           "e.prototype.method1 = function(){this.method2()};" +
           "e.prototype.method2 = function(){};" +
           
           "window['alias1'] = e.prototype.method1;",
         "function e(){}" +
           "e.prototype.method1=function(){this.method2()};" +
           "e.prototype.method2=function(){};" +
           "window['alias1']=e.prototype.method1;");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testAliasing7
  public void testAliasing7() {
    
    
    testSame("function e(){}" +
           "e.prototype['alias1'] = e.prototype.method1 = " +
               "function(){this.method2()};" +
           "e.prototype.method2 = function(){};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testStatementRestriction
  public void testStatementRestriction() {
    test("function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testMethodsFromExternsFileNotExported
  public void testMethodsFromExternsFileNotExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +
        "Foo.prototype.unused = function() {};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    String compiled =
        "function Foo(){}" +
        "var instance = new Foo;";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    canRemoveExterns = true;
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.method = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var _externInstance = new Foo();" +
        "Foo.prototype._externMethod = Foo.prototype.method";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.method = function(){};" +
        "var _externInstance = new Foo;" +
        "Foo.prototype._externMethod = Foo.prototype.method";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testExternMethodsFromExternsFile
  public void testExternMethodsFromExternsFile() {
    String classAndItsMethodAliasedAsExtern =
        "function Foo() {}" +
        "Foo.prototype.bar_ = function() {};" +  
        "Foo.prototype.unused = function() {};" +  
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";  

    String compiled =
        "function Foo(){}" +
        "Foo.prototype.bar_ = function(){};" +
        "var instance = new Foo;" +
        "Foo.prototype.bar = Foo.prototype.bar_";

    test(classAndItsMethodAliasedAsExtern, compiled);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertyReferenceGraph
  public void testPropertyReferenceGraph() {
    
    
    String constructor = "function Foo() {}";
    String defA =
        "Foo.prototype.a = function() { Foo.superClass_.a.call(this); };";
    String defB = "Foo.prototype.b = function() { this.a(); };";
    String defC = "Foo.prototype.c = function() { " +
        "Foo.superClass_.c.call(this); this.b(); this.a(); };";
    String defD = "Foo.prototype.d = function() { this.c(); };";
    String defE = "Foo.prototype.e = function() { this.a(); this.f(); };";
    String defF = "Foo.prototype.f = function() { };";
    String fullClassDef = constructor + defA + defB + defC + defD + defE + defF;

    
    test(fullClassDef, "");

    
    String callA = "(new Foo()).a();";
    String callB = "(new Foo()).b();";
    String callC = "(new Foo()).c();";
    String callD = "(new Foo()).d();";
    String callE = "(new Foo()).e();";
    String callF = "(new Foo()).f();";
    test(fullClassDef + callA, constructor + defA + callA);
    test(fullClassDef + callB, constructor + defA + defB + callB);
    test(fullClassDef + callC, constructor + defA + defB + defC + callC);
    test(fullClassDef + callD, constructor + defA + defB + defC + defD + callD);
    test(fullClassDef + callE, constructor + defA + defE + defF + callE);
    test(fullClassDef + callF, constructor + defF + callF);

    test(fullClassDef + callA + callC,
         constructor + defA + defB + defC + callA + callC);
    test(fullClassDef + callB + callC,
         constructor + defA + defB + defC + callB + callC);
    test(fullClassDef + callA + callB + callC,
         constructor + defA + defB + defC + callA + callB + callC);
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertiesDefinedWithGetElem
  public void testPropertiesDefinedWithGetElem() {
    testSame("function Foo() {} Foo.prototype['elem'] = function() {};");
    testSame("function Foo() {} Foo.prototype[1 + 1] = function() {};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testNeverRemoveImplicitlyUsedProperties
  public void testNeverRemoveImplicitlyUsedProperties() {
    testSame("function Foo() {} " +
             "Foo.prototype.length = 3; " +
             "Foo.prototype.toString = function() { return 'Foo'; }; " +
             "Foo.prototype.valueOf = function() { return 'Foo'; }; ");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testPropertyDefinedInBranch
  public void testPropertyDefinedInBranch() {
    test("function Foo() {} if (true) Foo.prototype.baz = function() {};",
         "if (true);");
    test("function Foo() {} while (true) Foo.prototype.baz = function() {};",
         "while (true);");
    test("function Foo() {} for (;;) Foo.prototype.baz = function() {};",
         "for (;;);");
    test("function Foo() {} do Foo.prototype.baz = function() {}; while(true);",
         "do; while(true);");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testUsingAnonymousObjectsToDefeatRemoval
  public void testUsingAnonymousObjectsToDefeatRemoval() {
    String constructor = "function Foo() {}";
    String declaration = constructor + "Foo.prototype.baz = 3;";
    test(declaration, "");
    testSame(declaration + "var x = {}; x.baz = 5;");
    testSame(declaration + "var x = {baz: 5};");
    test(declaration + "var x = {'baz': 5};",
         "var x = {'baz': 5};");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph
  public void testGlobalFunctionsInGraph() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() {}" +
        "Foo.prototype.baz = function() { y(); };",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph2
  public void testGlobalFunctionsInGraph2() {
    
    
    
    
    
    
    testSame(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { y(); };");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph3
  public void testGlobalFunctionsInGraph3() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { x(); };",
        "var x = function() { (new Foo).baz(); };" +
        "function Foo() { this.baz(); }" +
        "Foo.prototype.baz = function() { x(); };");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph4
  public void testGlobalFunctionsInGraph4() {
    test(
        "var x = function() { (new Foo).baz(); };" +
        "var y = function() { x(); };" +
        "function Foo() { Foo.prototype.baz = function() { y(); }; }",
        "");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph5
  public void testGlobalFunctionsInGraph5() {
    test(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };",
        "");

    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };",

        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph6
  public void testGlobalFunctionsInGraph6() {
    testSame(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "function x() { (new Foo).methodA(); }" +
        "Foo.prototype.methodB = function() { x(); };" +
        "(new Foo).methodB();");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGlobalFunctionsInGraph7
  public void testGlobalFunctionsInGraph7() {
    testSame(
        "function Foo() {}" +
        "Foo.prototype.methodA = function() {};" +
        "this.methodA();");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGetterBaseline
  public void testGetterBaseline() {
    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype = { " +
        "  methodA: function() {}," +
        "  methodB: function() { x(); }" +
        "};" +
        "function x() { (new Foo).methodA(); }",

        "function Foo() {}" +
        "Foo.prototype = { " +
        "  methodA: function() {}" +
        "};" +
        "function x() { (new Foo).methodA(); }");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGetter1
  public void testGetter1() {
    test(
      "function Foo() {}" +
      "Foo.prototype = { " +
      "  get methodA() {}," +
      "  get methodB() { x(); }" +
      "};" +
      "function x() { (new Foo).methodA; }",

      "function Foo() {}" +
      "Foo.prototype = {};");

    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}," +
        "  get methodB() { x(); }" +
        "};" +
        "function x() { (new Foo).methodA; }",

        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}" +
        "};" +
        "function x() { (new Foo).methodA; }");
  }

// com.google.javascript.jscomp.RemoveUnusedPrototypePropertiesTest::testGetter2
  public void testGetter2() {
    anchorUnusedVars = true;
    test(
        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}," +
        "  set methodA(a) {}," +
        "  get methodB() { x(); }," +
        "  set methodB(a) { x(); }" +
        "};" +
        "function x() { (new Foo).methodA; }",

        "function Foo() {}" +
        "Foo.prototype = { " +
        "  get methodA() {}," +
        "  set methodA(a) {}" +
        "};" +
        "function x() { (new Foo).methodA; }");
  }
