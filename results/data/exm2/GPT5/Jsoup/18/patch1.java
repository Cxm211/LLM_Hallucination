    String consumeToEnd() {
        String data = input.substring(pos);
        pos = input.length();
        return data;
    }