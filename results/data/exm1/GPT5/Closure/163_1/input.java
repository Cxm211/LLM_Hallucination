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
// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypeProperties
  public void testPrototypeProperties() {
    test("Bar.prototype.getA = function(){}; bar.getA();" +
         "Bar.prototype.getB = function(){};",
         "Bar.prototype.a = function(){}; bar.a();" +
         "Bar.prototype.b = function(){}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeys1
  public void testPrototypePropertiesAsObjLitKeys1() {
    test("Bar.prototype = {2: function(){}, getA: function(){}}; bar[2]();",
         "Bar.prototype = {2: function(){}, a: function(){}}; bar[2]();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeys2
  public void testPrototypePropertiesAsObjLitKeys2() {
    testSame("Bar.prototype = {get 2(){}}; bar[2];");

    testSame("Bar.prototype = {get 'a'(){}}; bar['a'];");

    test("Bar.prototype = {get getA(){}}; bar.getA;",
         "Bar.prototype = {get a(){}}; bar.a;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeys3
  public void testPrototypePropertiesAsObjLitKeys3() {
    testSame("Bar.prototype = {set 2(x){}}; bar[2];");

    testSame("Bar.prototype = {set 'a'(x){}}; bar['a'];");

    test("Bar.prototype = {set getA(x){}}; bar.getA;",
         "Bar.prototype = {set a(x){}}; bar.a;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeys1
  public void testMixedQuotedAndUnquotedObjLitKeys1() {
    test("Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
         "Bar = {a: function(){}, 'getB': function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeys2
  public void testMixedQuotedAndUnquotedObjLitKeys2() {
    test("Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
         "Bar = {a: function(){}, 'getB': function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testQuotedPrototypeProperty
  public void testQuotedPrototypeProperty() {
    testSame("Bar.prototype['getA'] = function(){}; bar['getA']();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testOverlappingOriginalAndGeneratedNames
  public void testOverlappingOriginalAndGeneratedNames() {
    test("Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
         "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesWithLeadingUnderscores
  public void testRenamePropertiesWithLeadingUnderscores() {
    test("Bar.prototype = {_getA: function(){}, _b: 0}; bar._getA();",
         "Bar.prototype = {a: function(){}, b: 0}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToObject
  public void testPropertyAddedToObject() {
    test("var foo = {}; foo.prop = '';",
         "var foo = {}; foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToFunction
  public void testPropertyAddedToFunction() {
    test("var foo = function(){}; foo.prop = '';",
         "var foo = function(){}; foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyOfObjectOfUnknownType
  public void testPropertyOfObjectOfUnknownType() {
    test("var foo = x(); foo.prop = '';",
         "var foo = x(); foo.a = '';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSetPropertyOfThis
  public void testSetPropertyOfThis() {
    test("this.prop = 'bar'",
         "this.a = 'bar'");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testReadPropertyOfThis
  public void testReadPropertyOfThis() {
    test("f(this.prop);",
         "f(this.a);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testObjectLiteralInLocalScope
  public void testObjectLiteralInLocalScope() {
    test("function x() { var foo = {prop1: 'bar', prop2: 'baz'}; }",
         "function x() { var foo = {a: 'bar', b: 'baz'}; }");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testIncorrectAttemptToAccessQuotedProperty
  public void testIncorrectAttemptToAccessQuotedProperty() {
    
    test("Bar.prototype = {'B': 0, 'getFoo': function(){}}; bar.getFoo();",
         "Bar.prototype = {'B': 0, 'getFoo': function(){}}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSetQuotedPropertyOfThis
  public void testSetQuotedPropertyOfThis() {
    testSame("this['prop'] = 'bar';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testExternedPropertyName
  public void testExternedPropertyName() {
    test("Bar.prototype = {toString: function(){}, foo: 0}; bar.toString();",
         "Bar.prototype = {toString: function(){}, a: 0}; bar.toString();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testExternedPropertyNameDefinedByObjectLiteral
  public void testExternedPropertyNameDefinedByObjectLiteral() {
    test("function x() { var foo = google.gears.factory; }",
         "function x() { var foo = google.gears.factory; }");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames
  public void testAvoidingConflictsBetweenQuotedAndUnquotedPropertyNames() {
    test("Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
         "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testSamePropertyNameQuotedAndUnquoted
  public void testSamePropertyNameQuotedAndUnquoted() {
    test("Bar.prototype.prop = function(){}; y = {'prop': 0};",
         "Bar.prototype.a = function(){}; y = {'prop': 0};");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStaticAndInstanceMethodWithSameName
  public void testStaticAndInstanceMethodWithSameName() {
    test("Bar = function(){}; Bar.getA = function(){}; " +
         "Bar.prototype.getA = function(){}; Bar.getA(); bar.getA();",
         "Bar = function(){}; Bar.a = function(){}; " +
         "Bar.prototype.a = function(){}; Bar.a(); bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall1
  public void testRenamePropertiesFunctionCall1() {
    test("var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCall2
  public void testRenamePropertiesFunctionCall2() {
    test("var foo = {myProp: 0}; " +
         "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
         "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
         "var foo = {a: 0}; f('b.a.c'); " +
         "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs1
  public void testRemoveRenameFunctionStubs1() {
    test("function JSCompiler_renameProperty(x) { return x; }",
         "");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRemoveRenameFunctionStubs2
  public void testRemoveRenameFunctionStubs2() {
    test("function JSCompiler_renameProperty(x) { return x; }" +
         "var foo = {myProp: 0}; f(foo[JSCompiler_renameProperty('myProp')]);",
         "var foo = {a: 0}; f(foo['a']);");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testGeneratePseudoNames
  public void testGeneratePseudoNames() {
    generatePseudoNames = true;
    test("var foo={}; foo.bar=1; foo['abc']=2",
         "var foo={}; foo.$bar$=1; foo['abc']=2");
    generatePseudoNames = false;
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testModules
  public void testModules() {
    String module1Js = "function Bar(){} Bar.prototype.getA=function(x){};" +
                       "var foo;foo.getA(foo);foo.doo=foo;foo.bloo=foo;";

    String module2Js = "function Far(){} Far.prototype.getB=function(x){};" +
                       "var too;too.getB(too);too.woo=too;too.bloo=too;";

    String module3Js = "function Car(){} Car.prototype.getC=function(x){};" +
                       "var noo;noo.getC(noo);noo.zoo=noo;noo.cloo=noo;";

    JSModule module1 = new JSModule("m1");
    module1.add(JSSourceFile.fromCode("input1", module1Js));

    JSModule module2 = new JSModule("m2");
    module2.add(JSSourceFile.fromCode("input2", module2Js));

    JSModule module3 = new JSModule("m3");
    module3.add(JSSourceFile.fromCode("input3", module3Js));

    JSModule[] modules = new JSModule[] { module1, module2, module3 };
    Compiler compiler = compileModules("", modules);

    Result result = compiler.getResult();
    assertTrue(result.success);

    assertEquals("function Bar(){}Bar.prototype.b=function(x){};" +
                 "var foo;foo.b(foo);foo.f=foo;foo.a=foo;",
                 compiler.toSource(module1));

    assertEquals("function Far(){}Far.prototype.c=function(x){};" +
                 "var too;too.c(too);too.g=too;too.a=too;",
                 compiler.toSource(module2));

    
    
    
    
    
    
    assertEquals("function Car(){}Car.prototype.d=function(x){};" +
                 "var noo;noo.d(noo);noo.h=noo;noo.e=noo;",
                 compiler.toSource(module3));
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAffinity
  public void testPropertyAffinity() {
    
    
    useAffinity = true;
    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
         "function f1() { foo.z; foo.z; foo.z; foo.y}" +
         "function f2() {                      foo.x}",

         "var foo={};foo.c=1;foo.b=2;foo.a=3;" +
         "function f1() { foo.a; foo.a; foo.a; foo.b}" +
         "function f2() {                      foo.c}");

    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
        "function f1() { foo.z; foo.z; foo.z; foo.y}" +
        "function f2() { foo.z; foo.z; foo.z; foo.x}",

        "var foo={};foo.b=1;foo.c=2;foo.a=3;" +
        "function f1() { foo.a; foo.a; foo.a; foo.c}" +
        "function f2() { foo.a; foo.a; foo.a; foo.b}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAffinityOff
  public void testPropertyAffinityOff() {
    useAffinity = false;
    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
         "function f1() { foo.z; foo.z; foo.z; foo.y}" +
         "function f2() {                      foo.x}",

         "var foo={};foo.b=1;foo.c=2;foo.a=3;" +
         "function f1() { foo.a; foo.a; foo.a; foo.c}" +
         "function f2() {                      foo.b}");

    test("var foo={};foo.x=1;foo.y=2;foo.z=3;" +
        "function f1() { foo.z; foo.z; foo.z; foo.y}" +
        "function f2() { foo.z; foo.z; foo.z; foo.x}",

        "var foo={};foo.b=1;foo.c=2;foo.a=3;" +
        "function f1() { foo.a; foo.a; foo.a; foo.c}" +
        "function f2() { foo.a; foo.a; foo.a; foo.b}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesStable
  public void testPrototypePropertiesStable() {
    testStableRenaming(
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}",
        "Bar.prototype.get = function(){}; bar.get();" +
        "Bar.prototype.getA = function(){}; bar.getA();" +
        "Bar.prototype.getB = function(){};",
        "Bar.prototype.c = function(){}; bar.c();" +
        "Bar.prototype.a = function(){}; bar.a();" +
        "Bar.prototype.b = function(){}");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPrototypePropertiesAsObjLitKeysStable
  public void testPrototypePropertiesAsObjLitKeysStable() {
    testStableRenaming(
        "Bar.prototype = {2: function(){}, getA: function(){}}; bar[2]();",
        "Bar.prototype = {2: function(){}, a: function(){}}; bar[2]();",
        "Bar.prototype = {getB: function(){},getA: function(){}}; bar.getB();",
        "Bar.prototype = {b: function(){},a: function(){}}; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testMixedQuotedAndUnquotedObjLitKeysStable
  public void testMixedQuotedAndUnquotedObjLitKeysStable() {
    testStableRenaming(
        "Bar = {getA: function(){}, 'getB': function(){}}; bar.getA();",
        "Bar = {a: function(){}, 'getB': function(){}}; bar.a();",
        "Bar = {get: function(){}, getA: function(){}, 'getB': function(){}};" +
        "bar.getA();bar.get();",
        "Bar = {b: function(){}, a: function(){}, 'getB': function(){}};" +
        "bar.a();bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testOverlappingOriginalAndGeneratedNamesStable
  public void testOverlappingOriginalAndGeneratedNamesStable() {
    testStableRenaming(
        "Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
        "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();",
        "Bar.prototype = {c: function(){}, b: function(){}, a: function(){}};" +
        "bar.b();",
        "Bar.prototype = {c: function(){}, a: function(){}, b: function(){}};" +
        "bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testStableWithTrickyExternsChanges
  public void testStableWithTrickyExternsChanges() {
    test("Bar.prototype = {b: function(){}, a: function(){}}; bar.b();",
         "Bar.prototype = {a: function(){}, b: function(){}}; bar.a();");
    prevUsedPropertyMap = renameProperties.getPropertyMap();
    String externs = EXTERNS + "prop.b;";
    test(externs,
         "Bar.prototype = {new_f: function(){}, b: function(){}, " +
         "a: function(){}};bar.b();",
         "Bar.prototype = {c:function(){}, b:function(){}, a:function(){}};" +
         "bar.b();", null, null);
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesWithLeadingUnderscoresStable
  public void testRenamePropertiesWithLeadingUnderscoresStable() {
    testStableRenaming(
        "Bar.prototype = {_getA: function(){}, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, b: 0}; bar.a();",
        "Bar.prototype = {_getA: function(){}, _c: 1, _b: 0}; bar._getA();",
        "Bar.prototype = {a: function(){}, c: 1,  b: 0}; bar.a();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testPropertyAddedToObjectStable
  public void testPropertyAddedToObjectStable() {
    testStableRenaming("var foo = {}; foo.prop = '';",
                       "var foo = {}; foo.a = '';",
                       "var foo = {}; foo.prop = ''; foo.a='';",
                       "var foo = {}; foo.a = ''; foo.b='';");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable
  public void testAvoidingConflictsBetQuotedAndUnquotedPropertyNamesStable() {
    testStableRenaming(
        "Bar.prototype.foo = function(){}; Bar.prototype['b'] = 0; bar.foo();",
        "Bar.prototype.a = function(){}; Bar.prototype['b'] = 0; bar.a();",
        "Bar.prototype.foo = function(){}; Bar.prototype['a'] = 0; bar.foo();",
        "Bar.prototype.b = function(){}; Bar.prototype['a'] = 0; bar.b();");
  }

// com.google.javascript.jscomp.RenamePropertiesTest::testRenamePropertiesFunctionCallStable
  public void testRenamePropertiesFunctionCallStable() {
    testStableRenaming(
        "var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;",
        "var bar = {newProp: 0}; var foo = {myProp: 0}; " +
        "f(JSCompiler_renameProperty('otherProp.myProp.someProp')); " +
        "foo.myProp = 1; foo.theirProp = 2; foo.yourProp = 3;",
        "var bar = {f: 0}; var foo = {a: 0}; f('b.a.c'); " +
        "foo.a = 1; foo.d = 2; foo.e = 3;");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement1
  public void testPrefixReplacement1() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("pre/","") );
    checkSourceMap2("", "pre/file1", "", "pre/file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"file1\",\"file2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement2
    public void testPrefixReplacement2() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("pre/file","src") );
    checkSourceMap2("", "pre/file1", "", "pre/file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"src1\",\"src2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement3
  public void testPrefixReplacement3() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("file1","x") );
    mappings.add( new SourceMap.LocationMapping("file2","y") );
    checkSourceMap2("", "file1", "", "file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"x\",\"y\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement4
  public void testPrefixReplacement4() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("file1","x") );
    mappings.add( new SourceMap.LocationMapping("file","y") );
    checkSourceMap2("", "file1", "", "file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"x\",\"y2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInline
  public void testSpecializeInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
        );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeCascadedInline
  public void testSpecializeCascadedInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return C()};" +
        "var C = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return C()};" + 
        "C = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithMultipleDependents
  public void testSpecializeInlineWithMultipleDependents() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
        "A();",
        
        "A();"
    );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();",
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();",

    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithNamespaces
  public void testSpecializeInlineWithNamespaces() {
    JSModule[] modules = createModuleStar(
        
        "var ns = {};" +
        
        "ns.A = function() {alert(B());ns.A()};" +
        "var B = function() {return 6};" +
        "ns.A();",
        
        "B = function() {return 7};" +
    "ns.A();");

    test(modules, new String[] {
        
        "var ns = {};" +
        "ns.A = function() {alert(6);ns.A()};" + 
        "ns.A();" +
        "var B;",
        
        "ns.A = function() {alert(B());ns.A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "ns.A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithRegularFunctions
  public void testSpecializeInlineWithRegularFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "function A() {alert(6);A()}" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeLocalNonAnonymousFunctions
  public void testDontSpecializeLocalNonAnonymousFunctions() {
    
    enableNormalize(false);

    JSModule[] modules = createModuleStar(
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        "");

    test(modules, new String[] {
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        ""
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testAddDummyVarsForRemovedFunctions
  public void testAddDummyVarsForRemovedFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B() + C());A()};" +
        "var B = function() {return 6};" +
        "var C = function() {return 8};" +
        "A();",
        
        "" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6 + 8);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B() + C());A()};" + 
        "B = function() {return 6};" + 
        "C = function() {return 8};" + 
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeRemoveUnusedProperties
  public void testSpecializeRemoveUnusedProperties() {
    JSModule[] modules = createModuleStar(
        
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "var aliasA = Foo.prototype.a;" +
        "var x = new Foo();" +
        "x.a();",
        
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_inline
  public void testDontSpecializeAliasedFunctions_inline() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();");

    test(modules, new String[] {
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_remove_unused_properties
  public void testDontSpecializeAliasedFunctions_remove_unused_properties() {
    JSModule[] modules = createModuleStar(
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "Foo.prototype.d = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "Foo.prototype.d = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethods
  public void testSpecializeDevirtualizePrototypeMethods() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};" +
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var JSCompiler_StaticMethods_a =" +
              "function(JSCompiler_StaticMethods_a$self) {" +
           "JSCompiler_StaticMethods_a(JSCompiler_StaticMethods_a$self);" +
           "return 7" +
        "};" +
        "var x = new Foo();" +
        "JSCompiler_StaticMethods_a(x);",
        
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethodsWithInline
  public void testSpecializeDevirtualizePrototypeMethodsWithInline() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {return 7};" +
        "var x = new Foo();" +
        "var z = x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var x = new Foo();" +
        "var z = 7;",
        
        "Foo.prototype.a = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testRemovedFunctions
    public void testRemovedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());

      Node functionF = findFunction("F");

      lastState.reportRemovedFunction(functionF, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF), lastState.getRemovedFunctions());

      Node functionG = findFunction("F");

      lastState.reportRemovedFunction(functionG, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getRemovedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializedFunctions
    public void testSpecializedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());

      Node functionF = findFunction("F");

      lastState.reportSpecializedFunction(functionF);
      assertEquals(ImmutableSet.of(functionF),
          lastState.getSpecializedFunctions());

      Node functionG = findFunction("F");

      lastState.reportSpecializedFunction(functionG);
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getSpecializedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testCanFixupFunction
    public void testCanFixupFunction() {
      testSame("function F(){}\n" +
               "var G = function(a){};\n" +
               "var ns = {};" +
               "ns.H = function(){};" +
               "var ns2 = {I : function anon1(){}};" +
               "(function anon2(){})();");

      assertTrue(lastState.canFixupFunction(findFunction("F")));
      assertTrue(lastState.canFixupFunction(findFunction("G")));
      assertTrue(lastState.canFixupFunction(findFunction("ns.H")));
      assertFalse(lastState.canFixupFunction(findFunction("anon1")));
      assertFalse(lastState.canFixupFunction(findFunction("anon2")));

      
      testSame("function A(){}\n" +
          "var aliasA = A;\n");

      assertFalse(lastState.canFixupFunction(findFunction("A")));
    }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVar
  public void testGlobalVar() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5;");
    assertNull(getGlobalVar(table, "y"));
    assertNotNull(getGlobalVar(table, "x"));
    assertEquals("number", getGlobalVar(table, "x").getType().toString());

    
    assertEquals(2, getVars(table).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences
  public void testGlobalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var x = this; function f() { return this + this + this; }");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(1, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences2
  public void testGlobalThisReferences2() throws Exception {
    
    SymbolTable table = createSymbolTable("");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(0, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences3
  public void testGlobalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable("this.foo = {}; this.foo.bar = {};");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisPropertyReferences
  public void testGlobalThisPropertyReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {} this.Foo;");

    Symbol foo = getGlobalVar(table, "Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVarReferences
  public void testGlobalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5; x = 6;");
    Symbol x = getGlobalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.VAR, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.ASSIGN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalVarReferences
  public void testLocalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "function f(x) { return x; }");
    Symbol x = getLocalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.PARAM_LIST, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.RETURN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences
  public void testLocalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() { this.foo = 3; this.bar = 5; }");

    Symbol f = getGlobalVar(table, "F");
    assertNotNull(f);

    Symbol t = table.getParameterInFunction(f, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences2
  public void testLocalThisReferences2() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}" +
        "F.prototype.baz = " +
        "    function() { this.foo = 3; this.bar = 5; };");

    Symbol baz = getGlobalVar(table, "F.prototype.baz");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences3
  public void testLocalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}");

    Symbol baz = getGlobalVar(table, "F");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(0, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNamespacedReferences
  public void testNamespacedReferences() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.dom = {};" +
        "goog.dom.DomHelper = function(){};");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);
    assertEquals(3, Iterables.size(table.getReferences(goog)));

    Symbol googDom = getGlobalVar(table, "goog.dom");
    assertNotNull(googDom);
    assertEquals(2, Iterables.size(table.getReferences(googDom)));

    Symbol googDomHelper = getGlobalVar(table, "goog.dom.DomHelper");
    assertNotNull(googDomHelper);
    assertEquals(1, Iterables.size(table.getReferences(googDomHelper)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testIncompleteNamespacedReferences
  public void testIncompleteNamespacedReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n" +
        "goog.dom.DomHelper = function(){};\n" +
        "var y = goog.dom.DomHelper;\n");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);
    assertEquals(2, table.getReferenceList(goog).size());

    Symbol googDom = getGlobalVar(table, "goog.dom");
    assertNotNull(googDom);
    assertEquals(2, table.getReferenceList(googDom).size());

    Symbol googDomHelper = getGlobalVar(table, "goog.dom.DomHelper");
    assertNotNull(googDomHelper);
    assertEquals(2, Iterables.size(table.getReferences(googDomHelper)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalRichObjectReference
  public void testGlobalRichObjectReference() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n" +
        "function A(){};\n" +
        " A.prototype.b;\n" +
        " var a = new A();\n" +
        "function g() {\n" +
        "  return a.b ? 'x' : 'y';\n" +
        "}\n" +
        "(function() {\n" +
        "  var x; if (x) { x = a.b.b; } else { x = a.b.c; }\n" +
        "  return x;\n" +
        "})();\n");

    Symbol ab = getGlobalVar(table, "a.b");
    assertNotNull(ab);
    assertEquals(1, table.getReferenceList(ab).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testRemovalOfNamespacedReferencesOfProperties
  public void testRemovalOfNamespacedReferencesOfProperties()
      throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.method = function() {};");

    Symbol domHelper = getGlobalVar(table, "DomHelper");
    assertNotNull(domHelper);

    Symbol domHelperNamespacedMethod = getGlobalVar(table, "DomHelper.method");
    assertEquals("method", domHelperNamespacedMethod.getName());

    Symbol domHelperMethod = domHelper.getPropertyScope().getSlot("method");
    assertNotNull(domHelperMethod);
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogScopeReferences
  public void testGoogScopeReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.scope = function() {};" +
        "goog.scope(function() {});");
    Symbol googScope = getGlobalVar(table, "goog.scope");
    assertNotNull(googScope);
    assertEquals(2, Iterables.size(table.getReferences(googScope)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogRequireReferences
  public void testGoogRequireReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.provide = function() {};" +
        "goog.require = function() {};" +
        "goog.provide('goog.dom');" +
        "goog.require('goog.dom');");
    Symbol goog = getGlobalVar(table, "goog");
    assertNotNull(goog);

    
    
    
    
    
    
    
    
    assertEquals(8, Iterables.size(table.getReferences(goog)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGoogRequireReferences2
  public void testGoogRequireReferences2() throws Exception {
    options.brokenClosureRequiresLevel = CheckLevel.OFF;
    SymbolTable table = createSymbolTable(
        "foo.bar = function(){};  
        + "goog.require('foo.bar')\n");
    Symbol fooBar = getGlobalVar(table, "foo.bar");
    assertNotNull(fooBar);
    assertEquals(2, Iterables.size(table.getReferences(fooBar)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVarInExterns
  public void testGlobalVarInExterns() throws Exception {
    SymbolTable table = createSymbolTable("customExternFn(1);");
    Symbol fn = getGlobalVar(table, "customExternFn");
    List<Reference> refs = table.getReferenceList(fn);
    assertEquals(2, refs.size());

    SymbolScope scope = table.getEnclosingScope(refs.get(0).getNode());
    assertTrue(scope.isGlobalScope());
    assertEquals(SymbolTable.GLOBAL_THIS,
        table.getSymbolForScope(scope).getName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalVarInExterns
  public void testLocalVarInExterns() throws Exception {
    SymbolTable table = createSymbolTable("");
    Symbol arg = getLocalVar(table, "customExternArg");
    List<Reference> refs = table.getReferenceList(arg);
    assertEquals(1, refs.size());

    Symbol fn = getGlobalVar(table, "customExternFn");
    SymbolScope scope = table.getEnclosingScope(refs.get(0).getNode());
    assertFalse(scope.isGlobalScope());
    assertEquals(fn, table.getSymbolForScope(scope));
  }

// com.google.javascript.jscomp.SymbolTableTest::testSymbolsForType
  public void testSymbolsForType() throws Exception {
    SymbolTable table = createSymbolTable(
        "function random() { return 1; }" +
        " function Foo() {}" +
        " function Bar() {}" +
        "var x = random() ? new Foo() : new Bar();");

    Symbol x = getGlobalVar(table, "x");
    Symbol foo = getGlobalVar(table, "Foo");
    Symbol bar = getGlobalVar(table, "Bar");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    Symbol fn = getGlobalVar(table, "Function");
    Symbol obj = getGlobalVar(table, "Object");
    assertEquals(
        Lists.newArrayList(foo, bar), table.getAllSymbolsForTypeOf(x));
    assertEquals(
        Lists.newArrayList(fn), table.getAllSymbolsForTypeOf(foo));
    assertEquals(
        Lists.newArrayList(foo), table.getAllSymbolsForTypeOf(fooPrototype));
    assertEquals(
        foo,
        table.getSymbolDeclaredBy(
            foo.getType().toMaybeFunctionType()));
  }

// com.google.javascript.jscomp.SymbolTableTest::testStaticMethodReferences
  public void testStaticMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.method = function() {};" +
        "function f() { var x = DomHelper; x.method() + x.method(); }");

    Symbol method =
        getGlobalVar(table, "DomHelper").getPropertyScope().getSlot("method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodReferences
  public void testMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        " DomHelper.prototype.method = function() {};" +
        "function f() { " +
        "  (new DomHelper()).method(); (new DomHelper()).method(); };");

    Symbol method =
        getGlobalVar(table, "DomHelper.prototype.method");
    assertEquals(
        3, Iterables.size(table.getReferences(method)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testSuperClassMethodReferences
  public void testSuperClassMethodReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};" +
        "goog.inherits = function(a, b) {};" +
        " var A = function(){};" +
        " A.prototype.method = function() {};" +
        "\n" +
        "var B = function(){};\n" +
        "goog.inherits(B, A);" +
        " B.prototype.method = function() {" +
        "  B.superClass_.method();" +
        "};");

    Symbol methodA =
        getGlobalVar(table, "A.prototype.method");
    assertEquals(
        2, Iterables.size(table.getReferences(methodA)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testFieldReferences
  public void testFieldReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){" +
        "   this.field = 3;" +
        "};" +
        "function f() { " +
        "  return (new DomHelper()).field + (new DomHelper()).field; };");

    Symbol field = getGlobalVar(table, "DomHelper.prototype.field");
    assertEquals(
        3, Iterables.size(table.getReferences(field)));
  }

// com.google.javascript.jscomp.SymbolTableTest::testUndeclaredFieldReferences
  public void testUndeclaredFieldReferences() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        " var DomHelper = function(){};" +
        "DomHelper.prototype.method = function() { " +
        "  this.field = 3;" +
        "  return x.field;" +
        "}");

    Symbol field = getGlobalVar(table, "DomHelper.prototype.field");
    assertNull(field);
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences
  public void testPrototypeReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function DomHelper() {}" +
        "DomHelper.prototype.method = function() {};");
    Symbol prototype =
        getGlobalVar(table, "DomHelper.prototype");
    assertNotNull(prototype);

    List<Reference> refs = table.getReferenceList(prototype);

    
    assertEquals(refs.toString(), 2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences2
  public void testPrototypeReferences2() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n"
        + "function Snork() {}\n"
        + "Snork.prototype.baz = 3;\n");
    Symbol prototype =
        getGlobalVar(table, "Snork.prototype");
    assertNotNull(prototype);

    List<Reference> refs = table.getReferenceList(prototype);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences3
  public void testPrototypeReferences3() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {}");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = table.getReferenceList(fooPrototype);
    assertEquals(1, refs.size());
    assertEquals(Token.NAME, refs.get(0).getNode().getType());

    
    
    assertEquals(
        refs.get(0).getNode(),
        table.getReferenceList(getGlobalVar(table, "Foo")).get(0).getNode());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences4
  public void testPrototypeReferences4() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {}" +
        "Foo.prototype = {bar: 3}");
    Symbol fooPrototype = getGlobalVar(table, "Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = Lists.newArrayList(
        table.getReferences(fooPrototype));
    assertEquals(1, refs.size());
    assertEquals(Token.GETPROP, refs.get(0).getNode().getType());
    assertEquals("Foo.prototype", refs.get(0).getNode().getQualifiedName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testPrototypeReferences5
  public void testPrototypeReferences5() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {};  goog.Foo = function() {};");
    Symbol fooPrototype = getGlobalVar(table, "goog.Foo.prototype");
    assertNotNull(fooPrototype);

    List<Reference> refs = table.getReferenceList(fooPrototype);
    assertEquals(1, refs.size());
    assertEquals(Token.GETPROP, refs.get(0).getNode().getType());

    
    
    assertEquals(
        refs.get(0).getNode(),
        table.getReferenceList(
            getGlobalVar(table, "goog.Foo")).get(0).getNode());
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocType
  public void testReferencesInJSDocType() {
    SymbolTable table = createSymbolTable(
        " function Foo() {}\n" +
        " var x;\n" +
        " function f(x) {}\n" +
        " function g() {}\n" +
        " function Sub() {}");
    Symbol foo = getGlobalVar(table, "Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(5, refs.size());

    assertEquals(1, refs.get(0).getNode().getLineno());
    assertEquals(29, refs.get(0).getNode().getCharno());
    assertEquals(3, refs.get(0).getNode().getLength());

    assertEquals(2, refs.get(1).getNode().getLineno());
    assertEquals(11, refs.get(1).getNode().getCharno());

    assertEquals(3, refs.get(2).getNode().getLineno());
    assertEquals(12, refs.get(2).getNode().getCharno());

    assertEquals(4, refs.get(3).getNode().getLineno());
    assertEquals(25, refs.get(3).getNode().getCharno());

    assertEquals(7, refs.get(4).getNode().getLineno());
    assertEquals(13, refs.get(4).getNode().getCharno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocType2
  public void testReferencesInJSDocType2() {
    SymbolTable table = createSymbolTable(
        " function f(x) {}\n");
    Symbol str = getGlobalVar(table, "String");
    assertNotNull(str);

    List<Reference> refs = table.getReferenceList(str);

    
    
    
    
    assertTrue(refs.size() > 1);

    int last = refs.size() - 1;
    for (int i = 0; i < refs.size(); i++) {
      Reference ref = refs.get(i);
      assertEquals(i != last, ref.getNode().isFromExterns());
      if (!ref.getNode().isFromExterns()) {
        assertEquals("in1", ref.getNode().getSourceFileName());
      }
    }
  }

// com.google.javascript.jscomp.SymbolTableTest::testReferencesInJSDocName
  public void testReferencesInJSDocName() {
    String code = " function f(x) {}\n";
    SymbolTable table = createSymbolTable(code);
    Symbol x = getLocalVar(table, "x");
    assertNotNull(x);

    List<Reference> refs = table.getReferenceList(x);
    assertEquals(2, refs.size());

    assertEquals(code.indexOf("x) {"), refs.get(0).getNode().getCharno());
    assertEquals(code.indexOf("x */"), refs.get(1).getNode().getCharno());
    assertEquals("in1",
        refs.get(0).getNode().getSourceFileName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalQualifiedNamesInLocalScopes
  public void testLocalQualifiedNamesInLocalScopes() {
    SymbolTable table = createSymbolTable(
        "function f() { var x = {}; x.number = 3; }");
    Symbol xNumber = getLocalVar(table, "x.number");
    assertNotNull(xNumber);
    assertFalse(table.getScope(xNumber).isGlobalScope());

    assertEquals("number", xNumber.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNaturalSymbolOrdering
  public void testNaturalSymbolOrdering() {
    SymbolTable table = createSymbolTable(
        " var a = {};" +
        " a.b = {};" +
        " function f(x) {}");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol f = getGlobalVar(table, "f");
    Symbol x = getLocalVar(table, "x");
    Ordering<Symbol> ordering = table.getNaturalSymbolOrdering();
    assertSymmetricOrdering(ordering, a, ab);
    assertSymmetricOrdering(ordering, a, f);
    assertSymmetricOrdering(ordering, f, ab);
    assertSymmetricOrdering(ordering, f, x);
  }

// com.google.javascript.jscomp.SymbolTableTest::testDeclarationDisagreement
  public void testDeclarationDisagreement() {
    SymbolTable table = createSymbolTable(
        " var goog = goog || {};\n" +
        "\n" +
        "goog.addSingletonGetter2 = function(x) {};\n" +
        "\n" +
        "goog.addSingletonGetter = goog.addSingletonGetter2;\n" +
        "\n" +
        "goog.addSingletonGetter = function(x) {};\n");

    Symbol method = getGlobalVar(table, "goog.addSingletonGetter");
    List<Reference> refs = table.getReferenceList(method);
    assertEquals(2, refs.size());

    
    assertEquals(7, method.getDeclaration().getNode().getLineno());
    assertEquals(5, refs.get(1).getNode().getLineno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMultipleExtends
  public void testMultipleExtends() {
    SymbolTable table = createSymbolTable(
        " var goog = goog || {};\n" +
        "goog.inherits = function(x, y) {};\n" +
        "\n" +
        "goog.A = function() { this.fieldA = this.constructor; };\n" +
        " goog.A.FooA = function() {};\n" +
        " goog.A.prototype.methodA = function() {};\n" +
        "\n" +
        "goog.B = function() { this.fieldB = this.constructor; };\n" +
        "goog.inherits(goog.B, goog.A);\n" +
        " goog.B.prototype.methodB = function() {};\n" +
        "\n" +
        "goog.B2 = function() { this.fieldB = this.constructor; };\n" +
        "goog.inherits(goog.B2, goog.A);\n" +
        " goog.B2.FooB = function() {};\n" +
        " goog.B2.prototype.methodB = function() {};\n" +
        "\n" +
        "goog.C = function() { this.fieldC = this.constructor; };\n" +
        "goog.inherits(goog.C, goog.B);\n" +
        " goog.C.FooC = function() {};\n" +
        " goog.C.prototype.methodC = function() {};\n");

    Symbol bCtor = getGlobalVar(table, "goog.B.prototype.constructor");
    assertNotNull(bCtor);

    List<Reference> bRefs = table.getReferenceList(bCtor);
    assertEquals(2, bRefs.size());
    assertEquals(11, bCtor.getDeclaration().getNode().getLineno());

    Symbol cCtor = getGlobalVar(table, "goog.C.prototype.constructor");
    assertNotNull(cCtor);

    List<Reference> cRefs = table.getReferenceList(cCtor);
    assertEquals(2, cRefs.size());
    assertEquals(26, cCtor.getDeclaration().getNode().getLineno());
  }

// com.google.javascript.jscomp.SymbolTableTest::testJSDocAssociationWithBadNamespace
  public void testJSDocAssociationWithBadNamespace() {
    SymbolTable table = createSymbolTable(
        
        
        
        " goog.Foo = function(){};");

    Symbol foo = getGlobalVar(table, "goog.Foo");
    assertNotNull(foo);

    JSDocInfo info = foo.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isConstructor());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMissingConstructorTag
  public void testMissingConstructorTag() {
    SymbolTable table = createSymbolTable(
        "function F() {" +
        "  this.field1 = 3;" +
        "}" +
        "F.prototype.method1 = function() {" +
        "  this.field1 = 5;" +
        "};" +
        "(new F()).method1();");

    
    
    assertNull(getGlobalVar(table, "F.prototype.field1"));

    Symbol sym = getGlobalVar(table, "F.prototype.method1");
    assertEquals(1, table.getReferenceList(sym).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testTypeCheckingOff
  public void testTypeCheckingOff() {
    options = new CompilerOptions();

    
    SymbolTable table = createSymbolTable(
        "" +
        "function F() {" +
        "  this.field1 = 3;" +
        "}" +
        "F.prototype.method1 = function() {" +
        "  this.field1 = 5;" +
        "};" +
        "(new F()).method1();");
    assertNull(getGlobalVar(table, "F.prototype.field1"));
    assertNull(getGlobalVar(table, "F.prototype.method1"));

    Symbol sym = getGlobalVar(table, "F");
    assertEquals(3, table.getReferenceList(sym).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testSuperClassReference
  public void testSuperClassReference() throws Exception {
    SymbolTable table = createSymbolTable(
        "  var a = {b: {}};\n"
        + "\n"
        + "a.b.BaseClass = function() {};\n"
        + "a.b.BaseClass.prototype.doSomething = function() {\n"
        + "  alert('hi');\n"
        + "};\n"
        + "\n"
        + "a.b.DerivedClass = function() {};\n"
        + "goog.inherits(a.b.DerivedClass, a.b.BaseClass);\n"
        + "\n"
        + "a.b.DerivedClass.prototype.doSomething = function() {\n"
        + "  a.b.DerivedClass.superClass_.doSomething();\n"
        + "};\n");

    Symbol bad = getGlobalVar(
        table, "a.b.DerivedClass.superClass_.doSomething");
    assertNull(bad);

    Symbol good = getGlobalVar(
        table, "a.b.BaseClass.prototype.doSomething");
    assertNotNull(good);

    List<Reference> refs = table.getReferenceList(good);
    assertEquals(2, refs.size());
    assertEquals("a.b.DerivedClass.superClass_.doSomething",
        refs.get(1).getNode().getQualifiedName());
  }

// com.google.javascript.jscomp.SymbolTableTest::testInnerEnum
  public void testInnerEnum() throws Exception {
    SymbolTable table = createSymbolTable(
        "var goog = {}; goog.ui = {};"
        + "  \n"
        + "goog.ui.Zippy = function() {};\n"
        + "\n"
        + "goog.ui.Zippy.EventType = { TOGGLE: 'toggle' };\n");

    Symbol eventType = getGlobalVar(table, "goog.ui.Zippy.EventType");
    assertNotNull(eventType);
    assertTrue(eventType.getType().isEnumType());

    Symbol toggle = getGlobalVar(table, "goog.ui.Zippy.EventType.TOGGLE");
    assertNotNull(toggle);
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodInAnonObject1
  public void testMethodInAnonObject1() throws Exception {
    SymbolTable table = createSymbolTable(
        "var a = {}; a.b = {}; a.b.c = function() {};");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol abc = getGlobalVar(table, "a.b.c");

    assertNotNull(abc);
    assertEquals(1, table.getReferenceList(abc).size());

    assertEquals("{b: {c: function (): undefined}}", a.getType().toString());
    assertEquals("{c: function (): undefined}", ab.getType().toString());
    assertEquals("function (): undefined", abc.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testMethodInAnonObject2
  public void testMethodInAnonObject2() throws Exception {
    SymbolTable table = createSymbolTable(
        "var a = {b: {c: function() {}}};");
    Symbol a = getGlobalVar(table, "a");
    Symbol ab = getGlobalVar(table, "a.b");
    Symbol abc = getGlobalVar(table, "a.b.c");

    assertNotNull(abc);
    assertEquals(1, table.getReferenceList(abc).size());

    assertEquals("{b: {c: function (): undefined}}", a.getType().toString());
    assertEquals("{c: function (): undefined}", ab.getType().toString());
    assertEquals("function (): undefined", abc.getType().toString());
  }

// com.google.javascript.jscomp.SymbolTableTest::testJSDocOnlySymbol
  public void testJSDocOnlySymbol() throws Exception {
    SymbolTable table = createSymbolTable(
        "\n"
        + "var a;");
    Symbol x = getDocVar(table, "x");
    assertNotNull(x);
    assertEquals("number", x.getType().toString());
    assertEquals(1, table.getReferenceList(x).size());

    Symbol y = getDocVar(table, "y");
    assertNotNull(x);
    assertEquals(null, y.getType());
    assertEquals(1, table.getReferenceList(y).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testNamespaceDefinitionOrder
  public void testNamespaceDefinitionOrder() throws Exception {
    
    
    SymbolTable table = createSymbolTable(
        " var goog = {};\n"
        + " goog.dom.Foo = function() {};\n"
        + " goog.dom = {};\n");

    Symbol goog = getGlobalVar(table, "goog");
    Symbol dom = getGlobalVar(table, "goog.dom");
    Symbol Foo = getGlobalVar(table, "goog.dom.Foo");

    assertNotNull(goog);
    assertNotNull(dom);
    assertNotNull(Foo);

    assertEquals(dom, goog.getPropertyScope().getSlot("dom"));
    assertEquals(Foo, dom.getPropertyScope().getSlot("Foo"));
  }

// com.google.javascript.jscomp.SymbolTableTest::testSymbolForScopeOfNatives
  public void testSymbolForScopeOfNatives() throws Exception {
    SymbolTable table = createSymbolTable("");

    
    Symbol sliceArg = getLocalVar(table, "sliceArg");
    assertNotNull(sliceArg);

    Symbol scope = table.getSymbolForScope(table.getScope(sliceArg));
    assertNotNull(scope);
    assertEquals(scope, getGlobalVar(table, "String.prototype.slice"));

    Symbol proto = getGlobalVar(table, "String.prototype");
    assertEquals(
        "externs1", proto.getDeclaration().getNode().getSourceFileName());
  }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }
