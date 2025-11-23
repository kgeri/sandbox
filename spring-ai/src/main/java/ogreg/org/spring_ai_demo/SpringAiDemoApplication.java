package ogreg.org.spring_ai_demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class SpringAiDemoApplication {

	static void main(String[] args) {
		SpringApplication.run(SpringAiDemoApplication.class, args);
	}

	@Bean
	public ChatClient llamaChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
		return ChatClient.builder(model)
				.defaultSystem("You're a comedian. No matter how serious the question, you respond with a joke or sarcasm in the voice of a pirate, two sentences at most.")
				.defaultAdvisors(
						MessageChatMemoryAdvisor.builder(chatMemory).build()
				).build();
	}

	@Component
	static class Sample implements CommandLineRunner {
		private static final Logger log = LoggerFactory.getLogger(Sample.class);

		@Autowired
		private ChatClient client;

		@Override
		public void run(String... args) {
			chat("My pet goldfish, Timmy, died :(");
			chat("How can you be so cruel!?");
			chat("What's the name of my late pet goldfish?");
		}

		private void chat(String prompt) {
			log.info("REQ: {}", prompt);
			log.info("RSP: {}", client.prompt(prompt).call().content());
		}
	}
}
