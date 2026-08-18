        private String formatMethodCall() {
            StringBuilder sb = new StringBuilder();
            sb.append(invocation.getMethod().getName()).append("(");
            Object[] args = invocation.getArguments();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(String.valueOf(args[i]));
            }
            sb.append(")");
            return sb.toString();
        }