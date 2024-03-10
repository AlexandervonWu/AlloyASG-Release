module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv8[] {
(link in (none->none))
}
pred inv8C[] {
(no link)
}
pred overconstrained[] {
((inv8C[]) && (!(inv8[])))
}
pred underconstrained[] {
((!(inv8C[])) && (inv8[]))
}
pred both[] {
((inv8C[]) && (inv8[]))
}



run overconstrained
run underconstrained
run both
