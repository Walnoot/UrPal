package nl.utwente.ewi.fmt.uppaalSMC.urpal.properties

import com.uppaal.model.core2.Document
import com.uppaal.model.core2.PrototypeDocument
import com.uppaal.model.system.UppaalSystem
import nl.utwente.ewi.fmt.uppaalSMC.NSTA
import nl.utwente.ewi.fmt.uppaalSMC.urpal.util.UppaalUtil
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@SanityCheck(name = "Deadlocks", shortName = "deadlock")
class DeadlockProperty : SafetyProperty() {
    override fun translateNSTA(nsta: NSTA, properties: Map<String, Any>): String {
        // compile UppaalSystem so we can iterate over every instantiated process
        val sys = UppaalUtil.compile(UppaalUtil.toDocument(nsta, Document(PrototypeDocument())))

        val locs = mutableListOf<String>()

        for (process in sys.processes) {
            // find corresponding NSTA template
            val template = nsta.template.find { it.name == process.template.getPropertyValue("name") }!!

            for (loc in template.location) {
                if (nsta.template.flatMap { it.edge }.none { e -> e.source == loc }) {
                    locs.add("${process.name}.${loc.name}")
                }
            }
        }

        val query = if (locs.isEmpty()) {
            "deadlock"
        } else {
            "deadlock and not (${locs.joinToString(" or ")})"
        }

        return query
    }

    fun doCheck(nsta: NSTA, doc: Document, sys: UppaalSystem, cb: (SanityCheckResult) -> Unit) {
        val cbs = HashSet<() -> Unit>()
        val i = AtomicInteger()
        val locs = sys.processes.flatMap { p ->
            p.locations.filter { l ->
                val result = p.edges.none { e -> e.edge.source == l.location }
                if (result && l.name.isNullOrEmpty()) {
                    l.location.setProperty("name", "__l${i.getAndIncrement()}")
                    cbs.add { l.location.setProperty("name", null) }
                }
                result
            }.map { "${p.name}.${it.name}" }
        }
        val query = if (locs.isEmpty()) {
            "A[] (!deadlock)"
        } else {
            "A[] (deadlock imply ${locs.joinToString(" or ")})"
        }
        UppaalUtil.reconnect()
//        engineQuery(sys, query, "trace 1") { qr, t ->
//            cbs.forEach { it() }
//            cb(object : SanityCheckResult() {
//                override fun quality() = (4.0 - qr.status) / 3.0
//                override fun getOutcome() = if (qr.status == QueryResult.OK) Outcome.SATISFIED else Outcome.VIOLATED
//
//                override fun write(out: PrintStream, err: PrintStream) {
//                    if (qr.status == QueryResult.OK) {
//                        out.println("No unwanted deadlocks found!")
//                    } else {
//                        err.println("Unwanted deadlocks found! See trace in the GUI:")
//
//                    }
//                }
//
//                override fun toPanel(): JPanel {
//                    val p = JPanel()
//                    p.layout = BoxLayout(p, BoxLayout.Y_AXIS)
//                    if (qr.status == QueryResult.OK) {
//                        val label = JLabel("No unwanted deadlocks!")
//                        p.add(label)
//                    } else {
//
//                        val label = JLabel("Unwanted deadlocks found! Click button below to load trace:")
//                        label.foreground = Color.RED
//                        p.add(label)
//                        val button = JButton("Load trace")
//                        button.addActionListener {
//                            MainUI.getSystemr().set(sys)
//                            MainUI.getTracer().set(UppaalUtil.transformTrace(t, sys))
//                        }
//                        p.add(button)
//                    }
//                    return p
//                }
//            })
//        }

    }

}
