module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv4[] {

}
pred inv4C[] {
(no (Protected & Trash))
}
pred overconstrained[] {
((inv4C[]) && (!(inv4[])))
}
pred underconstrained[] {
((!(inv4C[])) && (inv4[]))
}
pred both[] {
((inv4C[]) && (inv4[]))
}



run overconstrained
run underconstrained
run both
