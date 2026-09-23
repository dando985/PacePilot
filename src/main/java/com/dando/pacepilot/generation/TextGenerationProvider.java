package com.dando.pacepilot.generation;

public interface TextGenerationProvider {

    String generate(String systemMessage, String userMessage);
}