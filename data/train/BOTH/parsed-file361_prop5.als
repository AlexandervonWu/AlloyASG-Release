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
pred prop5[] {
(all t: (one Train) {
((t.(pos')) in ((t.pos).prox))
})
}
pred prop5C[] {
(all t: (one Train) {
(always ((some (t.pos)) => (((t.(pos')) = (t.pos)) || (((t.pos) in Exit) => (no (t.(pos'))) else ((some (t.(pos'))) && ((t.(pos')) in ((t.pos).prox)))))))
})
}
pred overconstrained[] {
((prop5C[]) && (!(prop5[])))
}
pred underconstrained[] {
((!(prop5C[])) && (prop5[]))
}
pred both[] {
((prop5C[]) && (prop5[]))
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
