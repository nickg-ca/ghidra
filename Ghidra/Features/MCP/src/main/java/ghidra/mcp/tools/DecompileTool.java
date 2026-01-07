package ghidra.mcp.tools;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Program;
import ghidra.util.task.TaskMonitor;
import ghidra.mcp.McpContext;
import ghidra.mcp.McpTool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;

public class DecompileTool implements McpTool {
	private final McpContext context;

	public DecompileTool(McpContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "decompile_function";
	}

	@Override
	public Tool getToolDef() {
		return Tool.builder()
			.name(getName())
			.description("Decompiles the function at the given address.")
			.inputSchema(new JsonSchema(
				"object",
				Map.of("address", Map.of("type", "string", "description", "The address of the function (hex string, e.g., 0x401000)")),
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
		if (program == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: No program open.")), true);
		}

		String addrStr = (String) arguments.get("address");
		Address addr = program.getAddressFactory().getAddress(addrStr);
		if (addr == null) {
			return new CallToolResult(Collections.singletonList(new TextContent("Error: Invalid address: " + addrStr)), true);
		}

		Function func = program.getFunctionManager().getFunctionAt(addr);
		if (func == null) {
			func = program.getFunctionManager().getFunctionContaining(addr);
			if (func == null) {
				return new CallToolResult(Collections.singletonList(new TextContent("Error: No function found at or containing " + addrStr)), true);
			}
		}

		DecompInterface ifc = new DecompInterface();
		ifc.openProgram(program);

		DecompileResults res = ifc.decompileFunction(func, 60, TaskMonitor.DUMMY);
		ifc.dispose();

		if (res.decompileCompleted()) {
			return new CallToolResult(Collections.singletonList(new TextContent(res.getDecompiledFunction().getC())), false);
		} else {
			return new CallToolResult(Collections.singletonList(new TextContent("Error decompiling: " + res.getErrorMessage())), true);
		}
	}
}
