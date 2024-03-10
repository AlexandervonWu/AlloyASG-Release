module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv7[] {
(no ((File.link) & Trash))
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
