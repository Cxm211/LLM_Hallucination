// com/google/javascript/jscomp/TypeCheckTest.java
public void testMatchConstraintPropertyExistsNotDeclared() throws Exception {
  testTypes(
    "/** @param {{a: string}} x */ function f(x) {} var obj = {a: 3}; f(obj);",
    "assignment to property a of {a: string}\nfound   : number\nrequired: string"
  );
}
