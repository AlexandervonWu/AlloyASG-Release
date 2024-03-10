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
pred prop10[] {
(always (all j: (one Junction) {
(lone (((prox.j).signal) :> Green))
}))
}
pred prop10C[] {
(all j: (one Junction) {
(always (lone (((prox.j).signal) & Green)))
})
}
pred overconstrained[] {
((prop10C[]) && (!(prop10[])))
}
pred underconstrained[] {
((!(prop10C[])) && (prop10[]))
}
pred both[] {
((prop10C[]) && (prop10[]))
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
