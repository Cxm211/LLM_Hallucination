private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    stream.update();
    return result;
  }