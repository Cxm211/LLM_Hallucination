public void write(byte[] wBuf, int wOffset, int numToWrite) throws IOException {
    int originalNumToWrite = numToWrite;
    if ((currBytes + numToWrite) > currSize) {
        throw new IOException("request to write '" + numToWrite
                              + "' bytes exceeds size in header of '"
                              + currSize + "' bytes for entry '"
                              + currName + "'");
    }
    if (assemLen > 0) {
        if ((assemLen + numToWrite) >= recordBuf.length) {
            int aLen = recordBuf.length - assemLen;
            System.arraycopy(assemBuf, 0, recordBuf, 0,
                             assemLen);
            System.arraycopy(wBuf, wOffset, recordBuf,
                             assemLen, aLen);
            buffer.writeRecord(recordBuf);
            currBytes += recordBuf.length;
            wOffset += aLen;
            numToWrite -= aLen;
            assemLen = 0;
        } else {
            System.arraycopy(wBuf, wOffset, assemBuf, assemLen,
                             numToWrite);
            wOffset += numToWrite;
            assemLen += numToWrite;
            numToWrite = 0;
        }
    }
    while (numToWrite > 0) {
        if (numToWrite < recordBuf.length) {
            System.arraycopy(wBuf, wOffset, assemBuf, assemLen,
                             numToWrite);
            assemLen += numToWrite;
            break;
        }
        buffer.writeRecord(wBuf, wOffset);
        int num = recordBuf.length;
        currBytes += num;
        numToWrite -= num;
        wOffset += num;
    }
    count(originalNumToWrite - numToWrite);
}