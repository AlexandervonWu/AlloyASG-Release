module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv5[] {
(all disj s1,s2: (one State) {
(all e: (one Event) {
(some n: (one State) {
((((e->n) in (s1.trans)) && ((e->n) in (s2.trans))) => (e = e))
})
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
