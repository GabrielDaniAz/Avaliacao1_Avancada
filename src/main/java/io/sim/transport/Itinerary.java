package io.sim.transport;

public class Itinerary {

	private String[] itinerary;

	public Itinerary(String[] itinerary) {
		this.itinerary = itinerary;
	}

	// Retorna a rota do itinerário
	public String getRoute(){
		return itinerary[1];
	}

	// Retorna o ID do itinerário
	public String getIdItinerary(){
		return itinerary[0];
	}
}
