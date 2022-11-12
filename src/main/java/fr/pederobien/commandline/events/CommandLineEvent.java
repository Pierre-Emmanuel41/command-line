package fr.pederobien.commandline.events;

import fr.pederobien.commandline.impl.CommandLine;

public class CommandLineEvent extends ProjectCommandLineEvent {
	private CommandLine commandLine;

	/**
	 * Creates a command line event.
	 * 
	 * @param commandLine The command line source involved in this event.
	 */
	public CommandLineEvent(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	/**
	 * @return The command line involved in this event.
	 */
	public CommandLine getCommandLine() {
		return commandLine;
	}
}
