package nl.utwente.ewi.fmt.uppaalSMC.urpal.properties

import java.io.PrintStream

import javax.swing.JPanel

class SanityCheckResult(val message: String, val satisfied: Boolean, val showTrace: (() -> Unit)?) {
}
