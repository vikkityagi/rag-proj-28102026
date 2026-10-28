package com.example.rag.service;



import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class TestService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public TestService(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore) {

        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void loadKnowledge() throws IOException {

        Resource resource = new ClassPathResource("knowledge.txt");

        String content = StreamUtils.copyToString(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        );

        Document document = new Document(
                content,
                Map.of("source", "knowledge.txt")
        );

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(300)
                .build();

        List<Document> chunks = splitter.split(List.of(document));

        vectorStore.add(chunks);
    }

    public String ask(String question) {

        Advisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(
                        SearchRequest.builder()
                                .topK(3)
                                .build()
                )
                .build();

        return chatClient.prompt()
                .system("""
                        Answer using the provided knowledge context.
                        If the answer is not available in the context,
                        clearly say that the knowledge base does not
                        contain enough information.
                        """)
                .advisors(ragAdvisor)
                .user(question)
                .call()
                .content();
    }
}
