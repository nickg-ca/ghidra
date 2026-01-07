package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.symbol.SourceType;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class SetLabelTool implements McpTool {
	private final McpContext context;

	public SetLabelTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "set_label";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Sets a label (symbol) at the specified address.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"address", Map.of("type", "string", "description", "The address to label"),
					"name", Map.of("type", "string", "description", "The name of the label")
				),
				List.of("address", "name"),
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
		String name = (String) arguments.get("name");

		Address addr = program.getAddressFactory().getAddress(addrStr);
		if (addr == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid address " + addrStr)), true);
		}

		int txId = program.startTransaction("Set Label: " + name);
		try {
			program.getSymbolTable().createLabel(addr, name, SourceType.USER_DEFINED);
			program.endTransaction(txId, true);
			return new CallToolResult(Collections.singletonList(new TextContent("Label set successfully.")), false);
		} catch (Exception e) {
			program.endTransaction(txId, false);
			return new CallToolResult(Collections.singletonList(new TextContent("Error setting label: " + e.getMessage())), true);
		}
	}
}
