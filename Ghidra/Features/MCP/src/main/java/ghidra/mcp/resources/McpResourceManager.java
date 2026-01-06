package ghidra.mcp.resources;

import java.util.ArrayList;
import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceRegistration;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;

public class McpResourceManager {

    public List<SyncResourceRegistration> getResources() {
        List<SyncResourceRegistration> resources = new ArrayList<>();

        resources.add(new SyncResourceRegistration(
            new Resource(
                "program://current/listing",
                "Current Program Listing",
                "Access to the current program's listing",
                "text/plain",
                null
            ),
            (request) -> {
                // request is a ReadResourceRequest
                return new io.modelcontextprotocol.spec.McpSchema.ReadResourceResult(
                    List.of(new io.modelcontextprotocol.spec.McpSchema.TextResourceContents(
                        request.uri(),
                        "text/plain",
                        "Use the get_listing tool to read specific ranges of the listing."
                    ))
                );
            }
        ));

        return resources;
    }
}
