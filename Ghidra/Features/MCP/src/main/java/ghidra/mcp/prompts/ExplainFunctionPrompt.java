package ghidra.mcp.prompts;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpPrompt;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class ExplainFunctionPrompt implements McpPrompt {
	private final McpContext context;

	public ExplainFunctionPrompt(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "explain_function";
	}

	@Override
	public Prompt getPromptDef() {
		return new Prompt(
			getName(),
			"Prompt to explain a decompiled function.",
			List.of(
				new PromptArgument("code", "The code of the function to explain", true)
			)
		);
	}

	@Override
	public GetPromptResult execute(Map<String, String> arguments) throws Exception {
		String code = arguments.get("code");
		String promptText = "Please explain the following decompiled C code from a binary:\n\n```c\n" + code + "\n```\n\nIdentify the purpose of the function, key variables, and any potential security issues.";

		return new GetPromptResult(
			"explain_function",
			Collections.singletonList(
				new PromptMessage(Role.USER, new TextContent(promptText))
			)
		);
	}
}
