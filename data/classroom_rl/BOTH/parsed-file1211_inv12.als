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
pred inv12[] {
(some c: (one Class) {
((some p: (one Person),g: (one Group) {
((c->(p->g)) in Groups)
}) => (all t: (one Teacher) {
((t->c) in Teaches)
}))
})
}
pred inv12C[] {
(all t: (one Teacher) {
(some ((t.Teaches).Groups))
})
}
pred overconstrained[] {
((inv12C[]) && (!(inv12[])))
}
pred underconstrained[] {
((!(inv12C[])) && (inv12[]))
}
pred both[] {
((inv12C[]) && (inv12[]))
}



run overconstrained
run underconstrained
run both