private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    looksLikeBlockComment = result.trim().startsWith("/*");
    return result;
}