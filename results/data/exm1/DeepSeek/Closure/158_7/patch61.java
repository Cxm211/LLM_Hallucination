  public void testPropertyInference8() throws Exception {
    testTypes(
        " function F() { " +
        "   this.x_ = 'x';" +
        "}" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "assignment to property x_ of F\n" +
        "found   : number\n" +
        "required: string");
  }