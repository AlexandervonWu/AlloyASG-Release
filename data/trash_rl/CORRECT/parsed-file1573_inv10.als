module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv10[] {
(all f: (one File) {
(all f1: (one File) {
((((f->f1) in link) && (f in Trash)) => (f1 in Trash))
})
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
