module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv5[] {
(all s,s1: (one State),e: (one Event) {
(some s2,s3: (one State) {
(((s->(e->s2)) in trans) => ((s1->(e->s3)) in trans))
})
})
}
pred inv5C[] {
(all s: (one State),s1: (one State) {
(((s.trans).State) = ((s1.trans).State))
})
}
pred overconstrained[] {
((inv5C[]) && (!(inv5[])))
}
pred underconstrained[] {
((!(inv5C[])) && (inv5[]))
}
pred both[] {
((inv5C[]) && (inv5[]))
}



run overconstrained
run underconstrained
run both
