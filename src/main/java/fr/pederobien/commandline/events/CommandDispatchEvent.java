package fr.pederobien.commandline.events;

import java.util.StringJoiner;

import fr.pederobien.commandline.impl.CommandLine;

public class CommandDispatchEvent extends CommandLineEvent {
	private String arguments;

	/**
	 * Creates an event thrown when a command has been dispatched in a command line.
	 * 
	 * @param commandLine The command line to which a command has been dispatched.
	 * @param arguments   The command arguments.
	 */
	public CommandDispatchEvent(CommandLine commandLine, String arguments) {
		super(commandLine);
		this.arguments = arguments;
	}

	/**
	 * @return The raw command arguments.
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * @return An array that contains each arguments of the command.
	 */
	public String[] getArgs() {
		return arguments.split(" ");
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("arguments=" + getArguments());
		return String.format("%s_%s", getName(), joiner);
	}
}
