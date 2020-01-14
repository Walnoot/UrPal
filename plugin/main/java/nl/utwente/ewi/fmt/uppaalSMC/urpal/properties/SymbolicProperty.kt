package nl.utwente.ewi.fmt.uppaalSMC.urpal.properties

import nl.utwente.ewi.fmt.uppaalSMC.NSTA

@SanityCheck(name = "Symbolic Query", shortName = "symbolic")
class SymbolicProperty : SafetyProperty() {
    override fun translateNSTA(nsta: NSTA, properties: Map<String, Any>): String {
        return properties["condition"] as String
    }
}