module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv2[] {
(no (Trash & File))
}
pred inv2C[] {
(File in Trash)
}
pred overconstrained[] {
((inv2C[]) && (!(inv2[])))
}
pred underconstrained[] {
((!(inv2C[])) && (inv2[]))
}
pred both[] {
((inv2C[]) && (inv2[]))
}



run overconstrained
run underconstrained
run both
