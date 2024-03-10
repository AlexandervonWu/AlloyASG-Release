module unknown
open util/integer [] as integer
sig Person {
Tutors: (set Person),
Teaches: (set Class)
}
sig Group {}
sig Class {
Groups: (Person->Group)
}
sig Teacher in Person {}
sig Student in Person {}
pred inv9[] {
(all c: (one Class) {
(all t,x: (one Teacher) {
((((t->c) in Teaches) && ((x->c) in Teaches)) => (t = x))
})
})
}
pred inv9C[] {
(all c: (one Class) {
(lone ((Teaches.c) & Teacher))
})
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
