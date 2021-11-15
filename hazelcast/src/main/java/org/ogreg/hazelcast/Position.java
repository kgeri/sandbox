package org.ogreg.hazelcast;

import java.io.Serializable;

record Position(int version) implements Serializable, Versioned {
}
