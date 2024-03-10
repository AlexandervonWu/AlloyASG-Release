module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv3[] {
(one Trash)
}
pred inv3C[] {
(some Trash)
}
pred overconstrained[] {
((inv3C[]) && (!(inv3[])))
}
pred underconstrained[] {
((!(inv3C[])) && (inv3[]))
}
pred both[] {
((inv3C[]) && (inv3[]))
}



run overconstrained
run underconstrained
run both
