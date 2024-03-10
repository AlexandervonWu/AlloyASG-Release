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
((((Person.Teaches).Teaches).Teaches) in Teacher)
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
