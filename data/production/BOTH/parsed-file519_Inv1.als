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
pred Inv1[] {
(always (some (Component.parts)))
}
pred Inv1C[] {
(all c: (one Component) {
(some (c.parts))
})
}
pred overconstrained[] {
((Inv1C[]) && (!(Inv1[])))
}
pred underconstrained[] {
((!(Inv1C[])) && (Inv1[]))
}
pred both[] {
((Inv1C[]) && (Inv1[]))
}



run overconstrained
run underconstrained
run both
