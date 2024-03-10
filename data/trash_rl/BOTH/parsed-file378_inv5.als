module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv5[] {
(all p: (one Protected) {
(!(p in Trash))
})
}
pred inv5C[] {
((File - Protected) in Trash)
}
pred overconstrained[] {
((inv5C[]) && (!(inv5[])))
}
pred underconstrained[] {
((!(inv5C[])) && (inv5[]))
}
pred both[] {
((inv5C[]) && (inv5[]))
}



run overconstrained
run underconstrained
run both
