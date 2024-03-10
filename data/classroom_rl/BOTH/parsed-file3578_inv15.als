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
pred inv15[] {
(all s: (one Person) {
(all c: (one Class) {
(all g: (one Group) {
(((c->(s->g)) in Groups) => (all t: (one Teacher) {
(((t->c) in Teaches) => ((t->s) in Tutors))
}))
})
})
})
}
pred inv15C[] {
(all s: (one Person) {
(some (Teacher & ((^Tutors).s)))
})
}
pred overconstrained[] {
((inv15C[]) && (!(inv15[])))
}
pred underconstrained[] {
((!(inv15C[])) && (inv15[]))
}
pred both[] {
((inv15C[]) && (inv15[]))
}



run overconstrained
run underconstrained
run both
