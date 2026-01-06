package ghidra.mcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import ghidra.mcp.tools.DecompilerFactory;
import ghidra.mcp.tools.DecompilerWrapper;
import ghidra.mcp.tools.McpDecompileResult;
import ghidra.mcp.tools.DecompileTool;
import ghidra.mcp.tools.GetListingTool;
import ghidra.mcp.tools.ReadBytesTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressFactory;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.address.GenericAddressSpace;
import ghidra.program.model.listing.CodeUnit;
import ghidra.program.model.listing.CodeUnitIterator;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Listing;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

public class McpServerTest {

    private Program mockProgram;
    private McpContext context;

    @Before
    public void setUp() {
        mockProgram = (Program) Proxy.newProxyInstance(
            Program.class.getClassLoader(),
            new Class[] { Program.class },
            new ProgramHandler()
        );
        context = new McpContext(null, mockProgram, null);
    }

    @Test
    public void testReadBytes() {
        ReadBytesTool tool = new ReadBytesTool();
        Map<String, Object> args = new HashMap<>();
        args.put("address", "1000");
        args.put("length", 4);

        CallToolResult result = tool.getHandler(context).apply(args);

        List<io.modelcontextprotocol.spec.McpSchema.Content> content = result.content();
        assertEquals(1, content.size());
        assertTrue(content.get(0) instanceof TextContent);
        TextContent text = (TextContent) content.get(0);

        // Mock memory returns bytes: 0, 1, 2, 3
        assertEquals("00010203", text.text());
    }

    @Test
    public void testGetListing() {
        GetListingTool tool = new GetListingTool();
        Map<String, Object> args = new HashMap<>();
        args.put("address", "1000");
        args.put("length", 1);

        CallToolResult result = tool.getHandler(context).apply(args);

        List<io.modelcontextprotocol.spec.McpSchema.Content> content = result.content();
        assertEquals(1, content.size());
        TextContent text = (TextContent) content.get(0);

        // Mock instruction at 1000 is "MOV EAX, 1"
        assertTrue(text.text().contains("MOV"));
    }

    @Test
    public void testDecompile() {
        // Mock the Decompiler
        DecompilerFactory mockFactory = new DecompilerFactory() {
            @Override
            public DecompilerWrapper create() {
                return (DecompilerWrapper) Proxy.newProxyInstance(
                    DecompilerWrapper.class.getClassLoader(),
                    new Class[] { DecompilerWrapper.class },
                    new DecompilerWrapperHandler()
                );
            }
        };

        DecompileTool tool = new DecompileTool(mockFactory);
        Map<String, Object> args = new HashMap<>();
        args.put("address", "1000"); // Assuming function at 1000

        CallToolResult result = tool.getHandler(context).apply(args);

        List<io.modelcontextprotocol.spec.McpSchema.Content> content = result.content();
        assertEquals(1, content.size());
        TextContent text = (TextContent) content.get(0);

        // Mock decompiler returns "void main() { ... }"
        assertEquals("void main() { return; }", text.text());
    }

    // --- Invocation Handlers ---

    private class ProgramHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getAddressFactory")) {
                return (AddressFactory) Proxy.newProxyInstance(
                    AddressFactory.class.getClassLoader(),
                    new Class[] { AddressFactory.class },
                    new AddressFactoryHandler()
                );
            }
            if (method.getName().equals("getMemory")) {
                return (Memory) Proxy.newProxyInstance(
                    Memory.class.getClassLoader(),
                    new Class[] { Memory.class },
                    new MemoryHandler()
                );
            }
            if (method.getName().equals("getListing")) {
                return (Listing) Proxy.newProxyInstance(
                    Listing.class.getClassLoader(),
                    new Class[] { Listing.class },
                    new ListingHandler()
                );
            }
            if (method.getName().equals("getFunctionManager")) {
                return (FunctionManager) Proxy.newProxyInstance(
                    FunctionManager.class.getClassLoader(),
                    new Class[] { FunctionManager.class },
                    new FunctionManagerHandler()
                );
            }
            return null;
        }
    }

    private class AddressFactoryHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getAddress")) {
                String addrStr = (String) args[0];
                return createMockAddress(Long.parseLong(addrStr, 16));
            }
            return null;
        }
    }

    private class MemoryHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getBytes")) {
                // getBytes(Address addr, byte[] b)
                byte[] buffer = (byte[]) args[1];
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte) i;
                }
                return buffer.length;
            }
            return null;
        }
    }

    private class ListingHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getCodeUnits")) {
                // getCodeUnits(Address start, boolean forward)
                return new MockCodeUnitIterator();
            }
            return null;
        }
    }

    private class FunctionManagerHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getFunctionContaining")) {
                return (Function) Proxy.newProxyInstance(
                    Function.class.getClassLoader(),
                    new Class[] { Function.class },
                    new FunctionHandler()
                );
            }
            return null;
        }
    }

    private class FunctionHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }

    private class DecompilerWrapperHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("decompileFunction")) {
                return new McpDecompileResult() {
                    @Override public boolean decompileCompleted() { return true; }
                    @Override public String getErrorMessage() { return null; }
                    @Override public String getC() { return "void main() { return; }"; }
                };
            }
            return null;
        }
    }

    private Address createMockAddress(long offset) {
        GenericAddressSpace space = new GenericAddressSpace("ram", 64, 1, 0);
        return space.getAddress(offset);
    }

    private class MockCodeUnitIterator implements CodeUnitIterator {
        private int count = 0;

        @Override
        public boolean hasNext() {
            return count < 10;
        }

        @Override
        public CodeUnit next() {
            count++;
            return (Instruction) Proxy.newProxyInstance(
                Instruction.class.getClassLoader(),
                new Class[] { Instruction.class },
                new InstructionHandler()
            );
        }

        @Override public void remove() {}
        @Override public Iterator<CodeUnit> iterator() { return this; }
    }

    private class InstructionHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getAddressString")) {
                return "1000";
            }
            if (method.getName().equals("getMnemonicString")) {
                return "MOV";
            }
            if (method.getName().equals("getNumOperands")) {
                return 2;
            }
            if (method.getName().equals("getDefaultOperandRepresentation")) {
                int op = (int) args[0];
                return (op == 0) ? "EAX" : "1";
            }
            return null;
        }
    }
}
