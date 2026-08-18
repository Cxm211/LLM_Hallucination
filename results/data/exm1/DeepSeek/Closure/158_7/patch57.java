  public void testPropertyInference4() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.x_ = 3;" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }