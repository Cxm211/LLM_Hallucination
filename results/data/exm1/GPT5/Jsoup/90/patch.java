private static boolean looksLikeUtf8(byte[] input) {
            int i = 0;
            // BOM:
            if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
                && (input[1] & 0xFF) == 0xBB && (input[2] & 0xFF) == 0xBF) {
                i = 3;
            }

            int j = input.length;
            while (i < j) {
                int o = input[i] & 0xFF;
                if ((o & 0x80) == 0) { // ASCII
                    i++;
                    continue;
                }

                int nCont;
                if ((o & 0xE0) == 0xC0) {
                    nCont = 1;
                } else if ((o & 0xF0) == 0xE0) {
                    nCont = 2;
                } else if ((o & 0xF8) == 0xF0) {
                    nCont = 3;
                } else {
                    return false;
                }

                if (i + nCont >= j) {
                    return false; // not enough bytes
                }

                for (int k = 1; k <= nCont; k++) {
                    int c = input[i + k] & 0xFF;
                    if ((c & 0xC0) != 0x80) {
                        return false;
                    }
                }
                i += nCont + 1;
            }
            return true;
        }