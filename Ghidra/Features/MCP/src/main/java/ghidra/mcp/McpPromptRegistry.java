package ghidra.mcp;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptRegistration;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;

public class McpPromptRegistry {

    private final Map<String, SyncPromptRegistration> prompts = new HashMap<>();

    public void register(Prompt prompt, Function<Map<String, String>, GetPromptResult> handler) {
        prompts.put(prompt.name(), new SyncPromptRegistration(prompt, handler));
    }

    public Map<String, SyncPromptRegistration> getRegisteredPrompts() {
        return prompts;
    }
}
