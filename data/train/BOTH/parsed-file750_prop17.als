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
pred prop17[] {
((one ((Train.pos) :> Exit)) since (one pos))
}
pred prop17C[] {
(all t: (one Train) {
(always (((some (t.pos)) && (historically (no ((Train - t).pos)))) => ((no ((Train.pos) & Exit)) until (some ((t.pos) & Exit)))))
})
}
pred overconstrained[] {
((prop17C[]) && (!(prop17[])))
}
pred underconstrained[] {
((!(prop17C[])) && (prop17[]))
}
pred both[] {
((prop17C[]) && (prop17[]))
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
