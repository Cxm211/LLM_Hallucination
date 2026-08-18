private String formatMethodCall() {
    StringBuilder sb = new StringBuilder();
    sb.append(invocation.getMethod().getName()).append("(");
    Object[] args = invocation.getArguments();
    if (args != null && args.length > 0) {
        for (int i = 0; i < args.length; i++) {
            sb.append(String.valueOf(args[i]));
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
    }
    sb.append(")");
    return sb.toString();
}