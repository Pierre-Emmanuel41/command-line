package fr.pederobien.commandline;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.dictionary.impl.DictionaryContext;
import fr.pederobien.dictionary.interfaces.IDictionary;
import fr.pederobien.dictionary.interfaces.IDictionaryContext;
import fr.pederobien.dictionary.interfaces.IDictionaryParser;
import fr.pederobien.dictionary.interfaces.IMessageEvent;
import fr.pederobien.utils.AsyncConsole;

public class CommandLineDictionaryContext implements IDictionaryContext {
	private IDictionaryContext context;

	private CommandLineDictionaryContext() {
		context = new DictionaryContext();
	}

	/**
	 * @return The unique instance of this dictionary context.
	 */
	public static CommandLineDictionaryContext instance() {
		return SingletonHolder.CONTEXT;
	}

	private static class SingletonHolder {
		private static final CommandLineDictionaryContext CONTEXT = new CommandLineDictionaryContext();
	}

	@Override
	public IDictionaryContext setParser(IDictionaryParser parser) {
		return context.setParser(parser);
	}

	@Override
	public IDictionaryContext register(IDictionary dictionary) {
		return context.register(dictionary);
	}

	@Override
	public IDictionaryContext register(String path) throws Exception {
		return context.register(path);
	}

	@Override
	public IDictionaryContext unregister(IDictionary dictionary) {
		return context.unregister(dictionary);
	}

	@Override
	public Optional<IDictionary> getDictionary(Locale locale) {
		return context.getDictionary(locale);
	}

	@Override
	public Map<Locale, IDictionary> getDictionaries() {
		return context.getDictionaries();
	}

	@Override
	public String getMessage(IMessageEvent event) {
		return context.getMessage(event);
	}

	/**
	 * Send a language sensitive message in the console.
	 * 
	 * @param event The event used to get which message should be send, and to who the message should be sent.
	 */
	public void send(IMessageEvent event) {
		AsyncConsole.printlnWithTimeStamp(getMessage(event));
	}
}