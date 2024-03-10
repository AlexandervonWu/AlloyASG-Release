module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv2[] {
(all s1,s2: (one State) {
(((s1 in Init) && (s2 in Init)) => (s1 = s2))
})
}
pred inv2C[] {
(one Init)
}
pred overconstrained[] {
((inv2C[]) && (!(inv2[])))
}
pred underconstrained[] {
((!(inv2C[])) && (inv2[]))
}
pred both[] {
((inv2C[]) && (inv2[]))
}



run overconstrained
run underconstrained
run both
