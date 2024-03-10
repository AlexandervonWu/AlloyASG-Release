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
pred inv10[] {
(all c: (one Class) {
(Student = (c.(Groups.Group)))
})
}
pred inv10C[] {
(all c: (one Class),s: (one Student) {
(some (s.(c.Groups)))
})
}
pred overconstrained[] {
((inv10C[]) && (!(inv10[])))
}
pred underconstrained[] {
((!(inv10C[])) && (inv10[]))
}
pred both[] {
((inv10C[]) && (inv10[]))
}



run overconstrained
run underconstrained
run both
