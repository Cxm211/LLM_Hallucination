public int read(byte[] buffer, int start, int length) throws IOException {
        if (closed) {
            throw new IOException("The stream is closed");
        }
        if (current == null) {
            return -1;
        }
        if (buffer == null) {
            throw new NullPointerException();
        }
        if (start < 0 || length < 0 || start > buffer.length || buffer.length - start < length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }

        if (current.getMethod() == ZipArchiveOutputStream.STORED) {
            int csize = (int) current.getSize();
            if (readBytesOfEntry >= csize) {
                return -1;
            }
            if (offsetInBuffer >= lengthOfLastRead) {
                offsetInBuffer = 0;
                if ((lengthOfLastRead = in.read(buf)) == -1) {
                    return -1;
                }
                count(lengthOfLastRead);
                bytesReadFromStream += lengthOfLastRead;
            }
            int remainingInBuffer = lengthOfLastRead - offsetInBuffer;
            int toRead = Math.min(length, remainingInBuffer);
            int remainingInEntry = csize - readBytesOfEntry;
            if (toRead > remainingInEntry) {
                toRead = remainingInEntry;
            }
            System.arraycopy(buf, offsetInBuffer, buffer, start, toRead);
            offsetInBuffer += toRead;
            readBytesOfEntry += toRead;
            crc.update(buffer, start, toRead);
            return toRead;
        }

        if (inf.finished()) {
            return -1;
        }
        if (inf.needsInput()) {
            fill();
            if (lengthOfLastRead > 0) {
                bytesReadFromStream += lengthOfLastRead;
            }
        }
        int read = 0;
        try {
            read = inf.inflate(buffer, start, length);
        } catch (DataFormatException e) {
            throw new ZipException(e.getMessage());
        }
        if (read == 0 && inf.finished()) {
            return -1;
        }
        crc.update(buffer, start, read);
        return read;
    }