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
pred Inv1[] {
(some (User.visible))
}
pred Inv1C[] {
(all u: (one User) {
((u.visible) in (u.profile))
})
}
pred overconstrained[] {
((Inv1C[]) && (!(Inv1[])))
}
pred underconstrained[] {
((!(Inv1C[])) && (Inv1[]))
}
pred both[] {
((Inv1C[]) && (Inv1[]))
}



run overconstrained
run underconstrained
run both
