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
pred prop13[] {
(always (all t: (one Train) {
(((before (once (some (t.pos)))) && (no (t.pos))) => (always (no (t.pos))))
}))
}
pred prop13C[] {
(all t: (one Train) {
(always (((no (t.pos)) && (once (some (t.pos)))) => (always (no (t.pos)))))
})
}
pred overconstrained[] {
((prop13C[]) && (!(prop13[])))
}
pred underconstrained[] {
((!(prop13C[])) && (prop13[]))
}
pred both[] {
((prop13C[]) && (prop13[]))
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
