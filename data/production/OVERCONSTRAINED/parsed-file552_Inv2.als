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
(no (Component <: (*parts)))
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
