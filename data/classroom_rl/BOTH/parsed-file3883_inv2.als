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
pred inv2[] {
(Group !in Teacher)
}
pred inv2C[] {
(no Teacher)
}
pred overconstrained[] {
((inv2C[]) && (!(inv2[])))
}
pred underconstrained[] {
((!(inv2C[])) && (inv2[]))
}
pred both[] {
((inv2C[]) && (inv2[]))
}



run overconstrained
run underconstrained
run both
