package ghidra.mcp.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import ghidra.mcp.HexUtils;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class SearchMemoryTool implements McpTool {
	private final McpContext context;

	public SearchMemoryTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "search_memory";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Searches memory for a byte sequence.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"query", Map.of("type", "string", "description", "Hex string of bytes to search for"),
					"start_address", Map.of("type", "string", "description", "Optional start address")
				),
				List.of("query"),
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

		String queryHex = (String) arguments.get("query");
		String startAddrStr = (String) arguments.get("start_address");

		Address startAddr = program.getMinAddress();
		if (startAddrStr != null) {
			startAddr = program.getAddressFactory().getAddress(startAddrStr);
		}

		byte[] queryBytes;
		try {
			queryBytes = HexUtils.hexStringToByteArray(queryHex);
		} catch (IllegalArgumentException e) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid hex string.")), true);
		}

		// Basic single result search for now, or maybe first 10
		List<String> found = new ArrayList<>();
		Address currentAddr = startAddr;
		int maxResults = 10;

		while (found.size() < maxResults) {
			Address hit = program.getMemory().findBytes(currentAddr, queryBytes, null, true, null);
			if (hit == null) break;
			found.add(hit.toString());
			currentAddr = hit.add(1);
		}

		return new CallToolResult(Collections.singletonList(new TextContent(String.join("\n", found))), false);
	}
}
