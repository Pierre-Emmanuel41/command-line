package fr.pederobien.commandline;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sun.jna.Platform;

import fr.pederobien.commandtree.exceptions.NodeNotFoundException;
import fr.pederobien.commandtree.exceptions.NotAvailableArgumentException;
import fr.pederobien.commandtree.interfaces.ICommandRootNode;
import fr.pederobien.dictionary.impl.JarXmlDictionaryParser;
import fr.pederobien.dictionary.impl.MessageEvent;
import fr.pederobien.dictionary.impl.XmlDictionaryParser;
import fr.pederobien.dictionary.interfaces.IDictionaryParser;
import fr.pederobien.utils.AsyncConsole;

public class CommandLine {
	/**
	 * Case when the code is running from a .jar file.
	 */
	public static final String PRODUCTION_ENVIRONMENT = "Production";

	/**
	 * Case when the code is running from an IDE.
	 */
	public static final String DEVELOPMENT_ENVIRONMENT = "Development";

	private static final String FILE_PREFIX = "file";
	private static final String JAR_PREFIX = "jar";
	private static final String DEV_DICTIONARY_FOLDER = "src/main/resources/dictionaries/";
	private static final String PROD_DICTIONARY_FOLDER = "resources/dictionaries/command-line/";

	private String environment;
	private IDictionaryParser parser;
	private CommandLineBuilder builder;
	private ICommandRootNode<ICode> root;
	private String[] args;
	private AtomicBoolean isInitialized;
	private Scanner scanner;

	private CommandLine(CommandLineBuilder builder, ICommandRootNode<ICode> root, String[] args) {
		this.builder = builder;
		this.root = root;
		this.args = args;

		isInitialized = new AtomicBoolean(false);
	}

	/**
	 * The environment is {@link #DEVELOPMENT_ENVIRONMENT} if the {@link #getUrl()} starts with "file" and is
	 * {@link #PRODUCTION_ENVIRONMENT} if it starts with "jar".
	 * 
	 * @return The environment in which the program is running.
	 */
	public String getEnvironment() {
		return environment;
	}

	/**
	 * Register dictionaries to the {@link CommandLineDictionaryContext}.
	 * 
	 * @param dictionaryFolder The path leading to the folder that contains several dictionaries.
	 * @param dictionaries     A list of file names corresponding to dictionary file to register.
	 */
	public void registerDictionaries(String dictionaryFolder, String[] dictionaries) {
		for (String dictionary : dictionaries)
			try {
				CommandLineDictionaryContext.instance().register(parser.parse(dictionaryFolder.concat(dictionary)));
			} catch (Exception e) {
				AsyncConsole.println(e);
				for (StackTraceElement element : e.getStackTrace())
					AsyncConsole.println(element);
			}
	}

	public void start() {
		if (!initialize()) {
			AsyncConsole.printlnWithTimeStamp("The initialization fails...");
			return;
		}

		if (builder.onStart != null && !builder.onStart.apply(root, args)) {
			AsyncConsole.printlnWithTimeStamp("The start fails...");
			return;
		}

		scanner = new Scanner(System.in);

		while (true) {
			AsyncConsole.print(">");
			String command = scanner.nextLine();

			if (command.trim().equals("stop") && builder.onStop != null) {
				// Disconnecting before stopping program
				try {
					builder.onStop.accept(root);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}

			try {
				root.onCommand(command.split(" "));
				Thread.sleep(200);
			} catch (NodeNotFoundException e) {
				send(ECommandLineCode.COMMAND_LINE__NODE_NOT_FOUND, e.getNotFoundArgument());
			} catch (NotAvailableArgumentException e) {
				send(ECommandLineCode.COMMAND_LINE__NODE_NOT_AVAILABLE, e.getArgument());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		scanner.close();
	}

	/**
	 * @return True if the context has been successfully initialized
	 */
	private boolean initialize() {
		if (!isInitialized.compareAndSet(false, true))
			return false;

		String url = getClass().getResource(getClass().getSimpleName() + ".class").toExternalForm();
		String dictionaryFolder = null;

		// Case Development environment
		if (url.startsWith(FILE_PREFIX)) {
			environment = DEVELOPMENT_ENVIRONMENT;
			parser = new XmlDictionaryParser();
			dictionaryFolder = DEV_DICTIONARY_FOLDER;

			// Case production environment
		} else if (url.startsWith(JAR_PREFIX)) {
			environment = PRODUCTION_ENVIRONMENT;
			// Fix: Should not remove character "/" on Linux environment
			String format = Platform.isWindows() ? "%s:%s:/" : "%s:%s:";
			parser = new JarXmlDictionaryParser(Paths.get(url.split("!")[0].substring(String.format(format, FILE_PREFIX, JAR_PREFIX).length()).replace("%20", " ")));
			dictionaryFolder = PROD_DICTIONARY_FOLDER;
		}

		if (parser == null)
			throw new IllegalStateException("Technical error, the environment is neither a development environment nor a production environment");

		registerDictionaries(dictionaryFolder, new String[] { "English.xml", "French.xml" });

		return builder.onInitialization == null ? true : builder.onInitialization.apply(root);
	}

	public static class CommandLineBuilder {
		private Function<ICommandRootNode<ICode>, Boolean> onInitialization;
		private BiFunction<ICommandRootNode<ICode>, String[], Boolean> onStart;
		private Consumer<ICommandRootNode<ICode>> onStop;

		/**
		 * Creates a builder for a command line context.
		 * 
		 * @param onInitialization The function to run during the initialization phase.
		 */
		public CommandLineBuilder(Function<ICommandRootNode<ICode>, Boolean> onInitialization) {
			this.onInitialization = onInitialization;
		}

		/**
		 * Specify the method to call before starting the context.
		 * 
		 * @param onStart The function to run before starting the main loop.
		 * 
		 * @return This builder.
		 */
		public CommandLineBuilder onStart(BiFunction<ICommandRootNode<ICode>, String[], Boolean> onStart) {
			this.onStart = onStart;
			return this;
		}

		/**
		 * Specify the method to call before stopping the context.
		 * 
		 * @param onStop The function to run before stopping the main loop.
		 * 
		 * @return This builder.
		 */
		public CommandLineBuilder onStop(Consumer<ICommandRootNode<ICode>> onStop) {
			this.onStop = onStop;
			return this;
		}

		/**
		 * Creates a command line context ready to start.
		 * 
		 * @param root The command root in order to store command line.
		 * @param args The string array from the main method.
		 * 
		 * @return The command line to start.
		 */
		public CommandLine build(ICommandRootNode<ICode> root, String[] args) {
			return new CommandLine(this, root, args);
		}
	}

	/**
	 * Send a language sensitive message in the console.
	 * 
	 * @param code Used as key to get the right message in the right dictionary.
	 * @param args Some arguments (optional) used for dynamic messages.
	 */
	public void send(ICode code, Object... args) {
		AsyncConsole.printlnWithTimeStamp(CommandLineDictionaryContext.instance().getMessage(new MessageEvent(Locale.getDefault(), code.getCode(), args)));
	}
}
