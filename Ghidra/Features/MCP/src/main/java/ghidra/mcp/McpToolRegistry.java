package ghidra.mcp;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolRegistration;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class McpToolRegistry {

    private final Map<String, SyncToolRegistration> tools = new HashMap<>();

    public void register(Tool tool, Function<Map<String, Object>, CallToolResult> handler) {
        tools.put(tool.name(), new SyncToolRegistration(tool, handler));
    }

    public Map<String, SyncToolRegistration> getRegisteredTools() {
        return tools;
    }
}
