module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv6[] {
(all f,a1,a2: (one File) {
((((f->a1) in link) && ((f->a2) in link)) => (a1 = a2))
})
}
pred inv6C[] {
(((~link).link) in iden)
}
pred overconstrained[] {
((inv6C[]) && (!(inv6[])))
}
pred underconstrained[] {
((!(inv6C[])) && (inv6[]))
}
pred both[] {
((inv6C[]) && (inv6[]))
}



run overconstrained
run underconstrained
run both
