module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv4[] {
(all i: (one Init) {
(some e: (one Event) {
(no ((State - Init) - (Event.(^(i.trans)))))
})
})
}
pred inv4C[] {
(let tr = ({ s1,s2: (one State) {
(some e: (one Event) {
((s1->(e->s2)) in trans)
})
} }) {
(State in (Init.(^tr)))
})
}
pred overconstrained[] {
((inv4C[]) && (!(inv4[])))
}
pred underconstrained[] {
((!(inv4C[])) && (inv4[]))
}
pred both[] {
((inv4C[]) && (inv4[]))
}



run overconstrained
run underconstrained
run both
