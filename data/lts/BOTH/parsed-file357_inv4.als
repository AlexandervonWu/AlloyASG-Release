module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv4[] {
(all s: (one State) {
(some e: (one Event) {
((e.(s.trans)) in Init)
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
