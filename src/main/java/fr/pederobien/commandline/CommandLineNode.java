package fr.pederobien.commandline;

import java.util.Locale;
import java.util.function.Supplier;

import fr.pederobien.commandtree.impl.CommandNode;
import fr.pederobien.dictionary.impl.MessageEvent;

public class CommandLineNode extends CommandNode<ICode> {

	/**
	 * Creates a node specified by the given parameters.
	 * 
	 * @param label       The primary node name.
	 * @param explanation The explanation associated to this node.
	 * @param isAvailable True if this node is available, false otherwise.
	 */
	protected CommandLineNode(String label, ICode explanation, Supplier<Boolean> isAvailable) {
		super(label, explanation, isAvailable);
	}

	/**
	 * Creates a node specified by the given parameters.
	 * 
	 * @param label       The primary node name.
	 * @param explanation The explanation associated to this node.
	 */
	protected CommandLineNode(String label, ICode explanation) {
		this(label, explanation, () -> false);
	}

	/**
	 * Send a language sensitive message in the console.
	 * 
	 * @param code Used as key to get the right message in the right dictionary.
	 * @param args Some arguments (optional) used for dynamic messages.
	 */
	protected void send(ICode code, Object... args) {
		CommandLineDictionaryContext.instance().send(new MessageEvent(Locale.getDefault(), code.getCode(), args));
	}

	/**
	 * Get a message associated to the given code translated in the OS language.
	 * 
	 * @param code Used as key to get the right message in the right dictionary.
	 * @param args Some arguments (optional) used for dynamic messages.
	 */
	protected String getMessage(ICode code, Object... args) {
		return CommandLineDictionaryContext.instance().getMessage(new MessageEvent(Locale.getDefault(), code.getCode(), args));
	}
}
