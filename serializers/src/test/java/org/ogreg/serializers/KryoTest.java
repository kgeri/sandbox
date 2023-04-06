package org.ogreg.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

class KryoTest {
	private static final Kryo kryo = new Kryo();
	private static final byte[] buffer = new byte[512 * 1024];

	static {
		kryo.setRegistrationRequired(false); // So that we don't have to register every type we serialize (although it's probably a good idea!)
		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy()); // So that we don't have to have a default ctor
	}

	@Test
	void testSerializeAndDeserialize() {
		Output output = new Output(buffer);
		SampleLike sample = Samples.newSample();
		kryo.writeObject(output, sample);
		System.out.printf("[KryoTest] size=%d, buffer=%s%n", output.position(), new String(buffer, 0, output.position(), UTF_8));

		Sample deserialized = kryo.readObject(new Input(buffer), Sample.class);
		Assertions.assertThat(deserialized).usingRecursiveComparison().isEqualTo(sample);
	}

	@Test
	void testSerializationPerformance() {
		int iterations = 1000;
		Output output = new Output(buffer);
		Input input = new Input(buffer);
		SampleLike sample = Samples.newSample();

		{
			long before = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				output.reset();
				kryo.writeObject(output, sample);
			}
			System.out.printf("[KryoTest] writes/s=%d%n", iterations * SECONDS.toNanos(1) / (System.currentTimeMillis() - before));
		}

		{
			long before = System.nanoTime();
			for (int i = 0; i < iterations; i++) {
				input.reset();
				kryo.readObject(input, Sample.class);
			}
			System.out.printf("[KryoTest] reads/s=%d%n", iterations * SECONDS.toNanos(1) / (System.nanoTime() - before));
		}
	}
}
