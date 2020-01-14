package nl.utwente.ewi.fmt.uppaalSMC.urpal.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.uppaal.model.core2.PrototypeDocument
import com.uppaal.model.io2.XMLReader
import nl.utwente.ewi.fmt.uppaalSMC.urpal.util.UppaalUtil
import nl.utwente.ewi.fmt.uppaalSMC.urpal.util.ValidationSpec
import org.apache.commons.io.input.CharSequenceInputStream
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.FileReader
import kotlin.system.exitProcess


class Application : CliktCommand() {
    val input by argument(help="Input Uppaal model").file(exists = true, folderOkay = false, readable = true)
    private val spec by argument(help="Input Specification").file(exists = true, folderOkay = false, readable = true)

    override fun run() {
        val xml = input.readText().replace(Regex("<!DOCTYPE.*>"), "")
        val doc = XMLReader(CharSequenceInputStream(xml, "UTF-8")).parse(PrototypeDocument())

        ValidationSpec(spec.readText()).check(doc).forEach { println(it.message) }

        UppaalUtil.engine.cancel()
        exitProcess(0)
    }
}
fun main(args: Array<String>) = Application().main(args)