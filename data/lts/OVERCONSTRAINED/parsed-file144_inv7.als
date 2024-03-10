module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv7[] {
(all x: (one State),y: (one Init),z: (one Event) {
((trans in (y one->one (z one->one x))) && (x != y))
})
}
pred inv7C[] {
(let tr = ({ s1,s2: (one State) {
(some e: (one Event) {
((s1->(e->s2)) in trans)
})
} }) {
(all s: (one (Init.(^tr))) {
(some i: (one Init) {
(i in (s.(^tr)))
})
})
})
}
pred overconstrained[] {
((inv7C[]) && (!(inv7[])))
}
pred underconstrained[] {
((!(inv7C[])) && (inv7[]))
}
pred both[] {
((inv7C[]) && (inv7[]))
}



run overconstrained
run underconstrained
run both
