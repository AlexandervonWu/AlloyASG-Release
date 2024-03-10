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
pred prop14[] {
(always (all t: (one Train) {
((one (((t.pos).signal) :> Green)) => (eventually (((t.pos).signal) in (Signal - Green))))
}))
}
pred prop14C[] {
(all s: (one Signal),t: (one Train) {
(always (((s in Green) && ((t.pos) = (signal.s)) && ((t.(pos')) != (signal.s))) => (after (s !in Green))))
})
}
pred overconstrained[] {
((prop14C[]) && (!(prop14[])))
}
pred underconstrained[] {
((!(prop14C[])) && (prop14[]))
}
pred both[] {
((prop14C[]) && (prop14[]))
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
