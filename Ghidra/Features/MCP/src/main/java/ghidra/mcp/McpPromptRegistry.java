package ghidra.mcp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class McpPromptRegistry {
	private final Map<String, McpPrompt> prompts = new HashMap<>();

	public void register(McpPrompt prompt) {
		prompts.put(prompt.getName(), prompt);
	}

	public Collection<McpPrompt> getPrompts() {
		return prompts.values();
	}
}
