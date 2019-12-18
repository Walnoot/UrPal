package nl.utwente.ewi.fmt.uppaalSMC.urpal.properties

import com.uppaal.model.core2.AbstractTemplate
import com.uppaal.model.core2.Document
import com.uppaal.model.core2.PrototypeDocument
import com.uppaal.model.io2.XMLReader
import com.uppaal.model.system.UppaalSystem
import nl.utwente.ewi.fmt.uppaalSMC.NSTA
import nl.utwente.ewi.fmt.uppaalSMC.Serialization
import nl.utwente.ewi.fmt.uppaalSMC.UppaalSMCFactory
import nl.utwente.ewi.fmt.uppaalSMC.parser.parser.antlr.UppaalSMCParser
import nl.utwente.ewi.fmt.uppaalSMC.urpal.ui.MainUI
import nl.utwente.ewi.fmt.uppaalSMC.urpal.util.UppaalUtil
import org.apache.commons.io.input.CharSequenceInputStream
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.common.services.Ecore2XtextTerminalConverters
import org.muml.uppaal.UppaalFactory
import org.muml.uppaal.declarations.*
import org.muml.uppaal.declarations.impl.ExpressionInitializerImpl
import org.muml.uppaal.expressions.IdentifierExpression
import org.muml.uppaal.expressions.LogicalExpression
import org.muml.uppaal.expressions.LogicalOperator
import org.muml.uppaal.serialization.UppaalSerialization
import org.muml.uppaal.templates.TemplatesFactory
import org.muml.uppaal.types.TypeSpecification
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JTextField

@SanityCheck(name = "Symbolic Query", shortName = "symbolic")
class SymbolicProperty : AbstractProperty() {
    lateinit var queryField: JTextField

    override fun doCheck(nsta: NSTA, doc: Document, sys: UppaalSystem, cb: (SanityCheckResult) -> Unit) {
//        println(nsta.template.flatMap { it.location }.map { it.name }.joinToString(", "))

        // hide all clocks whose clock rate is variable in one or more locations
        // ignore name space issues for now (variables in templates with the same name as a global variable)
        val hideVars = mutableSetOf<String>()
        nsta.eAllContents().forEach {
            if (it is IdentifierExpression && it.isClockRate) {
                hideVars.add(it.identifier.name)
            }

            if (it is VariableDeclaration && it.typeDefinition.baseType == nsta.double.baseType) {
                println("found double")
                println(it)
                println(it.variable.joinToString { it.name })
                it.variable.forEach { hideVars.add(it.name) }
            }
        }

        println(hideVars.size)
        println(hideVars.joinToString(", "))

        // hide vars in invariants
        nsta.template.flatMap { it.location }.forEach {
            if (it.invariant != null && removeVarReference(it.invariant, hideVars)) {
//                println("remove invariant in $it")
                it.invariant = UppaalUtil.createLiteral("true")
            }
        }

        // hide vars in guards, update
        nsta.template.flatMap { it.edge }.forEach {
            if (it.guard != null && removeVarReference(it.guard, hideVars)) {
//                println("remove guard in $it")
                it.guard = UppaalUtil.createLiteral("true")
            }

            if (it.update != null) {
                for (update in it.update.toList()) {
                    if(removeVarReference(update, hideVars)) {
                        it.update.remove(update)
                    }
                }
            }
        }

        // hide vars in global and template declarations
        hideDeclaration(nsta.globalDeclarations, hideVars)
        nsta.template.forEach {
            if (it.declarations != null) {
                hideDeclaration(it.declarations, hideVars)
            }
        }

//        nsta.globalDeclarations.declaration
//                .filterIsInstance<DataVariableDeclaration>()
//                .filter { it.prefix == DataVariablePrefix.CONST }
//                .flatMap { it.variable }
//                .map { it.initializer }
//                .filterIsInstance<ExpressionInitializer>()
//                .forEach { it.expression = UppaalUtil.createLiteral("7") }

        val proto = PrototypeDocument()
        proto.setProperty("synchronization", "")
        var xml = Serialization().main(nsta).toString().replace(
                "http://www.it.uu.se/research/group/darts/uppaal/flat-1_1.dtd",
                "http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd")
//        println(xml)

        val tDoc = XMLReader(CharSequenceInputStream(xml, "UTF-8")).parse(proto)

        translateDocument(tDoc, nsta, sys)

        val tSys = UppaalUtil.compile(tDoc)

        UppaalUtil.reconnect()

//        val query = translateQuery(queryField.text, nsta, sys)
        val query = queryField.text
        println(query)

        engineQuery(tSys, query, "trace 1") { qr, t ->
            println(qr)
            println(qr.exception)

            if (!t.isEmpty) {
//                MainUI.getTracer().set(UppaalUtil.transformTrace(t, sys))
            }
        }
    }

    override fun addToPanel (panel: JPanel) {
        queryField = JTextField("A[] (not deadlock)")
        queryField.preferredSize = Dimension(500, queryField.preferredSize.height)
        panel.add(queryField)
    }

    private fun translateQuery(query: String, nsta: NSTA, sys: UppaalSystem): String {
        var newQuery = query

        for (template in nsta.template) {
            for (process in sys.processes) {
                if (process.template.getPropertyValue("name") == template.name) {
                    for (i in process.locations.indices) {
                        val lname = template.name
                        val pname = process.locations[i].name
                        val tname = template.location[i].name

                        if (!pname.isNullOrEmpty()) {
                            // only replace location names, might cause issues
                            newQuery = newQuery.replace("$pname", "$tname")
//                            newQuery = newQuery.replace("$lname.$pname", "$lname.$tname")
                        }
                    }

                    break
                }
            }
        }

        return newQuery
    }

    private fun translateDocument(doc: Document, nsta: NSTA, sys: UppaalSystem) {
        for (template in nsta.template) {
            for (process in sys.processes) {
                if (process.template.getPropertyValue("name") == template.name) {
                    for (i in process.locations.indices) {
                        val lname = template.name
                        val pname = process.locations[i].name
                        val tname = template.location[i].name

                        if (!pname.isNullOrEmpty()) {
                            var loc = doc.getTemplate(lname).first
                            while (loc != null) {
                                if (loc.getPropertyValue("name") == tname) {
                                    //println("found loc in document")
                                    println("orig=$pname, trans=$tname")
                                    loc.setProperty("name", pname)
                                    break
                                }
                                loc = loc.next
                            }
                        }
                    }

                    break
                }
            }
        }
    }

    private fun removeVarReference (e: EObject, hideVars: Collection<String>): Boolean {
        if (e is IdentifierExpression && e.identifier.name in hideVars) {
//            println("found hidden var")
            return true
        }

        if (e is LogicalExpression) {
            if (removeVarReference(e.firstExpr, hideVars)) {
//                println("change logical expr")

                e.operator = LogicalOperator.AND
                e.firstExpr = UppaalUtil.createLiteral("true")
            }

            if (removeVarReference(e.secondExpr, hideVars)) {
//                println("change logical expr")

                e.operator = LogicalOperator.AND
                e.secondExpr = UppaalUtil.createLiteral("true")
            }
        } else {
            e.eAllContents().forEach {
                if (removeVarReference(it, hideVars)) {
//                    println("hidden var in child")
                    return true
                }
            }
        }

        return false
    }

    private fun hideDeclaration(declarations: Declarations, hideVars: Collection<String>) {
        declarations.declaration.toList().filterIsInstance<VariableDeclaration>().forEach {
            for (v in it.variable.toList()) {
                if (v.name in hideVars) {
                    it.variable.remove(v)
                }
            }

            if (it.variable.isEmpty()) {
                declarations.declaration.remove(it)
            }
        }
    }
}