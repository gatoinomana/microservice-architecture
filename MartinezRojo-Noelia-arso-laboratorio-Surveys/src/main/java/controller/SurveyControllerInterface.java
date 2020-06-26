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
	String createSurvey(String idCreator, String title, String instructions, 
			Date starts, Date ends, int minOptions, int maxOptions, 
			Visibility visibility) throws SurveyException;

	/**
	 * Añadir una opción a una encuesta
	 */
	void addOption(String surveyId, String text) throws SurveyException;
	
	/**
	 * Eliminar una opción de una encuesta. Utilidad para editar encuestas
	 */	
	void removeOption(String surveyId, String text) throws SurveyException;

	/**
	 * Eliminar una encuesta
	 */	
	void removeSurvey(String surveyId) throws SurveyException;
		
	/**
	 * Recuperar todas las encuestas
	 */
	List<Survey> getAllSurveys() throws SurveyException;

	/**
	 * Editar una encuesta. Devuelve el id de la encuesta, asignado desde la base de datos
	 * @return 
	 */	
	boolean editSurvey(String surveyId, String title, String instructions, 
			Date starts, Date ends, int minOptions, int maxOptions, 
			Visibility visibility) throws SurveyException;

	/**
	 * Consultar los resultados actuales de una encuesta. 
	 * Devuelve un diccionario asociando el texto de cada opción 
	 * a un número de votos
	 */	
	Map<String, Integer> getResults(
			String surveyId) throws SurveyException;;
	
	/**
	 * Actualiza los resultados actuales de una encuesta. 
	 * Toma un diccionario asociando el texto de cada opción 
	 * a un booleano (seleccionada/no seleccionada)
	 */	
	boolean respondSurvey(String surveyId, 
			Map<String, Boolean> responses) throws SurveyException;;

}
