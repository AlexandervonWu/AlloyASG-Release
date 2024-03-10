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
pred inv4[] {
((always (all s: (one Student) {
(s !in Teacher)
})) && (always (all t: (one Teacher) {
(t !in Student)
})))
}
pred inv4C[] {
(Person in (Student + Teacher))
}
pred overconstrained[] {
((inv4C[]) && (!(inv4[])))
}
pred underconstrained[] {
((!(inv4C[])) && (inv4[]))
}
pred both[] {
((inv4C[]) && (inv4[]))
}



run overconstrained
run underconstrained
run both
