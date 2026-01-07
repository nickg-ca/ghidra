package ghidra.mcp.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class GetReferencesTool implements McpTool {
	private final McpContext context;

	public GetReferencesTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "get_references";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Gets references to or from an address.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of(
					"address", Map.of("type", "string", "description", "The address to query"),
					"direction", Map.of("type", "string", "description", "'to' or 'from'. Default: to")
				),
				List.of("address"),
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
		String direction = (String) arguments.getOrDefault("direction", "to");

		Address addr = program.getAddressFactory().getAddress(addrStr);
		if (addr == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid address " + addrStr)), true);
		}

		Reference[] refs;
		if ("from".equalsIgnoreCase(direction)) {
			refs = program.getReferenceManager().getReferencesFrom(addr);
		} else {
			ReferenceIterator iter = program.getReferenceManager().getReferencesTo(addr);
			List<Reference> refList = new ArrayList<>();
			while(iter.hasNext()) {
				refList.add(iter.next());
			}
			refs = refList.toArray(new Reference[0]);
		}

		List<String> resultList = new ArrayList<>();
		for (Reference ref : refs) {
			Address otherAddr = "from".equalsIgnoreCase(direction) ? ref.getToAddress() : ref.getFromAddress();
			resultList.add(String.format("%s (%s)", otherAddr.toString(), ref.getReferenceType().getName()));
		}

		return new CallToolResult(Collections.singletonList(new TextContent(String.join("\n", resultList))), false);
	}
}
