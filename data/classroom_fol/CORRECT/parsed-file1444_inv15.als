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
(all p1: (one Person) {
((some p2: (one Teacher) {
((p2->p1) in Tutors)
}) || (some p2,p3: (one Person) {
(((p2->p1) in Tutors) && ((p3->p2) in Tutors) && (p3 in Teacher))
}) || (some p2,p3,p4: (one Person) {
(((p2->p1) in Tutors) && ((p3->p2) in Tutors) && ((p4->p3) in Tutors) && (p4 in Teacher))
}))
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
