String consumeToEnd() {
        if (pos >= length) return "";
        String data = input.substring(pos, length);
        pos = length;
        return data;
    }