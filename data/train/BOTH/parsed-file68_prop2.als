module unknown
open util/integer [] as integer
sig Track {
prox: (set Track),
signal: (lone Signal)
}
sig Junction extends Track {}
sig Entry in Track {}
sig Exit in Track {}
sig Signal {}
var sig Green in Signal {}
sig Train {
var pos: (lone Track)
}
pred prop2[] {
(eventually (all s: (one Signal) {
(eventually (Green in s))
}))
}
pred prop2C[] {
(all s: (one Signal) {
(eventually (s in Green))
})
}
pred overconstrained[] {
((prop2C[]) && (!(prop2[])))
}
pred underconstrained[] {
((!(prop2C[])) && (prop2[]))
}
pred both[] {
((prop2C[]) && (prop2[]))
}

fact Layout {
((all t: (one Track) {
((t !in Junction) <=> ((lone (t.prox)) && (lone (prox.t))))
}) && (no t: (one Track) {
(t in (t.(^prox)))
}) && (all s: (one Signal) {
(one (signal.s))
}) && (all j: (one Junction),t: (one (prox.j)) {
(some (t.signal))
}) && (all t: (one Track) {
((t in Entry) <=> (no (prox.t)))
}) && (all t: (one Track) {
((t in Exit) <=> (no (t.prox)))
}))
}

run overconstrained
run underconstrained
run both
