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
pred prop8[] {
(all t: (one Train) {
(((some ((t.pos).signal)) && (((t.pos).signal) in Green)) => ((t.(pos')) = (t.pos)))
})
}
pred prop8C[] {
(all t: (one Train),p: (one Track) {
(always ((((t.pos) = p) && ((p.signal) !in Green)) => (((p.signal) in Green) releases ((t.pos) = p))))
})
}
pred overconstrained[] {
((prop8C[]) && (!(prop8[])))
}
pred underconstrained[] {
((!(prop8C[])) && (prop8[]))
}
pred both[] {
((prop8C[]) && (prop8[]))
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
