package nl.utwente.ewi.fmt.uppaalSMC.urpal.properties

import com.uppaal.model.system.UppaalSystem
import nl.utwente.ewi.fmt.uppaalSMC.NSTA
import nl.utwente.ewi.fmt.uppaalSMC.urpal.util.UppaalUtil
import org.eclipse.emf.ecore.EObject
import org.muml.uppaal.declarations.*
import org.muml.uppaal.expressions.IdentifierExpression
import org.muml.uppaal.expressions.LogicalExpression
import org.muml.uppaal.expressions.LogicalOperator

@SanityCheck(name = "Symbolic Query", shortName = "symbolic")
class SymbolicProperty : SafetyProperty() {
    override fun translateNSTA(nsta: NSTA, properties: Map<String, Any>): String {
        return properties["condition"] as String
    }
}