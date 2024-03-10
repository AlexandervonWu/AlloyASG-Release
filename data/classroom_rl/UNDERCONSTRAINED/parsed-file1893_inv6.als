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
pred inv6[] {
(all p1,p2: (one Person) {
(((p1->p2) in Teaches) => (p1 in Teacher))
})
}
pred inv6C[] {
(Teacher in (Teaches.Class))
}
pred overconstrained[] {
((inv6C[]) && (!(inv6[])))
}
pred underconstrained[] {
((!(inv6C[])) && (inv6[]))
}
pred both[] {
((inv6C[]) && (inv6[]))
}



run overconstrained
run underconstrained
run both
