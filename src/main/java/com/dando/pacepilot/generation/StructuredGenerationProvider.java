package com.dando.pacepilot.generation;

import java.util.Map;

// Defines how the application requests generated JSON with a specific structure
public interface StructuredGenerationProvider {

    String generate(String systemMessage, String userMessage, Map<String, Object> jsonSchema);
}