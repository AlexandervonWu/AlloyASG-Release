module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv1[] {
(State in ((trans.State).Event))
}
pred inv1C[] {
(all s: (one State) {
(some (s.trans))
})
}
pred overconstrained[] {
((inv1C[]) && (!(inv1[])))
}
pred underconstrained[] {
((!(inv1C[])) && (inv1[]))
}
pred both[] {
((inv1C[]) && (inv1[]))
}



run overconstrained
run underconstrained
run both
