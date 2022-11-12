package fr.pederobien.commandline.impl;

import fr.pederobien.dictionary.interfaces.ICode;

public enum ECommandLineCode implements ICode {

	// Code when a node does not exist
	COMMAND_LINE__NODE_NOT_FOUND,

	// Code when a node is not available
	COMMAND_LINE__NODE_NOT_AVAILABLE;

	@Override
	public String getCode() {
		return name();
	}
}
