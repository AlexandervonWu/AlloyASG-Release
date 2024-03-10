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
pred inv1[] {
(all p: (one Person) {
(p in Teacher)
})
}
pred inv1C[] {
(Person in Student)
}
pred overconstrained[] {
((inv1C[]) && (!(inv1[])))
}
pred underconstrained[] {
((!(inv1C[])) && (inv1[]))
}
pred both[] {
((inv1C[]) && (inv1[]))
}



run overconstrained
run underconstrained
run both
