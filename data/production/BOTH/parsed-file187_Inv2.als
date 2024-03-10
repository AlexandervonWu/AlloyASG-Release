module unknown
open util/integer [] as integer
open util/ordering [Position] as ordering
sig Position {}
sig Product {}
sig Component extends Product {
parts: (set Product),
position: (one Position)
}
sig Resource extends Product {}
sig Robot {
position: (one Position)
}
pred Inv2[] {
(all c,c1: (one Component),p: (one Position) {
(((c !in (c.parts)) && (c1 !in (c1.parts)) && (((c->p) + (c1->p)) in position)) => (c = c1))
})
}
pred Inv2C[] {
(all c: (one Component) {
(c !in (c.(^parts)))
})
}
pred overconstrained[] {
((Inv2C[]) && (!(Inv2[])))
}
pred underconstrained[] {
((!(Inv2C[])) && (Inv2[]))
}
pred both[] {
((Inv2C[]) && (Inv2[]))
}



run overconstrained
run underconstrained
run both
