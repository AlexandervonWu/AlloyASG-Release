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
pred inv3[] {
((all w: (one Person) {
((w in Student) => (w !in Teacher))
}) && (all w: (one Person) {
((w in Teacher) => (w !in Student))
}))
}
pred inv3C[] {
(no (Student & Teacher))
}
pred overconstrained[] {
((inv3C[]) && (!(inv3[])))
}
pred underconstrained[] {
((!(inv3C[])) && (inv3[]))
}
pred both[] {
((inv3C[]) && (inv3[]))
}



run overconstrained
run underconstrained
run both
