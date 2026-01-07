package ghidra.mcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import ghidra.mcp.tools.ReadBytesTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressFactory;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
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
		Program mockProgram = (Program) java.lang.reflect.Proxy.newProxyInstance(
			Program.class.getClassLoader(),
			new Class[] { Program.class, ghidra.framework.model.DomainObject.class },
			(proxy, method, args) -> {
				if (method.getName().equals("getAddressFactory")) {
					return createMockAddressFactory();
				}
				if (method.getName().equals("getMemory")) {
					return createMockMemory();
				}
				if (method.getName().equals("endTransaction")) {
					return true;
				}
				return null;
			}
		);

		McpContext context = new MockContext(mockProgram);
		ReadBytesTool tool = new ReadBytesTool(context);

		Map<String, Object> args = Map.of("address", "1000", "length", 4);
		CallToolResult result = tool.execute(args);

		// result.content() is List<Content>
		// Content is sealed interface? TextContent or ImageContent.
		TextContent content = (TextContent) result.content().get(0);
		// TextContent has text() method.
		assertEquals("DEADBEEF", content.text());
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
				// Address also needs equals/compareTo often but maybe not for this simple test
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
}
