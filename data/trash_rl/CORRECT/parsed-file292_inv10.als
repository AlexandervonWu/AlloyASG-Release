module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv10[] {
(all t: (one Trash) {
((t.link) in Trash)
})
}
pred inv10C[] {
((Trash.link) in Trash)
}
pred overconstrained[] {
((inv10C[]) && (!(inv10[])))
}
pred underconstrained[] {
((!(inv10C[])) && (inv10[]))
}
pred both[] {
((inv10C[]) && (inv10[]))
}



run overconstrained
run underconstrained
run both
