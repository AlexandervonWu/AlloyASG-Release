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
pred inv11[] {
(all t: (one Teacher),c: (one Class) {
(((t->c) in Teaches) => (some s: (one Student),g: (one Group) {
((c->(s->g)) in Groups)
}))
})
}
pred inv11C[] {
(all c: (one Class) {
((some (c.Groups)) => (some (Teacher & (Teaches.c))))
})
}
pred overconstrained[] {
((inv11C[]) && (!(inv11[])))
}
pred underconstrained[] {
((!(inv11C[])) && (inv11[]))
}
pred both[] {
((inv11C[]) && (inv11[]))
}



run overconstrained
run underconstrained
run both
