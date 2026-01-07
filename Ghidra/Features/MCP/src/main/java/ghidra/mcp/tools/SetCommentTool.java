package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.CodeUnit;
import ghidra.program.model.listing.Program;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class SetCommentTool implements McpTool {
	private final McpContext context;

	public SetCommentTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "set_comment";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Sets a comment at the specified address.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"address", Map.of("type", "string", "description", "The address to comment"),
					"comment", Map.of("type", "string", "description", "The comment text"),
					"type", Map.of("type", "string", "description", "Type of comment (plate, pre, post, eol, repeatable). Default: eol")
				),
				List.of("address", "comment"),
				false,
				null,
				null
			))
			.build();
	}

	@Override
	public CallToolResult execute(Map<String, Object> arguments) throws Exception {
		Program program = context.getCurrentProgram();
		if (program == null) return new CallToolResult(Collections.singletonList(new TextContent("Error: No program open.")), true);

		String addrStr = (String) arguments.get("address");
		String comment = (String) arguments.get("comment");
		String typeStr = (String) arguments.getOrDefault("type", "eol");

		Address addr = program.getAddressFactory().getAddress(addrStr);
		if (addr == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid address " + addrStr)), true);
		}

		int commentType;
		switch (typeStr.toLowerCase()) {
			case "plate": commentType = CodeUnit.PLATE_COMMENT; break;
			case "pre": commentType = CodeUnit.PRE_COMMENT; break;
			case "post": commentType = CodeUnit.POST_COMMENT; break;
			case "eol": commentType = CodeUnit.EOL_COMMENT; break;
			case "repeatable": commentType = CodeUnit.REPEATABLE_COMMENT; break;
			default: return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid comment type: " + typeStr)), true);
		}

		int txId = program.startTransaction("Set Comment");
		try {
			program.getListing().setComment(addr, commentType, comment);
			program.endTransaction(txId, true);
			return new CallToolResult(Collections.singletonList(new TextContent("Comment set successfully.")), false);
		} catch (Exception e) {
			program.endTransaction(txId, false);
			return new CallToolResult(Collections.singletonList(new TextContent("Error setting comment: " + e.getMessage())), true);
		}
	}
}
