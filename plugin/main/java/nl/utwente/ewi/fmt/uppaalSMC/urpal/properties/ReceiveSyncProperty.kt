package nl.utwente.ewi.fmt.uppaalSMC.urpal.properties

import com.uppaal.model.core2.AbstractTemplate
import com.uppaal.model.core2.Document
import com.uppaal.model.core2.PrototypeDocument
import com.uppaal.model.system.SystemEdgeSelect
import com.uppaal.model.system.UppaalSystem
import com.uppaal.model.system.concrete.*

import nl.utwente.ewi.fmt.uppaalSMC.NSTA
import nl.utwente.ewi.fmt.uppaalSMC.urpal.ui.MainUI
import nl.utwente.ewi.fmt.uppaalSMC.urpal.util.UppaalUtil
import org.muml.uppaal.declarations.ChannelVariableDeclaration
import org.muml.uppaal.declarations.DeclarationsFactory
import org.muml.uppaal.expressions.AssignmentOperator
import org.muml.uppaal.expressions.CompareOperator
import org.muml.uppaal.expressions.ExpressionsFactory
import org.muml.uppaal.expressions.IncrementDecrementOperator
import org.muml.uppaal.templates.LocationKind
import org.muml.uppaal.templates.SynchronizationKind
import org.muml.uppaal.templates.TemplatesFactory
import org.muml.uppaal.types.TypesFactory
import java.math.BigDecimal

@SanityCheck(name = "Receive Syncs", shortName = "receivesyncs")
class ReceiveSyncProperty : SafetyProperty() {
//	private var channelsPanel: JPanel? = null
//
//	private val checkedChannels: MutableList<Pair<JComboBox<String>, JComboBox<String>>> = mutableListOf()
//
//    override fun doCheck(nsta: NSTA, doc: Document, origSys: UppaalSystem, cb: (SanityCheckResult) -> Unit) {
//		val (restoreFun, query) = transformModel(nsta, doc)
//
//		try {
//			UppaalUtil.reconnect()
//			engineQueryConcrete(MainUI.getSystemr().get(), null, query.formula, "trace 1") {qr, _ ->
//				println(qr)
//
//				val success = qr.status == QueryResult.NOT_OK
//
//				cb(object : SanityCheckResult() {
//					override fun quality() = (4.0 - qr.status) / 3.0
//					override fun getOutcome() = if (success) Outcome.SATISFIED else Outcome.VIOLATED
//
//					override fun write(out: PrintStream, err: PrintStream) {
//						if (qr.status == QueryResult.OK) {
//							out.println("No unwanted deadlocks found!")
//						} else {
//							err.println("Unwanted deadlocks found! See trace in the GUI:")
//						}
//					}
//
//					override fun toPanel(): JPanel {
//						val p = JPanel()
//						p.layout = BoxLayout(p, BoxLayout.Y_AXIS)
//						if (success) {
//							val label = JLabel("All synchronisations handled.")
//							p.add(label)
//						} else {
//							val message: String
//							if (qr.exception == null) {
//								message = "Synchronisation not always handled."
//							} else {
//								message = "Runtime error: ${qr.exception}"
//							}
//
//							val label = JLabel(message)
//							label.foreground = Color.RED
//							p.add(label)
//							val button = JButton("Load trace")
//							button.addActionListener {
//								val (restoreFun, query) = transformModel(nsta, doc)
//								runQueryGUI()
//								restoreFun.invoke()
//							}
//							p.add(button)
//						}
//						return p
//					}
//				})
//			}
//		} catch (e: Exception) {
//			e.printStackTrace()
//		} finally {
//			// restore Document and UppaalSystem to original state
//			if (stateSpaceSize != -1) {
//				// temp setting to disable restoring template
//				restoreFun.invoke()
//			}
//		}
//    }

//	private fun transformModel (nsta: NSTA, doc: Document): Pair<() -> Unit, Query> {
//		val restoreFuns = HashSet<() -> Unit>()
//
//		// list of all template/channel pairs that are checked
//		val checks = checkedChannels.map { Pair(it.first.selectedItem as String, it.second.selectedItem  as String) }
//
////		val channels = nsta.globalDeclarations.declaration
////				.filterIsInstance<ChannelVariableDeclaration>()
////				.flatMap { it.variable }
////				.map { it.name }
////				.filter { it in selectedChannels }
//
//		// maps from template/channel pair to counter variable name
//		val receivedVars = mutableMapOf<Pair<String, String>, String>()
//		val sentVars = mutableMapOf<Pair<String, String>, String>()
//
//		// add counter variables for every check
//		var decl = ""
//		for (c in checks) {
//			val receivedVar = "__received_${c.first}_${c.second}"
//			val sentVar = "__sent_${c.first}_${c.second}"
//
//			receivedVars[c] = receivedVar
//			sentVars[c] = sentVar
//
//			decl += "\nint $receivedVar, $sentVar;"
////			decl += "\nmeta int $receivedVar, $sentVar;"
//		}
//
//		val declVal = doc.properties.find { it.key == "declaration" }?.value
//		if (declVal != null) {
//			val origDecl = (declVal.value as String)
//
//			restoreFuns.add { doc.setProperty("declaration", origDecl) }
//			doc.setProperty("declaration", origDecl + decl)
//		}
//
//		val simTime = simTime
//		val numSims = 1000
//
//		// the condition that is true iff all synchronisations are handled in every process, i.e. the number of syncs
//		// sent equals the number of syncs received
//		val cond =  checks
//				.map { "(${receivedVars[it]} != ${sentVars[it]})" }
//				.joinToString(" or ")
//
//		val queryStr = "simulate [<=$simTime;$numSims] {1} :1: (${if (checks.isNotEmpty()) cond else "false"})"
//		val query = Query(queryStr, "Automatically generated query.")
//
//		// add query to the query list so it can be checked in the GUI
//		doc.queryList.addLast(query)
//		restoreFuns.add { doc.queryList.remove(doc.queryList.size() - 1) }
//
//		val sys = UppaalUtil.compile(doc)
//
//		val processes = mutableListOf<Process>()
//		val processCount = mutableMapOf<AbstractTemplate, Int>()
//
//		// collect all processes, count number of instances
//		var prevTemplate: AbstractTemplate? = null
//		for (p in sys.processes) {
//			if (p.template != prevTemplate) {
//				prevTemplate = p.template
//				processes.add(p)
//				processCount[p.template] = 1
//			} else {
//				processCount[p.template] = processCount[p.template]!! + 1
//			}
//		}
//
//		for (p in processes) {
//			for (edge in p.edges.map { it.edge }) {
//				val sync = edge.getPropertyValue("synchronisation")
//				if (sync != null && sync is String) {
//					val isSend = sync.endsWith("!")
//					val isReceive = sync.endsWith("?")
//
//					if (isSend || isReceive) {
//						// strip '!' or '?' from synchronisation to get channel
//						val syncChannel = sync.substring(0, sync.length - 1)
//
//						// get template name
//						val tname = edge.template.getPropertyValue("name") as String
//
//						if (syncChannel in checks.map { it.first }) {
//							if (isSend) {
//								for (c in checks.filter { it.first == syncChannel }) {
//									var count: Int? = null
//									for ((t, tcount) in processCount) {
//										if (t.getPropertyValue("name") == c.second) {
//											count = tcount
//											break
//										}
//									}
//
//									// add sent counter increment to edge update
//									val update = "${sentVars[c]} += ${count!!}"
//
//									val currentUpdate = edge.getPropertyValue("assignment") as String
//									val addition = if (currentUpdate.isEmpty()) update else ",\n$update"
//									edge.setProperty("assignment", currentUpdate + addition)
//
//									restoreFuns.add { edge.setProperty("assignment", currentUpdate) }
//								}
//							} else {
//								if (tname in checks.map { it.second }) {
//									println("Adding receive update in $tname, $syncChannel")
//									val update = "${receivedVars[Pair(syncChannel, tname)]}++"
//
//									val currentUpdate = edge.getPropertyValue("assignment") as String
//									val addition = if (currentUpdate.isEmpty()) update else ",\n$update"
//									edge.setProperty("assignment", currentUpdate + addition)
//
//									restoreFuns.add { edge.setProperty("assignment", currentUpdate) }
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//
////		MainUI.getDocument().fire(Repository.ChangeType.UPDATED)
//
//		println(queryStr)
//
//		// update loaded system
//		MainUI.getSystemr().set(sys)
//
//		// pack restore functions into one for convenience
//		val restoreFun = {restoreFuns.forEach { it() }}
//
//		return Pair(restoreFun, query)
//	}
//
//	private fun runQueryGUI () {
//		var tabPane: JTabbedPane? = null
//
//		// check parents of added panel to find the UPPAAL tab pane
//		var curr: Container? = channelsPanel?.parent
//		while (curr != null) {
//			if (curr is JTabbedPane) {
//				tabPane = curr
//				break
//			}
//
//			curr = curr.parent
//		}
//
//		if (tabPane != null) {
//			findQueryList(tabPane)
//
//			var button = findButton(tabPane, "Get Trace")
//
//			if (button != null) {
//				button.doClick()
//			} else {
//				println("Could not find button")
//			}
//		} else {
//			println("Could not find tab pane")
//		}
//	}
//
//	private fun findButton (container: Container, name: String): JButton? {
//		for (comp in iterateContainer(container)) {
//			if (comp is JButton && comp.text == name) {
//				return comp
//			}
//		}
//
//		return null
//	}
//
//	private fun findQueryList (container: Container) {
//		for (comp in iterateContainer(container)) {
//			if (comp is JList<*>) {
//				if (comp.model.size > 0 && comp.model.getElementAt(0) is Query) {
//					// select last element, which should be the query we added
//					comp.selectedIndex = comp.model.size - 1
//				}
//			}
//		}
//	}
//
//	private fun iterateContainer (container: Container): Sequence<Component> {
//		return sequence {
//			for (comp in container.components) {
//				yield(comp)
//
//				if (comp is Container) {
//					yieldAll(iterateContainer(comp))
//				}
//			}
//		}
//	}
//
//	override fun addToPanel (panel: JPanel) {
//		val channelsPanel = JPanel()
//		this.channelsPanel = channelsPanel
//		channelsPanel.layout = GridLayout(0, 1)
//
//		panel.add(channelsPanel)
//
//		val addChannelButton = JButton("Add channel")
//
//		addChannelButton.addActionListener {
//			val channelPanel = JPanel(FlowLayout(FlowLayout.LEFT))
//			channelPanel.border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)
//
//			channelPanel.add(JLabel("Channel:"))
//
//			val channelBox = JComboBox<String>(getChannels().toTypedArray())
//
//			MainUI.getSystemr().addListener {
//				println("system updated")
//				val selected = channelBox.selectedItem
//				channelBox.model = DefaultComboBoxModel<String>(getChannels().toTypedArray())
//				channelBox.selectedItem = selected
//			}
//
//			channelBox.minimumSize = Dimension(50, channelBox.minimumSize.height)
//			channelPanel.add(channelBox)
//
//			channelPanel.add(JLabel("Process:"))
//
//			val templateBox = JComboBox<String>()
//			getChannelTemplates(channelBox.selectedItem as String).forEach { templateBox.addItem(it) }
//
//			channelBox.addActionListener {
//				if (channelBox.selectedItem != null) {
//					val newItems = getChannelTemplates(channelBox.selectedItem as String).toTypedArray()
//					var changed = false
//					for (i in newItems.indices) {
//						if (newItems.size != templateBox.itemCount || newItems[i] != templateBox.getItemAt(i)) {
//							changed = true
//						}
//					}
//
//					if (changed) {
//						val selected = templateBox.selectedItem
//						templateBox.removeAllItems()
//						newItems.forEach { templateBox.addItem(it) }
//						templateBox.selectedItem = selected
//					}
//				}
//			}
//
//			channelPanel.add(templateBox)
//
//			val p = Pair(channelBox, templateBox)
//			checkedChannels.add(p)
//
//			val removeButton = JButton("Remove")
//			removeButton.addActionListener {
//				channelsPanel.remove(channelPanel)
//				channelsPanel.revalidate()
//
//				checkedChannels.remove(p)
//			}
//			channelPanel.add(removeButton)
//
//			channelsPanel.add(channelPanel)
//			channelsPanel.revalidate()
//		}
//
//		addChannelButton.size = Dimension(100, 40)
//		addChannelButton.alignmentX = Component.LEFT_ALIGNMENT
//		panel.add(addChannelButton)
//	}

    override fun translateNSTA(nsta: NSTA, properties: Map<String, Any>): String {
		val sys = UppaalUtil.compile(UppaalUtil.toDocument(nsta, Document(PrototypeDocument())))

        val checkedChannel = properties["channel"] as String
		val checkedTemplate = properties["template"] as String

        val dvd = DeclarationsFactory.eINSTANCE.createDataVariableDeclaration()
//        dvd.prefix = DataVariablePrefix.META
        val ref = TypesFactory.eINSTANCE.createTypeReference()
        ref.referredType = nsta.int
        dvd.typeDefinition = ref

        val receivedVar = UppaalUtil.createVariable("__received_${checkedChannel}")
        dvd.variable.add(receivedVar)

        val sentVar = UppaalUtil.createVariable("__sent_${checkedChannel}")
        dvd.variable.add(sentVar)

        nsta.globalDeclarations.declaration.add(dvd)

		val processCount = mutableMapOf<String, Int>()

		// collect all processes, count number of instances
		var prevTemplate: AbstractTemplate? = null
		for (p in sys.processes) {
			val name = p.template.getPropertyValue("name") as String
			if (p.template != prevTemplate) {
				prevTemplate = p.template
				processCount[name] = 1
			} else {
				processCount[name] = processCount[name]!! + 1
			}
		}

		for (template in nsta.template) {
			for (edge in template.edge) {
				if (edge.synchronization != null && edge.synchronization.channelExpression.identifier.name == checkedChannel) {
					if (edge.synchronization.kind == SynchronizationKind.RECEIVE) {
						if (edge.parentTemplate.name == checkedTemplate) {
							val increment = ExpressionsFactory.eINSTANCE.createPostIncrementDecrementExpression()
							increment.operator = IncrementDecrementOperator.INCREMENT
							increment.expression = UppaalUtil.createIdentifier(receivedVar)

							edge.update.add(increment)
						}
					} else {
						val assignment = ExpressionsFactory.eINSTANCE.createAssignmentExpression()
						assignment.operator = AssignmentOperator.PLUS_EQUAL
						assignment.firstExpr = UppaalUtil.createIdentifier(sentVar)
						assignment.secondExpr = UppaalUtil.createLiteral(processCount[checkedTemplate].toString())

						edge.update.add(assignment)
					}
				}
			}
		}

		val cd = DeclarationsFactory.eINSTANCE.createChannelVariableDeclaration()
		val chanType = TypesFactory.eINSTANCE.createTypeReference()
		chanType.referredType = nsta.chan
		cd.typeDefinition = chanType
		cd.isBroadcast = true
		cd.isUrgent = true

		val urgentClockVar = UppaalUtil.createVariable("__synccheck_clock_urgent")
		cd.variable.add(urgentClockVar)

		nsta.globalDeclarations.declaration.add(cd)

		val templateName = "__synccheck"
		val template = UppaalUtil.createTemplate(nsta, templateName)
		nsta.systemDeclarations.system.instantiationList[0].template.add(template)

		val startLoc = UppaalUtil.createLocation(template, "Start")
		template.init = startLoc

		val errorLocName = "Error"
		val errorLoc = UppaalUtil.createLocation(template, errorLocName)

		errorLoc.locationTimeKind = LocationKind.URGENT

		val errorEdge = UppaalUtil.createEdge(startLoc, errorLoc)

		val errorGuard = ExpressionsFactory.eINSTANCE.createCompareExpression()
		errorGuard.operator = CompareOperator.UNEQUAL
		errorGuard.firstExpr = UppaalUtil.createIdentifier(receivedVar)
		errorGuard.secondExpr = UppaalUtil.createIdentifier(sentVar)
		errorEdge.guard = errorGuard

		val sync = TemplatesFactory.eINSTANCE.createSynchronization()
		sync.channelExpression = UppaalUtil.createIdentifier(urgentClockVar)
		sync.kind = SynchronizationKind.SEND
		errorEdge.synchronization = sync

		val resetEdge = UppaalUtil.createEdge(errorLoc, startLoc)

		val resetReceived = ExpressionsFactory.eINSTANCE.createAssignmentExpression()
		resetReceived.operator = AssignmentOperator.EQUAL
		resetReceived.firstExpr = UppaalUtil.createIdentifier(receivedVar)
		resetReceived.secondExpr = UppaalUtil.createLiteral("0")

		resetEdge.update.add(resetReceived)

		val resetSent = ExpressionsFactory.eINSTANCE.createAssignmentExpression()
		resetSent.operator = AssignmentOperator.EQUAL
		resetSent.firstExpr = UppaalUtil.createIdentifier(sentVar)
		resetSent.secondExpr = UppaalUtil.createLiteral("0")

		resetEdge.update.add(resetSent)

//        val simTime = 66
//        val numSims = 1000
//        val query = "simulate [<=$simTime;$numSims] {1} :1: ($cond)"

		val cond = properties.getOrDefault("condition", "false") as String

		return  "$templateName.$errorLocName and !($cond)"

//		return "${receivedVar.name} != ${sentVar.name}";
//		return "(${receivedVar.name} != ${sentVar.name}) and !($cond)";
    }

	//	private fun translateSystem (nsta: NSTA, sys: UppaalSystem, cb: (SanityCheckResult) -> Unit) {
//		// alternate way of translating the model, using UPPAALEMF nsta model. Not used because loading concrete traces
//		// from the translated model seems not to be supported.
//
//		println("translating nsta")
//
//		var receivedVars = mutableMapOf<String, Variable>()
//		var sentVars = mutableMapOf<String, Variable>()
//
//		val channels = nsta.globalDeclarations.declaration
//				.filterIsInstance<ChannelVariableDeclaration>()
//				.flatMap { it.variable }
//				.map { it.name }
//
//		for (chan in channels) {
//			val dvd = DeclarationsFactory.eINSTANCE.createDataVariableDeclaration()
//			dvd.prefix = DataVariablePrefix.META
//			val ref = TypesFactory.eINSTANCE.createTypeReference()
//			ref.referredType = nsta.int
//			dvd.typeDefinition = ref
//
//			val receivedVar = UppaalUtil.createVariable("__received_${chan}")
//			dvd.variable.add(receivedVar)
//			receivedVars[chan] = receivedVar
//
//			val sentVar = UppaalUtil.createVariable("__sent_${chan}")
//			dvd.variable.add(sentVar)
//			sentVars[chan] = sentVar
//
//			nsta.globalDeclarations.declaration.add(dvd)
//		}
//
//		nsta.template
//				.flatMap { it.edge } // for every edge
//				.filter { it.synchronization != null } // that does some kind of sync
//				.forEach {// increment the global variable that tracks the send/receive count
//					val inc = ExpressionsFactory.eINSTANCE.createPostIncrementDecrementExpression()
//					inc.operator = IncrementDecrementOperator.INCREMENT
//
//					var id = ExpressionsFactory.eINSTANCE.createIdentifierExpression()
//					if (it.synchronization.kind == SynchronizationKind.RECEIVE) {
//						id.identifier = receivedVars[it.synchronization.channelExpression.identifier.name]
//					} else {
//						id.identifier = sentVars[it.synchronization.channelExpression.identifier.name]
//					}
//
//					inc.expression = id
//					it.update.add(inc)
//				}
//
//		val proto = PrototypeDocument()
//		proto.setProperty("synchronization", "")
//		var xml = Serialization().main(nsta).toString().replace(
//				"http://www.it.uu.se/research/group/darts/uppaal/flat-1_1.dtd",
//				"http://www.it.uu.se/research/group/darts/uppaal/flat-1_2.dtd")
//		println("$xml")
//		val tDoc = XMLReader(CharSequenceInputStream(xml, "UTF-8")).parse(proto)
//		val tSys = UppaalUtil.compile(tDoc)
//
//		val simTime = 100
//		val numSims = 1000
//
//		val cond =  channels
////				.filter { it in enteredChannels }
//				.map { "(${receivedVars[it]?.name} != ${sentVars[it]?.name})" }
//				.joinToString(" or ")
//
////		val query = "simulate [<=$simTime;$numSims] {1}"
//		val query = "simulate [<=$simTime;$numSims] {1} :1: (${if (channels.isNotEmpty()) cond else "false"})"
//
//		println(query)
//
//		engineQueryConcrete(tSys, null, query, "trace 1") {qr, t ->
//			println(qr)
//
//			val success = qr.status == QueryResult.NOT_OK;
//
//			var trans: String? = null
//			var state: String? = null
//
//			if (t != null) {
//				val tr = transformTrace(t, sys)
//				printTrace(tr)
//
//				var chan = t.last().edges[0].name
////				if (chan == null && t.last().edges.size > 0) {
////					chan = t.last().edges[1].name
////				}
//
//				val process = t.last().edges[0].process
//
//				val source = t[t.size() - 2].target.locations.filter { it.process == process }.map { it.name }.joinToString(", ")
//				val target = t[t.size() - 1].target.locations.filter { it.process == process }.map { it.name }.joinToString(", ")
//
//				val pname = process.name
//
//				trans = "$pname.$source -> $chan -> $pname.$target"
//				state = "(${t.last().target.locations.map { "${it.processName}.${it.name}" }.joinToString(", ")})"
//
//				println(t.last().edges.map { "${it.processName} ${it.name}" }.joinToString(", "))
//
//				println("Faulty channel: $chan")
//			} else {
//				println("no trace found")
//			}
//
//			cb(object : SanityCheckResult() {
//				override fun quality() = (4.0 - qr.status) / 3.0
//				override fun getOutcome() = if (success) Outcome.SATISFIED else Outcome.VIOLATED
//
//				override fun write(out: PrintStream, err: PrintStream) {
//					if (qr.status == QueryResult.OK) {
//						out.println("No unwanted deadlocks found!")
//					} else {
//						err.println("Unwanted deadlocks found! See trace in the GUI:")
//					}
//				}
//
//				override fun toPanel(): JPanel {
//					val p = JPanel()
//					p.layout = BoxLayout(p, BoxLayout.Y_AXIS)
//					if (success) {
//						val label = JLabel("All synchronisations handled.")
//						p.add(label)
//					} else {
//						val label = JLabel("Synchronisation $trans not handled in state $state.")
//						label.foreground = Color.RED
//						p.add(label)
//					}
//					return p
//				}
//			})
//		}
//	}

	private fun printTrace(t: ConcreteTrace) {
		println(t.size())
		println(t
				.map { trans ->
					"${trans.transitionDescription} " +
						"${trans.target.locations.map { it.name }.joinToString(", ")} " +
						"${trans.target.cVariables.map { it.getValue(trans.delay) }.joinToString(", ")} " +
						"${trans?.edges?.map { it.selectList.toString() }?.joinToString(", ")} " +
						"%.2f".format(trans.delay) }
				.joinToString("\n")
		)
	}

	private fun transformTrace(ts: ConcreteTrace, origSys: UppaalSystem): ConcreteTrace {
		var prev: ConcreteState? = null
		val result = ConcreteTrace()

		for (transition in ts) {
			var edges = transition.edges

			if (edges != null) {
				for (i in edges.indices) {
					val select = edges[i]
					val origProcess = origSys.getProcess(select.process.index)
					val origEdge = origProcess.getEdge(select.index)

					edges[i] = SystemEdgeSelect(origEdge, select.selectList)
				}
			}

			val origTarget = transition.target

			var locations = origTarget.locations
			for (i in locations.indices) {
				val origLoc = locations[i]
				val origProcess = origLoc.process

				locations[i] = origSys.getProcess(origProcess.index).getLocation(origLoc.index)
			}

			var cvariables = origTarget.cVariables
			for (i in cvariables.indices) {
				val origVar = cvariables[i]
				cvariables[i] = ConcreteVariable(origVar.getValue(BigDecimal(0)), origVar.rate)
			}

			val target = ConcreteState(origTarget.invariant.clone() as Limit?, locations, cvariables)

			val ctr = ConcreteTransitionRecord(transition.delay, edges, target)
			result.add(ctr)
		}

		return result
	}

	companion object {
		private const val OPTIONS = "order 0\nreduction 1\nrepresentation 0\ntrace 0\nextrapolation 0\nhashsize 27\nreuse 0\nsmcparametric 1\nmodest 0\nstatistical 0.01 0.01 0.05 0.05 0.05 0.9 1.1 0.0 0.0 4096.0 0.01"
	}
}



