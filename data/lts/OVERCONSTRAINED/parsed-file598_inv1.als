module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv1[] {
(let k = (trans.State) {
(iden in ((~k).k))
})
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
