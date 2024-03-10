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
pred inv14[] {
(all c: (one Class),p1,p2: (one Person) {
(((p1->p2) in Tutors) => ((p1 in Teacher) && (p2 in Student) && (p2 !in Teacher) && (p1 !in Student) && ((p1->c) in Teaches)))
})
}
pred inv14C[] {
(all s: (one Person),c: (one Class),t: (one Person),g: (one Group) {
((((c->(s->g)) in Groups) && ((t->c) in Teaches)) => ((t->s) in Tutors))
})
}
pred overconstrained[] {
((inv14C[]) && (!(inv14[])))
}
pred underconstrained[] {
((!(inv14C[])) && (inv14[]))
}
pred both[] {
((inv14C[]) && (inv14[]))
}



run overconstrained
run underconstrained
run both
