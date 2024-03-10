package is.fivefivefive.alloyasg.etc;

import parser.etc.Pair;

public class Triple<A, B, C> {
    public A x;
    public B y;
    public C z;
    public Triple(A x, B y, C z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Triple<A, B, C> t) {
        return this.x.equals(t.x) && this.y.equals(t.y) && this.z.equals(t.z);
    }

    public Pair<A, B> pairXY() {
        return Pair.of(x, y);
    }

    public Pair<A, C> pairXZ() {
        return Pair.of(x, z);
    }

    public Pair<B, C> pairYZ() {
        return Pair.of(y, z);
    }

    public String toString() {
        return "(" + x.toString() + ", " + y.toString() + ", " + z.toString() + ")";
    }
}
