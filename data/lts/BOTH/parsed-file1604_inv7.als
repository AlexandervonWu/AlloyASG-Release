module unknown
open util/integer [] as integer
sig State {
trans: (Event->State)
}
sig Init in State {}
sig Event {}
pred inv7[] {
(let t = ({ x,y: (one State) {
(some z: (one Event) {
((x->(z->y)) in trans)
})
} }) {
(all s: (one State) {
(Init in (s.(~t)))
})
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
