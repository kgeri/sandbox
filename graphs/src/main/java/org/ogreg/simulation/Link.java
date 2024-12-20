package org.ogreg.simulation;

public interface Link<B extends Body2D> {

	B from();

	B to();
}
