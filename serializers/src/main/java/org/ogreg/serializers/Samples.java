package org.ogreg.serializers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

interface Samples {

	static SampleLike newSample() {
		return new Sample(
				"mySample",
				LocalDate.parse("2023-03-30"),
				ZonedDateTime.parse("2023-03-30T10:00:00Z"),
				new BigDecimal("1234567.890"),
				SampleEnum.TWO,
				new OtherSample("A"),
				List.of(new OtherSample("B"), new OtherSample("C"))
		);
	}
}

interface SampleLike {
}

class Sample implements SampleLike {

	final String name;

	final LocalDate date;

	final ZonedDateTime timestamp;

	final BigDecimal amount;

	final SampleEnum enumerated;

	final SampleLike polymorph;

	final List<SampleLike> aggregate;

	Sample() { // For Jackson...
		this(null, null, null, null, null, null, null);
	}

	Sample(String name, LocalDate date, ZonedDateTime timestamp, BigDecimal amount, SampleEnum enumerated, SampleLike polymorph, List<SampleLike> aggregate) {
		this.name = name;
		this.date = date;
		this.timestamp = timestamp;
		this.amount = amount;
		this.enumerated = enumerated;
		this.polymorph = polymorph;
		this.aggregate = aggregate;
	}
}

class OtherSample implements SampleLike {
	final String name;

	OtherSample(String name) {
		this.name = name;
	}
}

enum SampleEnum {
	ONE, TWO, THREE;
}
