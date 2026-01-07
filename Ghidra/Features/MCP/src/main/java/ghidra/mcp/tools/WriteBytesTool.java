package ghidra.mcp.tools;

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

public class WriteBytesTool implements McpTool {
	private final McpContext context;

	public WriteBytesTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "write_bytes";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Writes bytes to memory at the specified address.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"address", Map.of("type", "string", "description", "The address to write to"),
					"bytes", Map.of("type", "string", "description", "Hex string of bytes to write")
				),
				List.of("address", "bytes"),
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
		String hexBytes = (String) arguments.get("bytes");

		Address addr = program.getAddressFactory().getAddress(addrStr);
		if (addr == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid address " + addrStr)), true);
		}

		byte[] bytes;
		try {
			bytes = HexUtils.hexStringToByteArray(hexBytes);
		} catch (IllegalArgumentException e) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid hex string.")), true);
		}

		int txId = program.startTransaction("Write Bytes");
		try {
			program.getMemory().setBytes(addr, bytes);
			program.endTransaction(txId, true);
			return new CallToolResult(Collections.singletonList(new TextContent("Bytes written successfully.")), false);
		} catch (Exception e) {
			program.endTransaction(txId, false);
			return new CallToolResult(Collections.singletonList(new TextContent("Error writing bytes: " + e.getMessage())), true);
		}
	}
}
