void setInitialBuffer(byte[] out, int outPos, int outAvail) {
    if (out != null && outPos + outAvail <= out.length) {
        buffer = out;
        pos = outPos;
        readPos = outPos;
    }
}