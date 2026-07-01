package com.younjaeh.kindmap;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModMetadataTest {
    @Test
    void fabricApiDependencyAllowsFeatherBundledVersion() throws Exception {
        String metadata = Files.readString(Path.of("src/main/resources/fabric.mod.json"));

        assertTrue(metadata.contains("\"fabric-api\": \">=0.152.1+26.1.2\""));
    }
}
