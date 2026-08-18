// buggy code
  private void processRequireCall(NodeTraversal t, Node n, Node parent) {
    Node left = n.getFirstChild();
    Node arg = left.getNext();
    if (verifyLastArgumentIsString(t, left, arg)) {
      String ns = arg.getString();
      ProvidedName provided = providedNames.get(ns);
      if (provided == null || !provided.isExplicitlyProvided()) {
        unrecognizedRequires.add(
            new UnrecognizedRequire(n, ns, t.getSourceName()));
      } else {
        JSModule providedModule = provided.explicitModule;

        // This must be non-null, because there was an explicit provide.
        Preconditions.checkNotNull(providedModule);

        JSModule module = t.getModule();
        if (moduleGraph != null &&
            module != providedModule &&
            !moduleGraph.dependsOn(module, providedModule)) {
          compiler.report(
              t.makeError(n, XMODULE_REQUIRE_ERROR, ns,
                  providedModule.getName(),
                  module.getName()));
        }
      }

      maybeAddToSymbolTable(left);
      maybeAddStringNodeToSymbolTable(arg);

      // Requires should be removed before further processing.
      // Some clients run closure pass multiple times, first with
      // the checks for broken requires turned off. In these cases, we
      // allow broken requires to be preserved by the first run to
      // let them be caught in the subsequent run.
      if (provided != null) {
        parent.detachFromParent();
        compiler.reportCodeChange();
      }
    }
  }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testIssue537b
  public void testIssue537b() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype = {method: function() {}};" +
        "\n" +
        "function Bar() {" +
        "  Foo.call(this);" +
        "  if (this.baz(1)) this.method();" +
        "}" +
        "Bar.prototype = {" +
        "  baz: function() {" +
        "    return true;" +
        "  }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;",
        "Function Bar.prototype.baz: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue537c
  public void testIssue537c() throws Exception {
    testTypes(
        " function Foo() {}" +
        "\n" +
        "function Bar() {" +
        "  Foo.call(this);" +
        "  if (this.baz2()) alert(1);" +
        "}" +
        "Bar.prototype = {" +
        "  baz: function() {" +
        "    return true;" +
        "  }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;",
        "Property baz2 never defined on Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue537d
  public void testIssue537d() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo.prototype = {" +
        "   x: function() { new Bar(); }," +
        "   y: function() { new Bar(); }" +
        "};" +
        "\n" +
        "function Bar() {" +
        "  this.xy = 3;" +
        "}" +
        " function f() { return new Bar(); }" +
        " function g() { return new Bar(); }" +
        "Bar.prototype = {" +
        "   x: function() { new Bar(); }," +
        "   y: function() { new Bar(); }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue586
  public void testIssue586() throws Exception {
    testTypes(
        "" +
        "var MyClass = function() {};" +
        "" +
        "MyClass.prototype.fn = function(success) {};" +
        "MyClass.prototype.test = function() {" +
        "  this.fn();" +
        "  this.fn = function() {};" +
        "};",
        "Function MyClass.prototype.fn: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue635
  public void testIssue635() throws Exception {
    
    testTypes(
        "" +
        "function F() {}" +
        "F.prototype.bar = function() { this.baz(); };" +
        "F.prototype.baz = function() {};" +
        "" +
        "function G() {}" +
        "G.prototype.bar = F.prototype.bar;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue635b
  public void testIssue635b() throws Exception {
    testTypes(
        "" +
        "function F() {}" +
        "" +
        "function G() {}" +
        " var x = F;",
        "initializing variable\n" +
        "found   : function (new:F): undefined\n" +
        "required: function (new:G): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue669
  public void testIssue669() throws Exception {
    testTypes(
        "" +
         "function f(a) {" +
         "  var results;" +
         "  if (a) {" +
         "    results = {};" +
         "    results.prop1 = {a: 3};" +
         "  } else {" +
         "    results = {prop2: 3};" +
         "  }" +
         "  return results;" +
         "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue688
  public void testIssue688() throws Exception {
    testTypes(
        " var SOME_DEFAULT =\n" +
        "     ({first: 1, second: 2});\n" +
        "\n" +
        "function TwoNumbers() {}\n" +
        "\n" +
        "TwoNumbers.prototype.first;\n" +
        "\n" +
        "TwoNumbers.prototype.second;\n" +
        " function f() { return SOME_DEFAULT; }",
        "inconsistent return type\n" +
        "found   : (TwoNumbers|null)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue700
  public void testIssue700() throws Exception {
    testTypes(
        "\n" +
        "function temp1(opt_data) {\n" +
        "  return opt_data.text;\n" +
        "}\n" +
        "\n" +
        "\n" +
        "function temp2(opt_data) {\n" +
        "  \n" +
        "  function __inner() {\n" +
        "    return temp1(opt_data.activity);\n" +
        "  }\n" +
        "  return __inner();\n" +
        "}\n" +
        "\n" +
        "\n" +
        "function temp3(opt_data) {\n" +
        "  return 'n: ' + opt_data.n + ', t: ' + opt_data.text + '.';\n" +
        "}\n" +
        "\n" +
        "function callee() {\n" +
        "  var output = temp3({\n" +
        "    n: 0,\n" +
        "    text: 'a string',\n" +
        "    b: true\n" +
        "  })\n" +
        "  alert(output);\n" +
        "}\n" +
        "\n" +
        "callee();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue725
  public void testIssue725() throws Exception {
    testTypes(
        " var RecordType1;" +
        " var RecordType2;" +
        " function f(rec) {" +
        "  alert(rec.name2222);" +
        "}",
        "Property name2222 never defined on rec");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue726
  public void testIssue726() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = function(x) {};" +
        " " +
        "Foo.prototype.getDeferredBar = function() { " +
        "  var self = this;" +
        "  return function() {" +
        "    self.bar(true);" +
        "  };" +
        "};",
        "actual parameter 1 of Foo.prototype.bar does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue765
  public void testIssue765() throws Exception {
    testTypes(
        "" +
        "var AnotherType = function (parent) {" +
        "    " +
        "    this.doSomething = function (stringParameter) {};" +
        "};" +
        "" +
        "var YetAnotherType = function () {" +
        "    this.field = new AnotherType(self);" +
        "    this.testfun=function(stringdata) {" +
        "        this.field.doSomething(null);" +
        "    };" +
        "};",
        "actual parameter 1 of AnotherType.doSomething " +
        "does not match formal parameter\n" +
        "found   : null\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue783
  public void testIssue783() throws Exception {
    testTypes(
        "" +
        "var Type = function () {" +
        "  " +
        "  this.me_ = this;" +
        "};" +
        "Type.prototype.doIt = function() {" +
        "  var me = this.me_;" +
        "  for (var i = 0; i < me.unknownProp; i++) {}" +
        "};",
        "Property unknownProp never defined on Type");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue791
  public void testIssue791() throws Exception {
    testTypes(
        "" +
        "function test1(obj) {}" +
        "var fnStruc1 = {};" +
        "fnStruc1.func = function() {};" +
        "test1(fnStruc1);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue810
  public void testIssue810() throws Exception {
    testTypes(
        "" +
        "var Type = function () {" +
        "};" +
        "Type.prototype.doIt = function(obj) {" +
        "  this.prop = obj.unknownProp;" +
        "};",
        "Property unknownProp never defined on obj");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1002
  public void testIssue1002() throws Exception {
    testTypes(
        "" +
        "var I = function() {};" +
        "" +
        "var A = function() {};" +
        "" +
        "var B = function() {};" +
        "var f = function() {" +
        "  if (A === B) {" +
        "    new B();" +
        "  }" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1023
  public void testIssue1023() throws Exception {
    testTypes(
        "" +
        "function F() {}" +
        "(function () {" +
        "  F.prototype = {" +
        "    " +
        "    bar: function (x) {  }" +
        "  };" +
        "})();" +
        "(new F()).bar(true)",
        "actual parameter 1 of F.prototype.bar does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1047
  public void testIssue1047() throws Exception {
    testTypes(
        "\n" +
        "function C2() {}\n" +
        "\n" +
        "\n" +
        "function C3(c2) {\n" +
        "  \n" +
        "  this.c2_;\n" +
        "\n" +
        "  var x = this.c2_.prop;\n" +
        "}",
        "Property prop never defined on C2");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIssue1072
  public void testIssue1072() throws Exception {
    testTypes(
        "\n" +
        "var f1 = function (x) {\n" +
        "  return 3;\n" +
        "};\n" +
        "\n" +
        "\n" +
        "var f2 = function (x) {\n" +
        "  if (!x) throw new Error()\n" +
        "  return  (f1('x'))\n" +
        "}\n" +
        "\n" +
        "\n" +
        "var f3 = function (x) {};\n" +
        "\n" +
        "f1(f3);",
        "actual parameter 1 of f1 does not match formal parameter\n" +
        "found   : function (string): undefined\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnums
  public void testEnums() throws Exception {
    testTypes(
        "var outer = function() {" +
        "  " +
        "  var Level = {" +
        "    NONE: 0," +
        "  };" +
        "  " +
        "  var l = Level.NONE;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug592170
  public void testBug592170() throws Exception {
    testTypes(
        "" +
        "function foo(opt_f) {" +
        "  " +
        "  return opt_f || function () {};" +
        "}",
        "Type annotations are not allowed here. Are you missing parentheses?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug901455
  public void testBug901455() throws Exception {
    testTypes(" function a() { return 3; }" +
        "var b = undefined === a()");
    testTypes(" function a() { return 3; }" +
        "var b = a() === undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug908701
  public void testBug908701() throws Exception {
    testTypes("var s = new String('foo');" +
        "var b = s.match(/a/) != null;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug908625
  public void testBug908625() throws Exception {
    testTypes("function A(){}" +
        "function B(){}" +
        "function foo(b){return b}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug911118
  public void testBug911118() throws Exception {
    
    Scope s = parseAndTypeCheckWithScope("var a = function(){};").scope;
    JSType type = s.getVar("a").getType();
    assertEquals("function (): undefined", type.toString());

    
    testTypes("function nullFunction() {};" +
        "var foo = nullFunction;" +
        "foo = function() {};" +
        "foo();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug909000
  public void testBug909000() throws Exception {
    testTypes("function A(){}\n" +
        "\n" +
        "function y(a) { return a }",
        "inconsistent return type\n" +
        "found   : A\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug930117
  public void testBug930117() throws Exception {
    testTypes(
        "function f(x){}" +
        "f(null);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : null\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1484445
  public void testBug1484445() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = null;" +
        " Foo.prototype.baz = null;" +
        "" +
        "function f(foo) {" +
        "  while (true) {" +
        "    if (foo.bar == null && foo.baz == null) {" +
        "      foo.bar;" +
        "    }" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1859535
  public void testBug1859535() throws Exception {
    testTypes(
        "" +
        "var inherits = function(childCtor, parentCtor) {" +
        "  " +
        "  function tempCtor() {};" +
        "  tempCtor.prototype = parentCtor.prototype;" +
        "  childCtor.superClass_ = parentCtor.prototype;" +
        "  childCtor.prototype = new tempCtor();" +
        "   childCtor.prototype.constructor = childCtor;" +
        "};" +
        "" +
        "var factory = function(constructor, var_args) {" +
        "  " +
        "  var tempCtor = function() {};" +
        "  tempCtor.prototype = constructor.prototype;" +
        "  var obj = new tempCtor();" +
        "  constructor.apply(obj, arguments);" +
        "  return obj;" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1940591
  public void testBug1940591() throws Exception {
    testTypes(
        "" +
        "var a = {};\n" +
        "\n" +
        "a.name = 0;\n" +
        "\n" +
        "a.g = function(x) { x.name = 'a'; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1942972
  public void testBug1942972() throws Exception {
    testTypes(
        "var google = {\n" +
        "  gears: {\n" +
        "    factory: {},\n" +
        "    workerPool: {}\n" +
        "  }\n" +
        "};\n" +
        "\n" +
        "google.gears = {factory: {}};\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1943776
  public void testBug1943776() throws Exception {
    testTypes(
        "" +
        "function bar() {" +
        "  return {foo: []};" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1987544
  public void testBug1987544() throws Exception {
    testTypes(
        " function foo(x) {}" +
        "var duration;" +
        "if (true && !(duration = 3)) {" +
        " foo(duration);" +
        "}",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug1940769
  public void testBug1940769() throws Exception {
    testTypes(
        " " +
        "function proto(obj) { return obj.prototype; }" +
        " function Map() {}" +
        "" +
        "function Map2() { Map.call(this); };" +
        "Map2.prototype = proto(Map);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug2335992
  public void testBug2335992() throws Exception {
    testTypes(
        " function f() { return 3; }" +
        "var x = f();" +
        "" +
        "x.y = 3;",
        "assignment\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug2341812
  public void testBug2341812() throws Exception {
    testTypes(
        "" +
        "function EventTarget() {}" +
        "" +
        "function Node() {}" +
        " Node.prototype.index;" +
        "" +
        "function foo(x) { return x.index; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug7701884
  public void testBug7701884() throws Exception {
    testTypes(
        "\n" +
        "var forEach = function(x, y) {\n" +
        "  for (var i = 0; i < x.length; i++) y(x[i]);\n" +
        "};" +
        "" +
        "function f(x) {}" +
        "" +
        "function h(x) {" +
        "  var top = null;" +
        "  forEach(x, function(z) { top = z; });" +
        "  if (top) f(top);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBug8017789
  public void testBug8017789() throws Exception {
    testTypes(
        "" +
        "var f = function(isResult) {" +
        "    while (true)" +
        "        isResult['t'];" +
        "};" +
        "" +
        "var map;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypedefBeforeUse
  public void testTypedefBeforeUse() throws Exception {
    testTypes(
        "" +
        "var map;" +
        "" +
        "var f = function(isResult) {" +
        "    while (true)" +
        "        isResult['t'];" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScopedConstructors1
  public void testScopedConstructors1() throws Exception {
    testTypes(
        "function foo1() { " +
        "   function Bar() { " +
        "     this.x = 3;" +
        "  }" +
        "}" +
        "function foo2() { " +
        "   function Bar() { " +
        "     this.x = 'y';" +
        "  }" +
        "  " +
        "  function baz(b) { return b.x; }" +
        "}",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScopedConstructors2
  public void testScopedConstructors2() throws Exception {
    testTypes(
        "" +
        "function foo1(f) {" +
        "  " +
        "  f.prototype.bar = function(g) {};" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference1
  public void testQualifiedNameInference1() throws Exception {
    testTypes(
        " function Foo() {}" +
        " Foo.prototype.bar = null;" +
        " Foo.prototype.baz = null;" +
        "" +
        "function f(foo) {" +
        "  while (true) {" +
        "    if (!foo.baz) break; " +
        "    foo.bar = null;" +
        "  }" +
        
        "  return foo.bar == null;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference2
  public void testQualifiedNameInference2() throws Exception {
    testTypes(
        "var x = {};" +
        "x.y = c;" +
        "function f(a, b) {" +
        "  if (a) {" +
        "    if (b) " +
        "      x.y = 2;" +
        "    else " +
        "      x.y = 1;" +
        "  }" +
        "  return x.y == null;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference3
  public void testQualifiedNameInference3() throws Exception {
    testTypes(
        "var x = {};" +
        "x.y = c;" +
        "function f(a, b) {" +
        "  if (a) {" +
        "    if (b) " +
        "      x.y = 2;" +
        "    else " +
        "      x.y = 1;" +
        "  }" +
        "  return x.y == null;" +
        "} function g() { x.y = null; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference4
  public void testQualifiedNameInference4() throws Exception {
    testTypes(
        " function f(x) {}\n" +
        "" +
        "function Foo(x) { this.x_ = x; }\n" +
        "Foo.prototype.bar = function() {" +
        "  if (this.x_) { f(this.x_); }" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference5
  public void testQualifiedNameInference5() throws Exception {
    testTypes(
        "var ns = {}; " +
        "(function() { " +
        "     ns.foo = function(x) {}; })();" +
        "(function() { ns.foo(true); })();",
        "actual parameter 1 of ns.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference6
  public void testQualifiedNameInference6() throws Exception {
    testTypes(
        " var ns = {}; " +
        " ns.foo = function(x) {};" +
        "(function() { " +
        "    ns.foo = function(x) {};" +
        "    ns.foo(true); " +
        "})();",
        "actual parameter 1 of ns.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference7
  public void testQualifiedNameInference7() throws Exception {
    testTypes(
        "var ns = {}; " +
        "(function() { " +
        "   " +
        "  ns.Foo = function(x) {};" +
        "   function f(x) {}" +
        "  f(new ns.Foo(true));" +
        "})();",
        "actual parameter 1 of ns.Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference8
  public void testQualifiedNameInference8() throws Exception {
    testClosureTypesMultipleWarnings(
        "var ns = {}; " +
        "(function() { " +
        "   " +
        "  ns.Foo = function(x) {};" +
        "})();" +
        " function f(x) {}" +
        "f(new ns.Foo(true));",
        Lists.newArrayList(
            "actual parameter 1 of ns.Foo does not match formal parameter\n" +
            "found   : boolean\n" +
            "required: number"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference9
  public void testQualifiedNameInference9() throws Exception {
    testTypes(
        "var ns = {}; " +
        "ns.ns2 = {}; " +
        "(function() { " +
        "   " +
        "  ns.ns2.Foo = function(x) {};" +
        "   function f(x) {}" +
        "  f(new ns.ns2.Foo(true));" +
        "})();",
        "actual parameter 1 of ns.ns2.Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference10
  public void testQualifiedNameInference10() throws Exception {
    testTypes(
        "var ns = {}; " +
        "ns.ns2 = {}; " +
        "(function() { " +
        "   " +
        "  ns.ns2.Foo = function() {};" +
        "   " +
        "  function F() {}" +
        "  (new F());" +
        "})();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference11
  public void testQualifiedNameInference11() throws Exception {
    testTypes(
        " function Foo() {}" +
        "function f() {" +
        "  var x = new Foo();" +
        "  x.onload = function() {" +
        "    x.onload = null;" +
        "  };" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference12
  public void testQualifiedNameInference12() throws Exception {
    
    
    testTypes(
        " function f(x) {}" +
        " function Foo() {" +
        "   this.bar = 3;" +
        "  f(function() { this.bar = true; });" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testQualifiedNameInference13
  public void testQualifiedNameInference13() throws Exception {
    testTypes(
        " function Foo() {}" +
        "function f(z) {" +
        "  var x = new Foo();" +
        "  if (z) {" +
        "    x.onload = function() {};" +
        "  } else {" +
        "    x.onload = null;" +
        "  };" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSheqRefinedScope
  public void testSheqRefinedScope() throws Exception {
    Node n = parseAndTypeCheck(
        "function A() {}\n" +
        " function B() {}\n" +
        "\n" +
        "B.prototype.p = function() { return 1; }\n" +
        "\n" +
        "function f(a, b) {\n" +
        "  b.p();\n" +
        "  if (a === b) {\n" +
        "    b.p();\n" +
        "  }\n" +
        "}");
    Node nodeC = n.getLastChild().getLastChild().getLastChild().getLastChild()
        .getLastChild().getLastChild();
    JSType typeC = nodeC.getJSType();
    assertTrue(typeC.isNumber());

    Node nodeB = nodeC.getFirstChild().getFirstChild();
    JSType typeB = nodeB.getJSType();
    assertEquals("B", typeB.toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssignToUntypedVariable
  public void testAssignToUntypedVariable() throws Exception {
    Node n = parseAndTypeCheck("var z; z = 1;");

    Node assign = n.getLastChild().getFirstChild();
    Node node = assign.getFirstChild();
    assertFalse(node.getJSType().isUnknownType());
    assertEquals("number", node.getJSType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testAssignToUntypedProperty
  public void testAssignToUntypedProperty() throws Exception {
    Node n = parseAndTypeCheck(
        " function Foo() {}\n" +
        "Foo.prototype.a = 1;" +
        "(new Foo).a;");

    Node node = n.getLastChild().getFirstChild();
    assertFalse(node.getJSType().isUnknownType());
    assertTrue(node.getJSType().isNumber());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew1
  public void testNew1() throws Exception {
    testTypes("new 4", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew2
  public void testNew2() throws Exception {
    testTypes("var Math = {}; new Math()", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew3
  public void testNew3() throws Exception {
    testTypes("new Date()");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew4
  public void testNew4() throws Exception {
    testTypes("function A(){}; new A();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew5
  public void testNew5() throws Exception {
    testTypes("function A(){}; new A();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew6
  public void testNew6() throws Exception {
    TypeCheckResult p =
      parseAndTypeCheckWithScope("function A(){};" +
      "var a = new A();");

    JSType aType = p.scope.getVar("a").getType();
    assertTrue(aType instanceof ObjectType);
    ObjectType aObjectType = (ObjectType) aType;
    assertEquals("A", aObjectType.getConstructor().getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew7
  public void testNew7() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "if (opt_constructor) { new opt_constructor; }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew8
  public void testNew8() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new opt_constructor;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew9
  public void testNew9() throws Exception {
    testTypes("" +
        "function foo(opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew10
  public void testNew10() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.Foo = function (opt_constructor) {" +
        "new (opt_constructor || Array);" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew11
  public void testNew11() throws Exception {
    testTypes("" +
        "function f(c1) {" +
        "  var c2 = function(){};" +
        "  c1.prototype = new c2;" +
        "}", TypeCheck.NOT_A_CONSTRUCTOR);
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew12
  public void testNew12() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = new Array();");
    Var a = p.scope.getVar("a");

    assertTypeEquals(ARRAY_TYPE, a.getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew13
  public void testNew13() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "function FooBar(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew14
  public void testNew14() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var FooBar = function(){};" +
        "var a = new FooBar();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("FooBar", a.getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew15
  public void testNew15() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function(){};" +
        "var a = new goog.A();");
    Var a = p.scope.getVar("a");

    assertTrue(a.getType() instanceof ObjectType);
    assertEquals("goog.A", a.getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew16
  public void testNew16() throws Exception {
    testTypes(
        "" +
        "function Foo(x) {}" +
        "function g() { new Foo(1); }",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew17
  public void testNew17() throws Exception {
    testTypes("var goog = {}; goog.x = 3; new goog.x",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNew18
  public void testNew18() throws Exception {
    testTypes("var goog = {};" +
              " goog.F = function() {};" +
              " goog.G = goog.F;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testName1
  public void testName1() throws Exception {
    assertTypeEquals(VOID_TYPE, testNameNode("undefined"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName2
  public void testName2() throws Exception {
    assertTypeEquals(OBJECT_FUNCTION_TYPE, testNameNode("Object"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName3
  public void testName3() throws Exception {
    assertTypeEquals(ARRAY_FUNCTION_TYPE, testNameNode("Array"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName4
  public void testName4() throws Exception {
    assertTypeEquals(DATE_FUNCTION_TYPE, testNameNode("Date"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testName5
  public void testName5() throws Exception {
    assertTypeEquals(REGEXP_FUNCTION_TYPE, testNameNode("RegExp"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation1
  public void testBitOperation1() throws Exception {
    testTypes("function foo(){ ~foo(); }",
        "operator ~ cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation2
  public void testBitOperation2() throws Exception {
    testTypes("function foo(){var a = foo()<<3;}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation3
  public void testBitOperation3() throws Exception {
    testTypes("function foo(){var a = 3<<foo();}",
        "operator << cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation4
  public void testBitOperation4() throws Exception {
    testTypes("function foo(){var a = foo()>>>3;}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation5
  public void testBitOperation5() throws Exception {
    testTypes("function foo(){var a = 3>>>foo();}",
        "operator >>> cannot be applied to undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation6
  public void testBitOperation6() throws Exception {
    testTypes("function foo(){var a = foo()&3;}",
        "bad left operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation7
  public void testBitOperation7() throws Exception {
    testTypes("var x = null; x |= undefined; x &= 3; x ^= '3'; x |= true;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation8
  public void testBitOperation8() throws Exception {
    testTypes("var x = void 0; x |= new Number(3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBitOperation9
  public void testBitOperation9() throws Exception {
    testTypes("var x = void 0; x |= {};",
        "bad right operand to bitwise operator\n" +
        "found   : {}\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall1
  public void testCall1() throws Exception {
    testTypes("3();", "number expressions are not callable");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall2
  public void testCall2() throws Exception {
    testTypes("function bar(foo){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall3
  public void testCall3() throws Exception {
    
    
    testTypes("var opt_f;" +
        "var f1;" +
        "var f2 = opt_f || f1;" +
        "f2();",
        "Bad type annotation. Unknown type some.unknown.type");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall4
  public void testCall4() throws Exception {
    testTypes("var foo = function bar(a){ bar('abc'); }",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall5
  public void testCall5() throws Exception {
    testTypes("var foo = function bar(a){ foo('abc'); }",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall6
  public void testCall6() throws Exception {
    testTypes("function bar(foo){}" +
        "bar('abc');",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall7
  public void testCall7() throws Exception {
    testTypes("var foo = function bar(a){};" +
        "foo('abc');",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: RegExp");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall8
  public void testCall8() throws Exception {
    testTypes("var f;f();",
        "(Function|number) expressions are " +
        "not callable");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall9
  public void testCall9() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        " var bar = function(a){};" +
        "bar('abc');",
        "actual parameter 1 of bar does not match formal parameter\n" +
        "found   : string\n" +
        "required: goog.Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall10
  public void testCall10() throws Exception {
    testTypes("var f;f();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCall11
  public void testCall11() throws Exception {
    testTypes("var f = new Function(); f();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall1
  public void testFunctionCall1() throws Exception {
    testTypes(
        " var foo = function(x) {};" +
        "foo.call(null, 3);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall2
  public void testFunctionCall2() throws Exception {
    testTypes(
        " var foo = function(x) {};" +
        "foo.call(null, 'bar');",
        "actual parameter 2 of foo.call does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall3
  public void testFunctionCall3() throws Exception {
    testTypes(
        " " +
        "var Foo = function(x) { this.bar.call(null, x); };" +
        " Foo.prototype.bar;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall4
  public void testFunctionCall4() throws Exception {
    testTypes(
        " " +
        "var Foo = function(x) { this.bar.call(null, x); };" +
        " Foo.prototype.bar;",
        "actual parameter 2 of this.bar.call " +
        "does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall5
  public void testFunctionCall5() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler) { handler.call(this, x); };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall6
  public void testFunctionCall6() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler) { handler.apply(this, x); };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall7
  public void testFunctionCall7() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler, opt_context) { " +
        "  handler.call(opt_context, x);" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall8
  public void testFunctionCall8() throws Exception {
    testTypes(
        " " +
        "var Foo = function(handler, opt_context) { " +
        "  handler.apply(opt_context, x);" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionCall9
  public void testFunctionCall9() throws Exception {
    testTypes(
        " function Foo() {}\n" +
        " Foo.prototype.bar = function(x) {}\n" +
        "var foo =  (new Foo());\n" +
        "foo.bar(3);",
        "actual parameter 1 of Foo.prototype.bar does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionBind1
  public void testFunctionBind1() throws Exception {
    testTypes(
        "" +
        "function f(x, y) { return true; }" +
        "f.bind(null, 3);",
        "actual parameter 2 of f.bind does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionBind2
  public void testFunctionBind2() throws Exception {
    testTypes(
        "" +
        "function f(x) { return true; }" +
        "f(f.bind(null, 3)());",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionBind3
  public void testFunctionBind3() throws Exception {
    testTypes(
        "" +
        "function f(x, y) { return true; }" +
        "f.bind(null, 3)(true);",
        "actual parameter 1 of function does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionBind4
  public void testFunctionBind4() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f.bind(null, 3, 3, 3)(true);",
        "actual parameter 1 of function does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: (number|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionBind5
  public void testFunctionBind5() throws Exception {
    testTypes(
        "" +
        "function f(x) {}" +
        "f.bind(null, true)(3, 3, 3);",
        "actual parameter 2 of f.bind does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: (number|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoogBind1
  public void testGoogBind1() throws Exception {
    testClosureTypes(
        "var goog = {}; goog.bind = function(var_args) {};" +
        "" +
        "function f(x, y) { return true; }" +
        "f(goog.bind(f, null, 'x')());",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoogBind2
  public void testGoogBind2() throws Exception {
    
    
    testClosureTypes(
        "var goog = {}; goog.bind = function(var_args) {};" +
        "" +
        "function f(x, y) { return true; }" +
        "f(goog.bind(f, null, 'x')());",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast2
  public void testCast2() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = new derived();\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast3
  public void testCast3() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = new base();\n",
        "initializing variable\n" +
        "found   : base\n" +
        "required: derived");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast3a
  public void testCast3a() throws Exception {
    
    testTypes("function Base() {}\n" +
        "function Derived() {}\n" +
        "var baseInstance = new Base();" +
        " var baz = baseInstance;\n",
        "initializing variable\n" +
        "found   : Base\n" +
        "required: Derived");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast4
  public void testCast4() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n" +
        " var baz = " +
        "(new base());\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast5
  public void testCast5() throws Exception {
    
    testTypes("function foo() {}\n" +
        "function bar() {}\n" +
        "var baz = (new bar);\n",
        "invalid cast - must be a subtype or supertype\n" +
        "from: bar\n" +
        "to  : foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast5a
  public void testCast5a() throws Exception {
    
    testTypes("function foo() {}\n" +
        "function bar() {}\n" +
        "var barInstance = new bar;\n" +
        "var baz = (barInstance);\n",
        "invalid cast - must be a subtype or supertype\n" +
        "from: bar\n" +
        "to  : foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast6
  public void testCast6() throws Exception {
    
    testTypes("function foo() {}\n" +
        "function bar() {}\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n" +
        "var baz = (new bar);\n" +
        "var baz = (new foo);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast7
  public void testCast7() throws Exception {
    testTypes("var x =  (new Object());",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast8
  public void testCast8() throws Exception {
    testTypes("function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast9
  public void testCast9() throws Exception {
    testTypes("var foo = {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast10
  public void testCast10() throws Exception {
    testTypes("var foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast11
  public void testCast11() throws Exception {
    testTypes("var goog = {}; goog.foo = {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast12
  public void testCast12() throws Exception {
    testTypes("var goog = {}; goog.foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast13
  public void testCast13() throws Exception {
    
    
    testClosureTypes("var goog = {}; " +
        "goog.addDependency('zzz.js', ['goog.foo'], []);" +
        "goog.foo = function() {};" +
        "function f() { return  (new Object()); }",
        "Bad type annotation. Unknown type goog.foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast14
  public void testCast14() throws Exception {
    
    
    testClosureTypes("var goog = {}; " +
        "goog.addDependency('zzz.js', ['goog.bar'], []);" +
        "function f() { return  (new Object()); }",
        null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast15
  public void testCast15() throws Exception {
    
    
    
    
    
    
    
    testTypes(
        "for (var i = 0; i < 10; i++) {" +
          "var x =  ({foo: 3});" +
          " function f(x) {}" +
          "f(x.foo);" +
          "f([].foo);" +
        "}",
        "Property foo never defined on Array");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast16
  public void testCast16() throws Exception {
    
    testTypes(
        "for (var i = 0; i < 10; i++) {" +
          "var x =  (" +
          "  { foo: 3});" +
        "}",
        "assignment to property foo of {foo: string}\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast17a
  public void testCast17a() throws Exception {
    
    testTypes(" function Foo() {} \n" +
        " var x =  (y)");

    testTypes(" function Foo() {} \n" +
        " var x =  (y)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast17b
  public void testCast17b() throws Exception {
    
    testTypes(" function Foo() {} \n" +
        " var x =  ({})");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast19
  public void testCast19() throws Exception {
    testTypes(
        "var x = 'string';\n" +
        "\n" +
        "var y = (x);",
        "invalid cast - must be a subtype or supertype\n" +
        "from: string\n" +
        "to  : number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast20
  public void testCast20() throws Exception {
    testTypes(
        "\n" +
        "var X = {" +
        "  AA: true," +
        "  BB: false," +
        "  CC: null" +
        "};\n" +
        "var y = (true);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast21
  public void testCast21() throws Exception {
    testTypes(
        "\n" +
        "var X = {" +
        "  AA: true," +
        "  BB: false," +
        "  CC: null" +
        "};\n" +
        "var value = true;\n" +
        "var y = (value);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast22
  public void testCast22() throws Exception {
    testTypes(
        "var x = null;\n" +
        "var y = (x);",
        "invalid cast - must be a subtype or supertype\n" +
        "from: null\n" +
        "to  : number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast23
  public void testCast23() throws Exception {
    testTypes(
        "var x = null;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast24
  public void testCast24() throws Exception {
    testTypes(
        "var x = undefined;\n" +
        "var y = (x);",
        "invalid cast - must be a subtype or supertype\n" +
        "from: undefined\n" +
        "to  : number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast25
  public void testCast25() throws Exception {
    testTypes(
        "var x = undefined;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast26
  public void testCast26() throws Exception {
    testTypes(
        "function fn(dir) {\n" +
        "  var node = dir ? 1 : 2;\n" +
        "  fn( (node));\n" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast27
  public void testCast27() throws Exception {
    
    testTypes(
        " function I() {}\n" +
        " function C() {}\n" +
        "var x = new C();\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast27a
  public void testCast27a() throws Exception {
    
    testTypes(
        " function I() {}\n" +
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast28
  public void testCast28() throws Exception {
    
    testTypes(
        " function I() {}\n" +
        " function C() {}\n" +
        " var x;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast28a
  public void testCast28a() throws Exception {
    
    testTypes(
        " function I() {}\n" +
        " function C() {}\n" +
        " var x;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast29a
  public void testCast29a() throws Exception {
    
    testTypes(
        " function C() {}\n" +
        "var x = new C();\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast29b
  public void testCast29b() throws Exception {
    
    testTypes(
        " function C() {}\n" +
        " var x;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast29c
  public void testCast29c() throws Exception {
    
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast30
  public void testCast30() throws Exception {
    
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast31
  public void testCast31() throws Exception {
    
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast32
  public void testCast32() throws Exception {
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast33
  public void testCast33() throws Exception {
    
    
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast34a
  public void testCast34a() throws Exception {
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCast34b
  public void testCast34b() throws Exception {
    testTypes(
        " function C() {}\n" +
        " var x ;\n" +
        "var y = (x);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNestedCasts
  public void testNestedCasts() throws Exception {
    testTypes("var T = function() {};\n" +
        "var V = function() {};\n" +
        "\n" +
        "function f(b) { return b ? new T() : new V(); }\n" +
        "\n" +
        "function g(b) { return b ? true : undefined; }\n" +
        "\n" +
        "function h() {\n" +
        "return  (f( (g(true))));\n" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNativeCast1
  public void testNativeCast1() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(String(true));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNativeCast2
  public void testNativeCast2() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Number(true));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNativeCast3
  public void testNativeCast3() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Boolean(''));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNativeCast4
  public void testNativeCast4() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f(Error(''));",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : Error\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadConstructorCall
  public void testBadConstructorCall() throws Exception {
    testTypes(
        " function Foo() {}" +
        "Foo();",
        "Constructor function (new:Foo): undefined should be called " +
        "with the \"new\" keyword");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeof
  public void testTypeof() throws Exception {
    testTypes("function foo(){ var a = typeof foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeof2
  public void testTypeof2() throws Exception {
    testTypes("function f(){ if (typeof 123 == 'numbr') return 321; }",
              "unknown type: numbr");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeof3
  public void testTypeof3() throws Exception {
    testTypes("function f() {" +
              "return (typeof 123 == 'number' ||" +
              "typeof 123 == 'string' ||" +
              "typeof 123 == 'boolean' ||" +
              "typeof 123 == 'undefined' ||" +
              "typeof 123 == 'function' ||" +
              "typeof 123 == 'object' ||" +
              "typeof 123 == 'unknown'); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType1
  public void testConstructorType1() throws Exception {
    testTypes("function Foo(){}" +
        "var f = new Date();",
        "initializing variable\n" +
        "found   : Date\n" +
        "required: Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType2
  public void testConstructorType2() throws Exception {
    testTypes("function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n" +
        "var f = new Foo();\n" +
        "var n = f.bar;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType3
  public void testConstructorType3() throws Exception {
    
    
    testTypes("var f = new Foo();\n" +
        "var n = f.bar;" +
        "function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType4
  public void testConstructorType4() throws Exception {
    testTypes("function Foo(){\n" +
        "this.bar = new Number(5);\n" +
        "}\n" +
        "var f = new Foo();\n" +
        "var n = f.bar;",
        "initializing variable\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType5
  public void testConstructorType5() throws Exception {
    testTypes("function Foo(){}\n" +
        "if (Foo){}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType6
  public void testConstructorType6() throws Exception {
    testTypes("\n" +
        "function bar() {}\n" +
        "function _foo() {\n" +
        " \n" +
        "  function f(x) {}\n" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType7
  public void testConstructorType7() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("function A(){};");

    JSType type = p.scope.getVar("A").getType();
    assertTrue(type instanceof FunctionType);
    FunctionType fType = (FunctionType) type;
    assertEquals("A", fType.getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType8
  public void testConstructorType8() throws Exception {
    testTypes(
        "var ns = {};" +
        "ns.create = function() { return function() {}; };" +
        " ns.Foo = ns.create();" +
        "ns.Foo.prototype = {x: 0, y: 0};" +
        "\n" +
        "function f(foo) {" +
        "  return foo.x;" +
        "}",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType9
  public void testConstructorType9() throws Exception {
    testTypes(
        "var ns = {};" +
        "ns.create = function() { return function() {}; };" +
        "ns.extend = function(x) { return x; };" +
        " ns.Foo = ns.create();" +
        "ns.Foo.prototype = ns.extend({x: 0, y: 0});" +
        "\n" +
        "function f(foo) {" +
        "  return foo.x;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType10
  public void testConstructorType10() throws Exception {
    testTypes("" +
              "function NonStr() {}" +
              "" +
              "function NonStrKid() {}",
              "NonStrKid cannot extend this type; " +
              "structs can only extend structs");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType11
  public void testConstructorType11() throws Exception {
    testTypes("" +
              "function NonDict() {}" +
              "" +
              "function NonDictKid() {}",
              "NonDictKid cannot extend this type; " +
              "dicts can only extend dicts");
  }

// com.google.javascript.jscomp.TypeCheckTest::testConstructorType12
  public void testConstructorType12() throws Exception {
    testTypes("\n" +
              "function Bar() {}\n" +
              "Bar.prototype = {};\n",
              "Bar cannot extend this type; " +
              "structs can only extend structs");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadStruct
  public void testBadStruct() throws Exception {
    testTypes("function Struct1() {}",
              "@struct used without @constructor for Struct1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadDict
  public void testBadDict() throws Exception {
    testTypes("function Dict1() {}",
              "@dict used without @constructor for Dict1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnonymousPrototype1
  public void testAnonymousPrototype1() throws Exception {
    testTypes(
        "var ns = {};" +
        " ns.Foo = function() {" +
        "  this.bar(3, 5);" +
        "};" +
        "ns.Foo.prototype = {" +
        "  bar: function(x) {}" +
        "};",
        "Function ns.Foo.prototype.bar: called with 2 argument(s). " +
        "Function requires at least 1 argument(s) and no more " +
        "than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnonymousPrototype2
  public void testAnonymousPrototype2() throws Exception {
    testTypes(
        " var Foo = function() {};" +
        "Foo.prototype = {" +
        "  foo: function(x) {}" +
        "};" +
        " var Bar = function() {};",
        "property foo on interface Foo is not implemented by type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnonymousType1
  public void testAnonymousType1() throws Exception {
    testTypes("function f() { return {}; }" +
        "\n" +
        "f().bar = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnonymousType2
  public void testAnonymousType2() throws Exception {
    testTypes("function f() { return {}; }" +
        "\n" +
        "f().bar = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnonymousType3
  public void testAnonymousType3() throws Exception {
    testTypes("function f() { return {}; }" +
        "\n" +
        "f().bar = {FOO: 1};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang1
  public void testBang1() throws Exception {
    testTypes("\n" +
        "function f(x) { return x; }",
        "inconsistent return type\n" +
        "found   : (Object|null)\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang2
  public void testBang2() throws Exception {
    testTypes("\n" +
        "function f(x) { return x ? x : new Object(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang3
  public void testBang3() throws Exception {
    testTypes("\n" +
        "function f(x) { return  (x); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang4
  public void testBang4() throws Exception {
    testTypes("\n" +
        "function f(x, y) {\n" +
        "if (typeof x != 'undefined') { return x == y; }\n" +
        "else { return x != y; }\n}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang5
  public void testBang5() throws Exception {
    testTypes("\n" +
        "function f(x, y) { return !!x && x == y; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang6
  public void testBang6() throws Exception {
    testTypes("\n" +
        "function f(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBang7
  public void testBang7() throws Exception {
    testTypes("function f(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDefinePropertyOnNullableObject1
  public void testDefinePropertyOnNullableObject1() throws Exception {
    testTypes(" var n = {};\n" +
        " n.x = 1;\n" +
        "function f() { return n.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDefinePropertyOnNullableObject2
  public void testDefinePropertyOnNullableObject2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "function f(t) {\n" +
        "t.x = 1; return t.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownConstructorInstanceType1
  public void testUnknownConstructorInstanceType1() throws Exception {
    testTypes(" function g(f) { return new f(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownConstructorInstanceType2
  public void testUnknownConstructorInstanceType2() throws Exception {
    testTypes("function g(f) { return (new f()); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownConstructorInstanceType3
  public void testUnknownConstructorInstanceType3() throws Exception {
    testTypes("function g(f) { var x = new f(); x.a = 1; return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testUnknownPrototypeChain
  public void testUnknownPrototypeChain() throws Exception {
    testTypes("\n" +
              "function inst(co) {\n" +
              " \n" +
              " var c = function() {};\n" +
              " c.prototype = co.prototype;\n" +
              " return new c;\n" +
              "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNamespacedConstructor
  public void testNamespacedConstructor() throws Exception {
    Node root = parseAndTypeCheck(
        "var goog = {};" +
        " goog.MyClass = function() {};" +
        " " +
        "function foo() { return new goog.MyClass(); }");

    JSType typeOfFoo = root.getLastChild().getJSType();
    assert(typeOfFoo instanceof FunctionType);

    JSType retType = ((FunctionType) typeOfFoo).getReturnType();
    assert(retType instanceof ObjectType);
    assertEquals("goog.MyClass", ((ObjectType) retType).getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testComplexNamespace
  public void testComplexNamespace() throws Exception {
    String js =
      "var goog = {};" +
      "goog.foo = {};" +
      "goog.foo.bar = 5;";

    TypeCheckResult p = parseAndTypeCheckWithScope(js);

    
    JSType googScopeType = p.scope.getVar("goog").getType();
    assertTrue(googScopeType instanceof ObjectType);
    assertTrue("foo property not present on goog type",
        ((ObjectType) googScopeType).hasProperty("foo"));
    assertFalse("bar property present on goog type",
        ((ObjectType) googScopeType).hasProperty("bar"));

    
    Node varNode = p.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    JSType googNodeType = varNode.getFirstChild().getJSType();
    assertTrue(googNodeType instanceof ObjectType);

    
    assertTrue(googScopeType == googNodeType);

    
    Node getpropFoo1 = varNode.getNext().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo1.getType());
    assertEquals("goog", getpropFoo1.getFirstChild().getString());
    JSType googGetpropFoo1Type = getpropFoo1.getFirstChild().getJSType();
    assertTrue(googGetpropFoo1Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo1Type == googScopeType);

    
    JSType googFooType = ((ObjectType) googScopeType).getPropertyType("foo");
    assertTrue(googFooType instanceof ObjectType);

    
    
    Node getpropFoo2 = varNode.getNext().getNext()
        .getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.GETPROP, getpropFoo2.getType());
    assertEquals("goog", getpropFoo2.getFirstChild().getString());
    JSType googGetpropFoo2Type = getpropFoo2.getFirstChild().getJSType();
    assertTrue(googGetpropFoo2Type instanceof ObjectType);

    
    assertTrue(googGetpropFoo2Type == googScopeType);

    
    
    JSType googFooGetprop2Type = getpropFoo2.getJSType();
    assertTrue("goog.foo incorrectly annotated in goog.foo.bar selection",
        googFooGetprop2Type instanceof ObjectType);
    ObjectType googFooGetprop2ObjectType = (ObjectType) googFooGetprop2Type;
    assertFalse("foo property present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("foo"));
    assertTrue("bar property not present on goog.foo type",
        googFooGetprop2ObjectType.hasProperty("bar"));
    assertTypeEquals("bar property on goog.foo type incorrectly inferred",
        NUMBER_TYPE, googFooGetprop2ObjectType.getPropertyType("bar"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomSimpleNamespace
  public void testAddingMethodsUsingPrototypeIdiomSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype.m1 = 5");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 1,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace1
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace1()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsUsingPrototypeIdiomComplexNamespace2
  public void testAddingMethodsUsingPrototypeIdiomComplexNamespace2()
      throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var goog = {};" +
        "goog.A = function() {};" +
        "goog.A.prototype.m1 = 5");

    testAddingMethodsUsingPrototypeIdiomComplexNamespace(p);
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace
  public void testAddingMethodsPrototypeIdiomAndObjectLiteralSimpleNamespace()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 2,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testDontAddMethodsIfNoConstructor
  public void testDontAddMethodsIfNoConstructor()
      throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true}");

    JSType functionAType = js1Node.getFirstChild().getJSType();
    assertEquals("function (): undefined", functionAType.toString());
    assertTypeEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m1"));
    assertTypeEquals(UNKNOWN_TYPE,
        U2U_FUNCTION_TYPE.getPropertyType("m2"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionAssignement
  public void testFunctionAssignement() throws Exception {
    testTypes("" +
        "function MSG_CALENDAR_ACCESS_ERROR(ph0, ph1) {return ''}" +
        "" +
        "var MSG_CALENDAR_ADD_ERROR = MSG_CALENDAR_ACCESS_ERROR;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAddMethodsPrototypeTwoWays
  public void testAddMethodsPrototypeTwoWays() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {}" +
        "A.prototype = {m1: 5, m2: true};" +
        "A.prototype.m3 = 'third property!';");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals("A", instanceType.toString());
    assertEquals(NATIVE_PROPERTIES_COUNT + 3,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", NUMBER_TYPE);
    checkObjectType(instanceType, "m2", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m3", STRING_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypePropertyTypes
  public void testPrototypePropertyTypes() throws Exception {
    Node js1Node = parseAndTypeCheck(
        "function A() {\n" +
        "   this.m1;\n" +
        "   this.m2 = {};\n" +
        "   this.m3;\n" +
        "}\n" +
        " A.prototype.m4;\n" +
        " A.prototype.m5 = 0;\n" +
        " A.prototype.m6;\n");

    ObjectType instanceType = getInstanceType(js1Node);
    assertEquals(NATIVE_PROPERTIES_COUNT + 6,
        instanceType.getPropertiesCount());
    checkObjectType(instanceType, "m1", STRING_TYPE);
    checkObjectType(instanceType, "m2",
        createUnionType(OBJECT_TYPE, NULL_TYPE));
    checkObjectType(instanceType, "m3", BOOLEAN_TYPE);
    checkObjectType(instanceType, "m4", STRING_TYPE);
    checkObjectType(instanceType, "m5", NUMBER_TYPE);
    checkObjectType(instanceType, "m6", BOOLEAN_TYPE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueTypeBuiltInPrototypePropertyType
  public void testValueTypeBuiltInPrototypePropertyType() throws Exception {
    Node node = parseAndTypeCheck("\"x\".charAt(0)");
    assertTypeEquals(STRING_TYPE, node.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeclareBuiltInConstructor
  public void testDeclareBuiltInConstructor() throws Exception {
    
    
    Node node = parseAndTypeCheck(
        " var String = function(opt_str) {};\n" +
        "(new String(\"x\")).charAt(0)");
    assertTypeEquals(STRING_TYPE, node.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendBuiltInType1
  public void testExtendBuiltInType1() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n1 = parseAndTypeCheck(externs + "(new String(\"x\")).substr(0,1);");
    assertTypeEquals(STRING_TYPE, n1.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendBuiltInType2
  public void testExtendBuiltInType2() throws Exception {
    String externs =
        " var String = function(opt_str) {};\n" +
        "\n" +
        "String.prototype.substr = function(start, opt_length) {};\n";
    Node n2 = parseAndTypeCheck(externs + "\"x\".substr(0,1);");
    assertTypeEquals(STRING_TYPE, n2.getLastChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendFunction1
  public void testExtendFunction1() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(new Function()).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertTypeEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.TypeCheckTest::testExtendFunction2
  public void testExtendFunction2() throws Exception {
    Node n = parseAndTypeCheck("Function.prototype.f = " +
        "function() { return 1; };\n" +
        "(function() {}).f();");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertTypeEquals(NUMBER_TYPE, type);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck1
  public void testInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck2
  public void testInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo not defined on any superclass of Sub");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck3
  public void testInheritanceCheck3() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck4
  public void testInheritanceCheck4() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck5
  public void testInheritanceCheck5() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on superclass Root; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck6
  public void testInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck7
  public void testInheritanceCheck7() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck8
  public void testInheritanceCheck8() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = 5;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_1
  public void testInheritanceCheck9_1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() { return 3; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_2
  public void testInheritanceCheck9_2() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck9_3
  public void testInheritanceCheck9_3() throws Exception {
    testTypes(
        "function Super() {};" +
        "" +
        "Super.prototype.foo = function() { return 1; };" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_1
  public void testInheritanceCheck10_1() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() { return 3; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_2
  public void testInheritanceCheck10_2() throws Exception {
    testTypes(
        "function Root() {};" +
        "" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck10_3
  public void testInheritanceCheck10_3() throws Exception {
    testTypes(
        "function Root() {};" +
        "" +
        "Root.prototype.foo = function() { return 1; };" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return \"some string\" };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Root\n" +
        "original: function (this:Root): number\n" +
        "override: function (this:Sub): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck11
  public void testInterfaceInheritanceCheck11() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck12
  public void testInheritanceCheck12() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        "goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        "goog.Sub.prototype.foo = \"some string\";");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck13
  public void testInheritanceCheck13() throws Exception {
    testTypes(
        "var goog = {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck14
  public void testInheritanceCheck14() throws Exception {
    testClosureTypes(
        "var goog = {};\n" +
        "\n" +
        "goog.Super = function() {};\n" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "Bad type annotation. Unknown type goog.Missing");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck15
  public void testInheritanceCheck15() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo;" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck16
  public void testInheritanceCheck16() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        " goog.Super.prototype.foo = 3;" +
        "goog.Sub = function() {};" +
        " goog.Sub.prototype.foo = 5;",
        "property foo already defined on superclass goog.Super; " +
        "use @override to override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInheritanceCheck17
  public void testInheritanceCheck17() throws Exception {
    
    
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(
        "var goog = {};" +
        "goog.Super = function() {};" +
        " goog.Super.prototype.foo = function(x) {};" +
        "goog.Sub = function() {};" +
        " goog.Sub.prototype.foo = function(x) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from superclass goog.Super\n" +
        "original: function (this:goog.Super, number): undefined\n" +
        "override: function (this:goog.Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyOverride1
  public void testInterfacePropertyOverride1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyOverride2
  public void testInterfacePropertyOverride2() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck1
  public void testInterfaceInheritanceCheck1() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        "property foo already defined on interface Super; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck2
  public void testInterfaceInheritanceCheck2() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck3
  public void testInterfaceInheritanceCheck3() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo = function() { return 1;};",
        "property foo already defined on interface Root; use @override to " +
        "override it");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck4
  public void testInterfaceInheritanceCheck4() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1;};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck5
  public void testInterfaceInheritanceCheck5() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck6
  public void testInterfaceInheritanceCheck6() throws Exception {
    testTypes(
        "function Root() {};" +
        "Root.prototype.foo = function() {};" +
        "function Super() {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function() { return 1; };",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Root\n" +
        "original: function (this:Root): string\n" +
        "override: function (this:Sub): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck7
  public void testInterfaceInheritanceCheck7() throws Exception {
    testTypes(
        "function Super() {};" +
        "Super.prototype.foo = function(bar) {};" +
        "function Sub() {};" +
        "Sub.prototype.foo =\n" +
        "function(bar) {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Super\n" +
        "original: function (this:Super, number): undefined\n" +
        "override: function (this:Sub, string): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck8
  public void testInterfaceInheritanceCheck8() throws Exception {
    testTypes(
        "function Sub() {};" +
        "Sub.prototype.foo = function() {};",
        new String[] {
          "Bad type annotation. Unknown type Super",
          "property foo not defined on any superclass of Sub"
        });
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck9
  public void testInterfaceInheritanceCheck9() throws Exception {
    testTypes(
        " function I() {}" +
        " I.prototype.bar = function() {};" +
        " function F() {}" +
        " F.prototype.bar = function() {return 3; };" +
        " F.prototype.foo = function() {return 3; };" +
        " " +
        "function G() {}" +
        " function f() { return new G().bar(); }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck10
  public void testInterfaceInheritanceCheck10() throws Exception {
    testTypes(
        " function I() {}" +
        " I.prototype.bar = function() {};" +
        " function F() {}" +
        " F.prototype.foo = function() {return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.bar = G.prototype.foo;" +
        " function f() { return new G().bar(); }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck12
  public void testInterfaceInheritanceCheck12() throws Exception {
    testTypes(
        " function I() {};\n" +
        " I.prototype.foobar;\n" +
        "\n" +
        "function C() {\n" +
        " this.foobar = 2;};\n" +
        " \n var test = new C(); alert(test.foobar);",
        "mismatch of the foobar property type and the type of the property" +
        " it overrides from interface I\n" +
        "original: string\n" +
        "override: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck13
  public void testInterfaceInheritanceCheck13() throws Exception {
    testTypes(
        "function abstractMethod() {};\n" +
        "var base = function() {};\n" +
        " var Int = function() {}\n" +
        " var x; \n" +
        " base.prototype.bar = abstractMethod; \n" +
        " var foo;\n" +
        "foo.bar();");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck14
  public void testInterfaceInheritanceCheck14() throws Exception {
    testTypes(
        "function A() {};" +
        "A.prototype.foo = function() {};" +
        "function B() {};" +
        "B.prototype.bar = function() {};" +
        "function C() {};" +
        "C.prototype.foo = function() {};" +
        "C.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck15
  public void testInterfaceInheritanceCheck15() throws Exception {
    testTypes(
        "function A() {};" +
        "A.prototype.foo = function() {};" +
        "function B() {};" +
        "B.prototype.bar = function() {};" +
        "function C() {};" +
        "C.prototype.foo = function() {};" +
        "C.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInheritanceCheck16
  public void testInterfaceInheritanceCheck16() throws Exception {
    testTypes(
        "function A() {};" +
        "A.prototype.foo = function() {};" +
        "A.prototype.bar = function() {};" +
        "function B() {};" +
        "B.prototype.foo = function() { return 'string'};" +
        "B.prototype.bar = function() { return 3 };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented
  public void testInterfacePropertyNotImplemented() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented2
  public void testInterfacePropertyNotImplemented2() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Int2() {};" +
        "function Foo() {};",
        "property foo on interface Int is not implemented by type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfacePropertyNotImplemented3
  public void testInterfacePropertyNotImplemented3() throws Exception {
    testTypes(
        "function Int() {};" +
        "Int.prototype.foo = function() {};" +
        "function Foo() {};" +
        "Foo.prototype.foo = function() {};",
        "mismatch of the foo property type and the type of the property it " +
        "overrides from interface Int\n" +
        "original: function (this:Int): string\n" +
        "override: function (this:Foo): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubConstructorImplementingInterface
  public void testStubConstructorImplementingInterface() throws Exception {
    
    
    testTypes(
        
        " function Int() {}\n" +
        "Int.prototype.foo = function() {};" +
        " var Foo;\n",
        "", null, false);
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteral
  public void testObjectLiteral() throws Exception {
    Node n = parseAndTypeCheck("var a = {m1: 7, m2: 'hello'}");

    Node nameNode = n.getFirstChild().getFirstChild();
    Node objectNode = nameNode.getFirstChild();

    
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals(Token.OBJECTLIT, objectNode.getType());

    
    ObjectType objectType =
        (ObjectType) objectNode.getJSType();
    assertTypeEquals(NUMBER_TYPE, objectType.getPropertyType("m1"));
    assertTypeEquals(STRING_TYPE, objectType.getPropertyType("m2"));

    
    assertTypeEquals(objectType, nameNode.getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration1
  public void testObjectLiteralDeclaration1() throws Exception {
    testTypes(
        "var x = {" +
        " abc: true," +
        " 'def': 0," +
        " 3: 'fgh'" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration2
  public void testObjectLiteralDeclaration2() throws Exception {
    testTypes(
        "var x = {" +
        "   abc: true" +
        "};" +
        "x.abc = 0;",
        "assignment to property abc of x\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration3
  public void testObjectLiteralDeclaration3() throws Exception {
    testTypes(
        " function f(x) {}" +
        "f({foo: function() {}});");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration4
  public void testObjectLiteralDeclaration4() throws Exception {
    testClosureTypes(
        "var x = {" +
        "   abc: function(x) {}" +
        "};" +
        " x.abc = function(x) {};",
        "assignment to property abc of x\n" +
        "found   : function (string): undefined\n" +
        "required: function (boolean): undefined");
    
    
    
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration5
  public void testObjectLiteralDeclaration5() throws Exception {
    testTypes(
        "var x = {" +
        "   abc: function(x) {}" +
        "};" +
        " x.abc = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration6
  public void testObjectLiteralDeclaration6() throws Exception {
    testTypes(
        "var x = {};" +
        " x.abc = function(x) {};" +
        "x = {" +
        "  " +
        "  abc: function(x) {}" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testObjectLiteralDeclaration7
  public void testObjectLiteralDeclaration7() throws Exception {
    testTypes(
        "var x = {};" +
        " x.abc = function(x) {};" +
        "x = {" +
        "  " +
        "  abc: function(x) {}" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallDateConstructorAsFunction
  public void testCallDateConstructorAsFunction() throws Exception {
    
    
    Node n = parseAndTypeCheck("Date()");
    assertTypeEquals(STRING_TYPE, n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallErrorConstructorAsFunction
  public void testCallErrorConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Error('x')");
    assertTypeEquals(ERROR_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testCallArrayConstructorAsFunction
  public void testCallArrayConstructorAsFunction() throws Exception {
    Node n = parseAndTypeCheck("Array()");
    assertTypeEquals(ARRAY_TYPE,
                 n.getFirstChild().getFirstChild().getJSType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropertyTypeOfUnionType
  public void testPropertyTypeOfUnionType() throws Exception {
    testTypes("var a = {};" +
        " a.N = function() {};\n" +
        "a.N.prototype.p = 1;\n" +
        " a.S = function() {};\n" +
        "a.S.prototype.p = 'a';\n" +
        "\n" +
        "var f = function(x) { return x.p; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface1
  public void testAnnotatedPropertyOnInterface1() throws Exception {
    
    
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface2
  public void testAnnotatedPropertyOnInterface2() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface3
  public void testAnnotatedPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        " T.prototype.f = function() { };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAnnotatedPropertyOnInterface4
  public void testAnnotatedPropertyOnInterface4() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function T() {};\n" +
        " T.prototype.f = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface5
  public void testWarnUnannotatedPropertyOnInterface5() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnUnannotatedPropertyOnInterface6
  public void testWarnUnannotatedPropertyOnInterface6() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface1
  public void testDataPropertyOnInterface1() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface2
  public void testDataPropertyOnInterface2() throws Exception {
    reportMissingOverrides = CheckLevel.OFF;
    testTypes(" function T() {};\n" +
        "T.prototype.x;\n" +
        "\n" +
        "function C() {}\n" +
        "C.prototype.x = 'foo';",
        "mismatch of the x property type and the type of the property it " +
        "overrides from interface T\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface3
  public void testDataPropertyOnInterface3() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x;\n" +
        "\n" +
        "function C() {}\n" +
        "\n" +
        "C.prototype.x = 'foo';",
        "mismatch of the x property type and the type of the property it " +
        "overrides from interface T\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDataPropertyOnInterface4
  public void testDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x;\n" +
        "\n" +
        "function C() {  \n this.x = 'foo'; }\n",
        "mismatch of the x property type and the type of the property it " +
        "overrides from interface T\n" +
        "original: number\n" +
        "override: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface3
  public void testWarnDataPropertyOnInterface3() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testWarnDataPropertyOnInterface4
  public void testWarnDataPropertyOnInterface4() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = 1;",
        "interface members can only be empty property declarations, "
        + "empty functions, or goog.abstractMethod");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface4
  public void testErrorMismatchingPropertyOnInterface4() throws Exception {
    testTypes(" u.T = function () {};\n" +
        "u.T.prototype.x =\n" +
        "function() {};",
        "parameter foo does not appear in u.T.prototype.x's parameter list");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface5
  public void testErrorMismatchingPropertyOnInterface5() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { };",
        "assignment to property x of T.prototype\n" +
        "found   : function (): undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testErrorMismatchingPropertyOnInterface6
  public void testErrorMismatchingPropertyOnInterface6() throws Exception {
    testClosureTypesMultipleWarnings(
        " function T() {};\n" +
        "T.prototype.x = 1",
        Lists.newArrayList(
            "assignment to property x of T.prototype\n" +
            "found   : number\n" +
            "required: function (this:T): number",
            "interface members can only be empty property declarations, " +
            "empty functions, or goog.abstractMethod"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceNonEmptyFunction
  public void testInterfaceNonEmptyFunction() throws Exception {
    testTypes(" function T() {};\n" +
        "T.prototype.x = function() { return 'foo'; }",
        "interface member functions must have an empty body"
        );
  }

// com.google.javascript.jscomp.TypeCheckTest::testDoubleNestedInterface
  public void testDoubleNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.I3 = function() {};\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticDataPropertyOnNestedInterface
  public void testStaticDataPropertyOnNestedInterface() throws Exception {
    testTypes(" var I1 = function() {};\n" +
              " I1.I2 = function() {};\n" +
              " I1.I2.x = 1;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceInstantiation
  public void testInterfaceInstantiation() throws Exception {
    testTypes("var f = function(){}; new f",
              "cannot instantiate non-constructor");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrototypeLoop
  public void testPrototypeLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type T",
            "Could not resolve type in @extends tag of T"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplementsLoop
  public void testImplementsLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "var T = function() {};" +
        "alert((new T).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type T"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testImplementsExtendsLoop
  public void testImplementsExtendsLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "var G = function() {};" +
            "var F = function() {};" +
        "alert((new F).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type F"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceExtendsLoop
  public void testInterfaceExtendsLoop() throws Exception {
    
    
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "var G = function() {};" +
            "var F = function() {};",
        Lists.newArrayList(
            "Could not resolve type in @extends tag of G"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testConversionFromInterfaceToRecursiveConstructor
  public void testConversionFromInterfaceToRecursiveConstructor()
      throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            " var OtherType = function() {}\n" +
            "\n" +
            "var MyType = function() {}\n" +
            "\n" +
            "var x =  (new Object());",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type MyType",
            "initializing variable\n" +
            "found   : OtherType\n" +
            "required: (MyType|null)"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssign
  public void testDirectPrototypeAssign() throws Exception {
    
    testTypes(
        " function Foo() {}" +
        " function Bar() {}" +
        " Bar.prototype = new Foo()");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry1
  public void testResolutionViaRegistry1() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry2
  public void testResolutionViaRegistry2() throws Exception {
    testTypes(
        " u.T = function() {" +
        "  this.a = 0; };\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry3
  public void testResolutionViaRegistry3() throws Exception {
    testTypes(" u.T = function() {};\n" +
        " u.T.prototype.a = 0;\n" +
        "\n" +
        "var f = function(t) { return t.a; };",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry4
  public void testResolutionViaRegistry4() throws Exception {
    testTypes(" u.A = function() {};\n" +
        "\nu.A.A = function() {}\n;" +
        "\nu.A.B = function() {};\n" +
        "var ab = new u.A.B();\n" +
        " var a = ab;\n" +
        " var aa = ab;\n",
        "initializing variable\n" +
        "found   : u.A.B\n" +
        "required: u.A.A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testResolutionViaRegistry5
  public void testResolutionViaRegistry5() throws Exception {
    Node n = parseAndTypeCheck(" u.T = function() {}; u.T");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof FunctionType);
    assertEquals("u.T",
        ((FunctionType) type).getInstanceType().getReferenceName());
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation1
  public void testGatherProperyWithoutAnnotation1() throws Exception {
    Node n = parseAndTypeCheck(" var T = function() {};" +
        "var t; t.x; t;");
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(objectType),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testGatherProperyWithoutAnnotation2
  public void testGatherProperyWithoutAnnotation2() throws Exception {
    TypeCheckResult ns =
        parseAndTypeCheckWithScope("var t; t.x; t;");
    Node n = ns.root;
    JSType type = n.getLastChild().getLastChild().getJSType();
    assertFalse(type.isUnknownType());
    assertTypeEquals(type, OBJECT_TYPE);
    assertTrue(type instanceof ObjectType);
    ObjectType objectType = (ObjectType) type;
    assertFalse(objectType.hasProperty("x"));
    Asserts.assertTypeCollectionEquals(
        Lists.newArrayList(OBJECT_TYPE),
        registry.getTypesWithProperty("x"));
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionMasksVariableBug
  public void testFunctionMasksVariableBug() throws Exception {
    testTypes("var x = 4; var f = function x(b) { return b ? 1 : x(true); };",
        "function x masks variable (IE bug)");
  }
