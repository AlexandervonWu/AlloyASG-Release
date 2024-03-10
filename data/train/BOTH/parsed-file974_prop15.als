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
pred prop15[] {
(always (some t: (one Train) {
(((no (t.pos)) => (eventually (some (t.pos)))) || ((t.pos) != (t.(pos'))))
}))
}
pred prop15C[] {
(all t: (one Train),p: (one Track) {
(!(eventually (always ((t.pos) = p))))
})
}
pred overconstrained[] {
((prop15C[]) && (!(prop15[])))
}
pred underconstrained[] {
((!(prop15C[])) && (prop15[]))
}
pred both[] {
((prop15C[]) && (prop15[]))
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
