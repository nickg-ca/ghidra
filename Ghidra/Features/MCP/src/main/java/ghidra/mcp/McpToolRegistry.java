package ghidra.mcp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class McpToolRegistry {
	private final Map<String, McpTool> tools = new HashMap<>();

	public void register(McpTool tool) {
		tools.put(tool.getName(), tool);
	}

	public Collection<McpTool> getTools() {
		return tools.values();
	}
}
