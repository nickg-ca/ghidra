package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.CodeUnit;
import ghidra.program.model.listing.CodeUnitIterator;
import ghidra.program.model.listing.Program;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class GetListingTool implements McpTool {
	private final McpContext context;

	public GetListingTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "get_listing";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Gets the disassembly listing for a range of addresses.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"address", Map.of("type", "string", "description", "Start address"),
					"count", Map.of("type", "integer", "description", "Number of code units to list")
				),
				List.of("address", "count"),
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
		int count = ((Number) arguments.get("count")).intValue();

		Address addr = program.getAddressFactory().getAddress(addrStr);
		CodeUnitIterator it = program.getListing().getCodeUnits(addr, true);

		StringBuilder sb = new StringBuilder();
		int c = 0;
		while (it.hasNext() && c < count) {
			CodeUnit cu = it.next();
			sb.append(cu.getAddressString(false, true)).append("  ")
			  .append(cu.toString()).append("\n");
			c++;
		}

		return new CallToolResult(Collections.singletonList(new TextContent(sb.toString())), false);
	}
}
