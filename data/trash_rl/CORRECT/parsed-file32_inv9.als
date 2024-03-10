module unknown
open util/integer [] as integer
sig File {
link: (set File)
}
sig Trash in File {}
sig Protected in File {}
pred inv9[] {
(all f: (one File) {
(no ((f.link).link))
})
}
pred inv9C[] {
(no (link.link))
}
pred overconstrained[] {
((inv9C[]) && (!(inv9[])))
}
pred underconstrained[] {
((!(inv9C[])) && (inv9[]))
}
pred both[] {
((inv9C[]) && (inv9[]))
}



run overconstrained
run underconstrained
run both
