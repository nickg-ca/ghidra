package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class ReadBytesTool implements McpTool {
	private final McpContext context;

	public ReadBytesTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "read_bytes";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Reads bytes from memory at the given address.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"address", Map.of("type", "string", "description", "The address to read from"),
					"length", Map.of("type", "integer", "description", "Number of bytes to read")
				),
				List.of("address", "length"),
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
		int length = ((Number) arguments.get("length")).intValue();

		Address addr = program.getAddressFactory().getAddress(addrStr);
		byte[] buf = new byte[length];

		try {
			int read = program.getMemory().getBytes(addr, buf);
			StringBuilder hex = new StringBuilder();
			for (int i = 0; i < read; i++) {
				hex.append(String.format("%02X", buf[i] & 0xFF));
			}
			return new CallToolResult(Collections.singletonList(new TextContent(hex.toString())), false);
		} catch (MemoryAccessException e) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error reading memory: " + e.getMessage())), true);
		}
	}
}
