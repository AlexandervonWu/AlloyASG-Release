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
pred inv5[] {
(some c: (one Class),g: (one Group),t: (one Teacher) {
((c->(t->g)) in Groups)
})
}
pred inv5C[] {
(some (Teacher.Teaches))
}
pred overconstrained[] {
((inv5C[]) && (!(inv5[])))
}
pred underconstrained[] {
((!(inv5C[])) && (inv5[]))
}
pred both[] {
((inv5C[]) && (inv5[]))
}



run overconstrained
run underconstrained
run both
