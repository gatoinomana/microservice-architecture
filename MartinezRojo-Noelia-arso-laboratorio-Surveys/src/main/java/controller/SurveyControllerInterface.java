package controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import model.Survey;
import model.Visibility;

public interface SurveyControllerInterface {

	/**
	 * Crear una encuesta. Devuelve el id de la encuesta, asignado desde la base de datos
	 */	
	String createSurvey(String title, String instructions, Date openingDateTime, 
			Date closingDateTime, int minOptions, int maxOptions, Visibility visibility);

	/**
	 * Añadir una opción a una encuesta
	 */
	void addOption(String surveyId, String text);
	
	/**
	 * Eliminar una opción de una encuesta. Utilidad para editar encuestas
	 */	
	void removeOption(String surveyId, String text);
		
	/**
	 * Recuperar todas las encuestas
	 */
	List<Survey> getAllSurveys();

	/**
	 * Editar una encuesta. Devuelve el id de la encuesta, asignado desde la base de datos
	 */	
	void editSurvey(String title, String instructions, Date openingDateTime, 
			Date closingDateTime, int minOptions, int maxOptions, Visibility visibility);

	/**
	 * Consultar los resultados de una encuesta. Devuelve un diccionario
	 * asociando el texto de cada opción a un porcentaje de votos
	 */	
	Map<String, Integer> getResults(String surveyId);
	
	/**
	 * Consultar los resultados de una encuesta. Devuelve un diccionario
	 * asociando el texto de cada opción a un porcentaje de votos.
	 * Toma un diccionario asociando el texto de cada opción a un booleano
	 * indicando seleccionada / no seleccionada
	 */	
	Map<String, Integer> respondSurvey(String surveyId, boolean[] responses);

}
