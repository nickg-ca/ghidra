package ghidra.mcp.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.framework.model.DomainFile;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ListModulesTool implements McpTool {
	private final McpContext context;

	public ListModulesTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "list_modules";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Lists all modules (programs) in the current Ghidra project.")
			.build();
	}

	@Override
	public CallToolResult execute(Map<String, Object> arguments) throws Exception {
		List<String> files = new ArrayList<>();
		listFiles(context.getProjectData().getRootFolder(), files, "");

		StringBuilder sb = new StringBuilder();
		for (String f : files) {
			sb.append(f).append("\n");
		}

		return new CallToolResult(
			Collections.singletonList(new TextContent(sb.toString())),
			false
		);
	}

	private void listFiles(ghidra.framework.model.DomainFolder folder, List<String> result, String prefix) {
		for (DomainFile file : folder.getFiles()) {
			result.add(prefix + "/" + file.getName());
		}
		for (ghidra.framework.model.DomainFolder child : folder.getFolders()) {
			listFiles(child, result, prefix + "/" + child.getName());
		}
	}
}
