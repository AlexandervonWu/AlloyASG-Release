module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv3[] {
(all s: (one State),e,e1: (one Event) {
((((s->e) in (trans.State)) && ((s->e1) in (trans.State))) => (e = e1))
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
