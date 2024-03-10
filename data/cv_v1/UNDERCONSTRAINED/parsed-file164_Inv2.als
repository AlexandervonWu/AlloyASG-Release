module unknown
open util/integer [] as integer
abstract sig Source {}
sig User extends Source {
profile: (set Work),
visible: (set Work)
}
sig Institution extends Source {}
sig Id {}
sig Work {
ids: (some Id),
source: (one Source)
}
pred Inv2[] {
(all u: (one User),w: (one Work) {
(((u->w) in profile) => (((w.source) in User) || ((w.source) in Institution)))
})
}
pred Inv2C[] {
(all u: (one User),w: (one Work) {
((w in (u.profile)) => ((u in (w.source)) || (some i: (one Institution) {
(i in (w.source))
})))
})
}
pred overconstrained[] {
((Inv2C[]) && (!(Inv2[])))
}
pred underconstrained[] {
((!(Inv2C[])) && (Inv2[]))
}
pred both[] {
((Inv2C[]) && (Inv2[]))
}



run overconstrained
run underconstrained
run both
