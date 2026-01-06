package ghidra.mcp.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import ghidra.mcp.McpContext;
import ghidra.mcp.McpToolRegistry;
import ghidra.program.model.listing.Program;
import ghidra.test.AbstractGhidraHeadedIntegrationTest;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolRegistration;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ExecuteBatchToolTest {

    private McpToolRegistry registry;
    private ExecuteBatchTool batchTool;
    private DummyMcpContext context;

    // Dummy Context to mock Program (partial mock)
    class DummyMcpContext extends McpContext {
        public DummyMcpContext() {
            super(null, null, null);
        }

        @Override
        public Program getCurrentProgram() {
            // We use a simpler mock here or null, but ExecuteBatchTool needs a program for transaction.
            // Since we can't easily spin up a full Ghidra Program in this lightweight test without extending IntegrationTest,
            // we will try to use a mock object if possible.
            // However, extending AbstractGhidraHeadedIntegrationTest requires full environment.
            // For now, let's just check the tool logic up to the point of Program usage, or mock the program heavily.
            // Actually, let's create a MockProgram proxy.
            return mockProgram();
        }
    }

    private Program mockProgram() {
        return (Program) java.lang.reflect.Proxy.newProxyInstance(
            Program.class.getClassLoader(),
            new Class[] { Program.class },
            (proxy, method, args) -> {
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

    @Before
    public void setUp() {
        registry = new McpToolRegistry();
        batchTool = new ExecuteBatchTool(registry);
        context = new DummyMcpContext();

        // Register a dummy tool "echo"
        registry.register(
            new Tool("echo", "Echoes input", Map.of()),
            args -> {
                String msg = (String) args.get("msg");
                if ("FAIL".equals(msg)) {
                    return new CallToolResult(List.of(new TextContent("Echo Failed")), true);
                }
                return new CallToolResult(List.of(new TextContent("Echo: " + msg)), false);
            }
        );
    }

    @Test
    public void testSequentialExecution() {
        Map<String, Object> req1 = Map.of("tool", "echo", "args", Map.of("msg", "Hello"));
        Map<String, Object> req2 = Map.of("tool", "echo", "args", Map.of("msg", "World"));

        Map<String, Object> args = Map.of("requests", List.of(req1, req2));

        CallToolResult result = batchTool.getHandler(context).apply(args);

        assert !result.isError();
        List<Content> content = result.content();
        assertEquals(2, content.size());
        assertTrue(((TextContent)content.get(0)).text().contains("Success"));
        assertTrue(((TextContent)content.get(0)).text().contains("Echo: Hello"));
        assertTrue(((TextContent)content.get(1)).text().contains("Success"));
        assertTrue(((TextContent)content.get(1)).text().contains("Echo: World"));
    }

    @Test
    public void testExecutionFailureStopsBatch() {
        Map<String, Object> req1 = Map.of("tool", "echo", "args", Map.of("msg", "Hello"));
        Map<String, Object> req2 = Map.of("tool", "echo", "args", Map.of("msg", "FAIL"));
        Map<String, Object> req3 = Map.of("tool", "echo", "args", Map.of("msg", "Skipped"));

        Map<String, Object> args = Map.of("requests", List.of(req1, req2, req3));

        CallToolResult result = batchTool.getHandler(context).apply(args);

        assert result.isError();
        List<Content> content = result.content();
        assertEquals(2, content.size()); // Should have 1 success, 1 failure
        assertTrue(((TextContent)content.get(0)).text().contains("Success"));
        assertTrue(((TextContent)content.get(1)).text().contains("Failed"));
    }
}
