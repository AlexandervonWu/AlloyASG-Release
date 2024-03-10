module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv7[] {
(all f,l: (one File) {
(((f->l) in link) => (l !in Trash))
})
}
pred inv7C[] {
(no (link.Trash))
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
