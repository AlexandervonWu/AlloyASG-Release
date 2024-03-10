module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv7[] {
(all f,f2: (one File) {
(((f->f2) in link) => (f !in Trash))
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
