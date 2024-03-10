module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv3[] {
(all x,z,v: (one State),y: (one Event) {
((((x->(y->z)) in trans) && ((x->(y->v)) in trans)) => (z = v))
})
}
pred inv3C[] {
(all s: (one State),e: (one Event) {
(lone (e.(s.trans)))
})
}
pred overconstrained[] {
((inv3C[]) && (!(inv3[])))
}
pred underconstrained[] {
((!(inv3C[])) && (inv3[]))
}
pred both[] {
((inv3C[]) && (inv3[]))
}



run overconstrained
run underconstrained
run both
