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
(all c: (one Class) {
(some g: (one Group) {
(all t,p: (one Person) {
((((c->(p->g)) in Groups) && ((c->(t->g)) in Groups)) => (((t->c) in Teaches) && (t in Teacher)))
})
})
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
