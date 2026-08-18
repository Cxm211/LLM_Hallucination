// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testDirectCallAndApply() {
    test(
        "(function() {\n" +
        "  function hello() {\n" +
        "    return \"hi\";\n" +
        "  }\n" +
        "  function world() {\n" +
        "    return \"earth\";\n" +
        "  }\n" +
        "  function add(x, y) {\n" +
        "    return x + y;\n" +
        "  }\n" +
        "  var a = hello();\n" +
        "  var b = world.call();\n" +
        "  var c = add.apply(null, [5, 6]);\n" +
        "})()",
        "(function(){" +
        "  var a=\"hi\";" +
        "  var b=\"earth\";" +
        "  var c=5+6" +
        "})()");
  }
