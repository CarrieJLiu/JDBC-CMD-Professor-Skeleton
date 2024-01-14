/********************************************************************************************************
 * File:  GenerateRandomProfessorRecords.java Course materials (23W) CST8277
 * 
 * @date 2021 09
 * @author Mike Norman
 * 
 * @date December 2022
 * @author Teddy Yap
 * 
 * @date January 2021
 * @author Shariar (Shawn) Emami
 */
package jdbccmd;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdbccmd.LoggingOutputStream.LogLevel;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;
import uk.co.jemos.podam.api.ClassInfoStrategy;
import uk.co.jemos.podam.api.DefaultClassInfoStrategy;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 * Helper class that generates random professors and writes them to the database
 */
@Command(description = "Generate random professors", name = "jdbccmd.GenerateRandomProfessorRecords")
public class GenerateRandomProfessorRecords {
	
	//Get the current class type, in this case "jdbccmd.GenerateRandomProfessorRecords"
	protected static final Class<?> MY_KLASSNAME = MethodHandles.lookup().lookupClass();
	
	//Create a logger based on the type of the current class
	private static final Logger logger = LoggerFactory.getLogger(MY_KLASSNAME);

	//Error message to be customized for CMD display
	protected static final String CMDLINE_PARSING_ERROR_MSG = "cmdLine parsing error:  {}";
	
	//Message to be customized for time taken to complete the current task.
	protected static final String ELAPSED_TIME_MSG = "Elapsed time = {} ms";
	
	//Empty the table before adding new content to it.
	private static final String TRUNC_PROFESSOR = "TRUNCATE TABLE PROFESSOR";
	
	//Insert statement for professor table
	//TODO add the degree and major fields into the INSERT statement below
	protected static final String INSERT_PROFESSOR = "INSERT INTO PROFESSOR(LAST_NAME, FIRST_NAME, EMAIL, PHONE, CREATED) VALUES (?, ?, ?, ?, now())";

	public static void main(String[] args) {
		
		//Create an instance of CmdLineOptions class which has the variables we need from CMD.
		CmdLineOptions cmdLineOptions = new CmdLineOptions();
		
		//Pass the instance of CmdLineOptions class to picocli.CommandLine to automatically pass CMD arguments.
		CommandLine cmdLine = new CommandLine(cmdLineOptions);
		
		//Pass the name of the class which is operating CommandLine instance.
		cmdLine.setCommandName(MY_KLASSNAME.getName());
		
		try {
			
			//Parse incoming arguments 
			cmdLine.parseArgs(args);
			
		} catch (ParameterException e) {
			
			//Let user know the parsing has failed.
			logger.error(CMDLINE_PARSING_ERROR_MSG, e.getLocalizedMessage());
			logCmdLineUsage(e.getCommandLine(), LogLevel.ERROR);
			System.exit(-1);
			
		}
		
		if (cmdLineOptions.helpRequested) {
			
			//Display help
			logCmdLineUsage(cmdLine, LogLevel.INFO);
			
		} else {
			
			//Generate data for DB
			generateProfessors(cmdLineOptions.jdbcUrl, cmdLineOptions.username, cmdLineOptions.password,
					cmdLineOptions.count);
			
		}
		
	}

	public static void generateProfessors(String jdbcUrl, String username, String password, int genCount) {
		Instant startTime = Instant.now();

		//Create a properties object to store the password and username
		Properties dbProps = new Properties();
		dbProps.put("user", username);
		dbProps.put("password", password);

		//TODO Complete the try with resource below.
        try ( //try-with-resources:  auto-close'ing resources makes catch-finally logic easier
       
        	//Create a new connection using jdbcUrl and the db properties object above.
        	Connection connection = DriverManager.getConnection(jdbcUrl, dbProps);
    		
           	//TODO Create a PreparedStatement for INSERT_PROFESSOR.  We also want the ability to get the
            //generated id back - use Statement.RETURN_GENERATED_KEYS as second parameter
            PreparedStatement pstmtInsert = null;

       		//TODO Create a PreparedStatement for TRUNC_PROFESSOR.
            PreparedStatement pstmtTrunc = null;
        		
        ) {
        	
            //TODO Don't forget to execute the truncate statement here before performing any insert

			//PODAM - POjo DAta Mocker
			PodamFactory factory = new PodamFactoryImpl();
			ClassInfoStrategy classInfoStrategy = factory.getClassStrategy();
			
			//No need to generate primary key (id), database will do that for us
			((DefaultClassInfoStrategy) classInfoStrategy).addExcludedField(Professor.class, "id");
			factory.getStrategy().addOrReplaceTypeManufacturer(String.class, new ProfessorManufacturer());
			
			for (int cnt = 0, numRandomProfessors = genCount; cnt < numRandomProfessors; cnt++) {
				
				Professor randomProfessor = factory.manufacturePojoWithFullData(Professor.class);
				
				//Write randomProfessor to database
				//Setters are chosen based on the data type, setString, setInt, setDouble, etc.
				//The number as first argument represents the order of the '?' in the INSERT_PROFESSOR statement.
				pstmtInsert.setString(1, randomProfessor.getLastName());
				pstmtInsert.setString(2, randomProfessor.getFirstName());
				pstmtInsert.setString(3, randomProfessor.getEmail());
				pstmtInsert.setString(4, randomProfessor.getPhoneNumber());
				//TODO Set the degree and major fields of the INSERT_PROFESSOR statement.
				
				//Execute the query, return true if successful
				pstmtInsert.execute();
				
				//Get the generated keys from DB as the result of INSERT_PROFESSOR statement.
				//The ResultSet is AutoCloseable so it can be placed in a try-with-resource to be auto closed.
				try (ResultSet generatedKeys = pstmtInsert.getGeneratedKeys()) {
					
					if (generatedKeys.next()) { //If a key is returned
						
						int id = generatedKeys.getInt(1); //Get the key
						
						//Set the key to the professor and print it on the console
						randomProfessor.setId(id);
						
						logger.debug("created random professor \r\n\t{}", randomProfessor);
						
					} else {
						
						logger.error("could not retrieve generated PK");
						
					}
					
				}
				
			}
			
		//Catch any exceptions that might have been thrown from Connection, PreparedStatement, and/or ResultSet
		} catch (SQLException e) {
			
			logger.error("something went wrong inserting new professor, ", e);
			
		}

		Instant endTime = Instant.now();
		long elapsedTime = Duration.between(startTime, endTime).toMillis();
		logger.info(ELAPSED_TIME_MSG, elapsedTime);
		
	}

	/**
	 * Print the content of the LoggingOutputStream to the logger
	 * 
	 * @param cmdLine - command line instance
	 * @param los     - custom SLF4J instance
	 */
	protected static void logCmdLineUsage(CommandLine cmdLine, LogLevel level) {
		
		//Create a new custom output stream for SLF4J to print to logger.
		LoggingOutputStream los = new LoggingOutputStream(logger, level);
		PrintWriter pw = new PrintWriter(los);
		cmdLine.usage(pw);
		pw.flush();
		los.line();
		
	}

}
