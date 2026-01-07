package ghidra.mcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ghidra.mcp.tools.ExecuteBatchTool;
import ghidra.mcp.tools.ReadBytesTool;
import ghidra.mcp.tools.SetLabelTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressFactory;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.symbol.SourceType;
import ghidra.program.model.symbol.SymbolTable;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

// Simple mock support
class MockContext extends McpContext {
	public MockContext(Program p) {
		super(null);
		setCurrentProgram(p);
	}
}

public class GhidraMcpTest {

	@Test
	public void testReadBytesTool() throws Exception {
		Program mockProgram = createMockProgram();
		McpContext context = new MockContext(mockProgram);
		ReadBytesTool tool = new ReadBytesTool(context);

		Map<String, Object> args = Map.of("address", "1000", "length", 4);
		CallToolResult result = tool.execute(args);

		TextContent content = (TextContent) result.content().get(0);
		assertEquals("DEADBEEF", content.text());
	}

	@Test
	public void testSetLabelTool() throws Exception {
		Program mockProgram = createMockProgram();
		McpContext context = new MockContext(mockProgram);
		SetLabelTool tool = new SetLabelTool(context);

		Map<String, Object> args = Map.of("address", "1000", "name", "my_label");
		CallToolResult result = tool.execute(args);

		// We assume success if no exception and correct message
		TextContent content = (TextContent) result.content().get(0);
		assertTrue(content.text().contains("Label set successfully"));
	}

	@Test
	public void testExecuteBatchTool() throws Exception {
		Program mockProgram = createMockProgram();
		McpContext context = new MockContext(mockProgram);
		McpToolRegistry registry = new McpToolRegistry();

		// Register real tools for the test
		registry.register(new ReadBytesTool(context));
		registry.register(new SetLabelTool(context));

		ExecuteBatchTool batchTool = new ExecuteBatchTool(registry);

		List<Map<String, Object>> ops = List.of(
			Map.of(
				"tool", "read_bytes",
				"args", Map.of("address", "1000", "length", 4)
			),
			Map.of(
				"tool", "set_label",
				"args", Map.of("address", "1000", "name", "test_batch")
			)
		);

		Map<String, Object> args = Map.of("operations", ops);
		CallToolResult result = batchTool.execute(args);

		// Verify results
		// 1. Header for ReadBytes
		// 2. ReadBytes result
		// 3. Header for SetLabel
		// 4. SetLabel result
		assertEquals(4, result.content().size());

		assertTrue(((TextContent)result.content().get(0)).text().contains("Result for read_bytes"));
		assertEquals("DEADBEEF", ((TextContent)result.content().get(1)).text());

		assertTrue(((TextContent)result.content().get(2)).text().contains("Result for set_label"));
		assertTrue(((TextContent)result.content().get(3)).text().contains("Label set successfully"));
	}

	private Program createMockProgram() {
		return (Program) java.lang.reflect.Proxy.newProxyInstance(
			Program.class.getClassLoader(),
			new Class[] { Program.class, ghidra.framework.model.DomainObject.class },
			(proxy, method, args) -> {
				if (method.getName().equals("getAddressFactory")) {
					return createMockAddressFactory();
				}
				if (method.getName().equals("getMemory")) {
					return createMockMemory();
				}
				if (method.getName().equals("getSymbolTable")) {
					return createMockSymbolTable();
				}
				if (method.getName().equals("startTransaction")) {
					return 1;
				}
				if (method.getName().equals("endTransaction")) {
					return true;
				}
				return null;
			}
		);
	}

	private AddressFactory createMockAddressFactory() {
		return (AddressFactory) java.lang.reflect.Proxy.newProxyInstance(
			AddressFactory.class.getClassLoader(),
			new Class[] { AddressFactory.class },
			(proxy, method, args) -> {
				if (method.getName().equals("getAddress")) {
					return createMockAddress((String)args[0]);
				}
				return null;
			}
		);
	}

	private Address createMockAddress(String addr) {
		return (Address) java.lang.reflect.Proxy.newProxyInstance(
			Address.class.getClassLoader(),
			new Class[] { Address.class },
			(proxy, method, args) -> {
				if (method.getName().equals("toString")) return addr;
				if (method.getName().equals("equals")) return addr.equals(((Address)args[0]).toString());
				return null;
			}
		);
	}

	private Memory createMockMemory() {
		return (Memory) java.lang.reflect.Proxy.newProxyInstance(
			Memory.class.getClassLoader(),
			new Class[] { Memory.class },
			(proxy, method, args) -> {
				if (method.getName().equals("getBytes")) {
					byte[] buf = (byte[]) args[1];
					buf[0] = (byte) 0xDE;
					buf[1] = (byte) 0xAD;
					buf[2] = (byte) 0xBE;
					buf[3] = (byte) 0xEF;
					return 4;
				}
				return null;
			}
		);
	}

	private SymbolTable createMockSymbolTable() {
		return (SymbolTable) java.lang.reflect.Proxy.newProxyInstance(
			SymbolTable.class.getClassLoader(),
			new Class[] { SymbolTable.class },
			(proxy, method, args) -> {
				if (method.getName().equals("createLabel")) {
					// In a real mock, we'd store this or verify it.
					// For now, just succeed.
					return null;
				}
				return null;
			}
		);
	}
}
