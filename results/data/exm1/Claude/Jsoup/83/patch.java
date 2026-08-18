String consumeTagName() {
        bufferUp();
        final int start = bufPos;
        int remaining = bufLength;
        char[] val = charBuf;

        while (bufPos < remaining) {
            final char c = val[bufPos];
            if (c == '\t'|| c ==  '\n'|| c ==  '\r'|| c ==  '\f'|| c ==  ' '|| c ==  '/'|| c ==  '>'|| c ==  TokeniserState.nullChar)
                break;
            bufPos++;
        }
        
        if (bufPos == remaining && !isEmpty()) {
            bufferUp();
            remaining = bufLength;
            val = charBuf;
            while (bufPos < remaining) {
                final char c = val[bufPos];
                if (c == '\t'|| c ==  '\n'|| c ==  '\r'|| c ==  '\f'|| c ==  ' '|| c ==  '/'|| c ==  '>'|| c ==  TokeniserState.nullChar)
                    break;
                bufPos++;
            }
        }

        return bufPos > start ? cacheString(charBuf, stringCache, start, bufPos -start) : "";
    }