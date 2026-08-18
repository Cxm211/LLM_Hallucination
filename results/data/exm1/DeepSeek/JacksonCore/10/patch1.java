public int calcHash(int q1)
{
    int hash = q1 ^ _seed;
    hash += (hash >>> 16);
    hash ^= (hash >>> 12);
    return hash;
}