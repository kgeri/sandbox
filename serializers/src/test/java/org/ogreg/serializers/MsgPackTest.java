package org.ogreg.serializers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

class MsgPackTest {
	private static final ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());

	static {
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE); // To avoid using property accessors
		mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY); // To use fields, ignoring visibility
		mapper.registerModule(new JavaTimeModule()); // For Serializing LocalDate and ZonedDateTime

		// To be able to serialize Sample.polymorph
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().build();
		mapper.activateDefaultTyping(ptv);
	}

	@Test
	void testSerializeAndDeserialize() throws IOException {
		SampleLike sample = Samples.newSample();
		byte[] buffer = mapper.writeValueAsBytes(sample);
		System.out.printf("[MsgPackTest] size=%d, buffer=%s%n", buffer.length, new String(buffer, UTF_8));

		Sample deserialized = mapper.readValue(buffer, Sample.class);
		Assertions.assertThat(deserialized).usingRecursiveComparison().isEqualTo(sample);
	}

	@Test
	void testSerializationPerformance() {
		// TODO once I can get the above to work...
	}
}
