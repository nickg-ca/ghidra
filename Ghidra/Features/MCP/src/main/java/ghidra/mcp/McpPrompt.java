package ghidra.mcp;

import java.util.Map;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;

public interface McpPrompt {
	String getName();
	Prompt getPromptDef();
	GetPromptResult execute(Map<String, String> arguments) throws Exception;
}
